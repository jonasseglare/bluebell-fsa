(ns fsa.core
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::table (spec/map-of (constantly true) fn?))
(spec/def ::current (constantly true))
(spec/def ::state (spec/keys :req [::current ::table]))

(defn state? [x]
  (spec/valid? ::state x))

;;;;;; Helpers
(spec/def ::dispatch-pair (spec/cat :predicate fn?
                                    :action fn?))
(spec/def ::dispatch-pairs (spec/* ::dispatch-pair))

(defn combine2 [f g]
  (fn [state x]
    (g (f state x) x)))

(defn eval-dispatch-pair [state x]
  (fn [d]
    (if ((:predicate d) x)
      (let [y ((:action d) state x)]
        (assert (state? y))
        y))))

(defn add-word [word]
  (fn [words] (conj (or words []) word)))

(defn push-accumulated-word [state]
  (if (contains? state ::word)
    (update state ::words (add-word (::word state)))
    state))

(defn dissoc-word [state]
  (dissoc state ::word))

;;;;;; Composite ops
(defn combine [& args]
  (reduce combine2 args))

(defn find-first [f]
  (fn [state x]
    (if state
      state
      (f x))))

(defn dispatch [& args]
  (let [args (spec/conform ::dispatch-pairs args)]
    (fn [state x]
      (let [next (reduce (find-first (eval-dispatch-pair state x))
                         nil
                         (seq args))]
        (assert (state? next))
        next))))


;;;;;; Standard ops
(defn go-to [x]
  (fn [state _]
    (assoc state ::current x)))

(defn no-op [state _] state)

(defn accumulate-word [state x]
  (update
   state
   ::word (fn [word] (conj (or word []) x))))

(defn push-word [word]
  (fn [state _]
    (update state ::words (add-word word))))

(defn flush-word [state _]
  (-> state
      push-accumulated-word
      dissoc-word))

;;;;;;;;;; Common predicates

(defn whitespace? [c]
  (Character/isSpace c))

(def non-whitespace? (complement whitespace?))

(defn is= [x]
  #(= x %))

(defn end? [x]
  (= ::end x))

(defn or-pred [& args]
  (fn [x]
    (first (map (fn [f] (f x)) args))))

;;;;;;;;


(def test-state
  {::current :whitespace
   ::table {:whitespace no-op
            :word no-op}})

(spec/explain ::state test-state)
(assert (spec/valid? ::state test-state))

(defn add [state x]
  (assert (state? state))
  (let [{current ::current
         table ::table} state]
    (assert (contains? table current))
    ((get table current) state x)))

(defn parse [state x]
  (add (reduce add state x) ::end))

(defn get-words [state]
  (if (contains? state ::words)
    (map #(apply str %) (::words state))
    []))

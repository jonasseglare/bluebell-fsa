(ns fsa.core
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as stest]))

(spec/def ::table (spec/map-of (constantly true) fn?))
(spec/def ::current (constantly true))
(spec/def ::state (spec/keys :req [::current ::table]))

(defn state? [x]
  (spec/valid? ::state x))

;;;;;; Helpers
(spec/def ::predicate fn?)
(spec/def ::action fn?)
(spec/def ::parsed-dispatch-pair (spec/keys :req-un [::predicate ::action]))
(spec/def ::dispatch-pair (spec/cat :predicate fn?
                                    :action fn?))
(spec/def ::dispatch-pairs (spec/* ::dispatch-pair))

(defn dissoc-word [state]
  (dissoc state ::word))

(defn eval-dispatch-pair [state]
  (fn [d]
    (if ((:predicate d) (::input state))
      (let [y ((:action d) state)]
        (assert (state? y))
        y))))

(defn add-word [word]
  (fn [words] (conj (or words []) word)))

(defn push-accumulated-word [state]
  (if (contains? state ::word)
    (update state ::words (add-word [(::current state) (::word state)]))
    state))

;;;;;; Composite ops
(defn combine [& args]
  (apply comp (reverse args)))

(defn find-first [f]
  (fn [state x]
    (if state
      state
      (f x))))

(defn dispatcher [args]
  (fn [state]
    (let [next (reduce (find-first (eval-dispatch-pair state))
                       nil
                       (seq args))]
      (if (nil? next)
        (throw (ex-info
                "No matching form in dispatch"
                {:args args}))
        next))))

(defn dispatch [& args]
  (let [args (spec/conform ::dispatch-pairs args)]
    (dispatcher args)))
(spec/fdef dispatch :args ::dispatch-pairs)

;;;;;; Standard ops
(defn go-to [x]
  (fn [state]
    (assoc state ::current x)))

(def no-op identity)

(defn accumulate-word [state]
  (update
   state
   ::word (fn [word] (conj (or word []) (::input state)))))

(defn push-word [word]
  (fn [state]
    (update state ::words (add-word word))))

(defn tag-symbol [tag]
  (fn [state]
    (update state ::words (add-word [tag (::input state)]))))

(defn flush-word [state]
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

;; Use with (dispatcher [... end ... ])
(def end
  {:predicate end?
   :action flush-word})

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
    ((get table current) (assoc state ::input x))))

(defn parse [state x]
  (add (reduce add state x) ::end))

(spec/def ::word (spec/cat :type (constantly true)
                           :value (spec/* char?)))

(defn format-word [[tp v]]
  [tp (if (and (coll? v)
               (every? char? v))
        (apply str v)
        v)])

(defn get-words [state]
  (if (contains? state ::words)
    (map format-word (::words state))
    []))

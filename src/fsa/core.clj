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



;;;;;; Composite ops
(defn combine [& args]
  (reduce combine2 args))

(defn eval-dispatch-pair [state x]
  (fn [d]
    (if ((:predicate d) x) ((:action d) state x))))

(defn dispatch [& args]
  (let [args (spec/conform ::dispatch-pairs args)]
    (fn [state x]
      (let [next (first
                  (filter
                   state?
                   (map (eval-dispatch-pair state x) args)))]
        (assert (state? next))
        next))))


;;;;;; Standard ops
(defn go-to-state [x]
  (fn [state _]
    (assoc state ::current x)))

(defn no-op [state _] state)

;;;;;;;;


(def test-state
  {::current :whitespace
   ::table {:whitespace no-op
            :word no-op}})

(spec/explain ::state test-state)
(assert (spec/valid? ::state test-state))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(ns fsa.core
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::table (spec/map-of (constantly true) fn?))
(spec/def ::current (constantly true))
(spec/def ::state (spec/keys :req [::current ::table]))

(defn combine2 [f g]
  (fn [state x]
    (g (f state x) x)))

(defn combine [& args]
  (reduce combine2 args))

(defn go-to-state [x]
  (fn [state _]
    (assoc state ::current x)))

(defn no-op [state _]
  state)

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

(ns fsa.tokenizer
  (:require [fsa.core :as fsa]))

(defn remove-eol-comment [s]
  (let [i (.indexOf s "//")]
    (if (= -1 i)
      s
      (subs s 0 i))))

(defn remove-eol-comments [x]
  (apply
   str
   (map remove-eol-comment
        (clojure.string/split-lines x))))

(def graphviz-state
  {::fsa/current :idle
   ::fsa/table {:idle (fsa/dispatch)}})

;(fsa/parse graphviz-state "digraph kattskit { a b c }")


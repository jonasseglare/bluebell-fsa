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

(def start-string
  {:predicate (fsa/is= \")
   :action (fsa/go-to :string)})

(def end-string
  {:predicate (fsa/is= \")
   :action (fsa/combine
            fsa/flush-word
            (fsa/go-to :idle))})

(def escape-char
  {:predicate (fsa/is= \\)
   :action (fsa/go-to :escape)})

(def read-escaped
  (fsa/combine
   (fsa/go-to :string)
   fsa/accumulate-word))

(def read-char
  {:predicate (constantly true)
   :action fsa/accumulate-word})

(def ignore-whitespace
  {:predicate fsa/whitespace?
   :action fsa/no-op})

(def brackets (set "(){}[]"))

(defn bracket? [x]
  (contains? brackets x))

(def bracket
  {:predicate bracket?
   :action (fsa/tag-symbol :bracket)})

(def acc-word
  {:predicate fsa/identifier-char?
   :action (fsa/combine
            (fsa/go-to :word)
            fsa/accumulate-word)})

(def end-of-word
  {:predicate (complement fsa/identifier-char?)
   :action (fsa/combine
            fsa/flush-word
            (fsa/go-to :idle)
            fsa/re-add)})

;;(defn special-graphviz-symbols (set "->"))

(def graphviz-state
  {::fsa/current :idle
   ::fsa/table {:idle (fsa/dispatcher
                       [fsa/end
                        ignore-whitespace
                        start-string
                        bracket
                        acc-word])
                :string (fsa/dispatcher
                         [fsa/end
                          escape-char
                          end-string
                          read-char])
                :escape read-escaped
                :word (fsa/dispatcher
                       [fsa/end
                        acc-word
                        end-of-word])}})



;(fsa/get-words (fsa/parse graphviz-state "   abc   katt   (  a b c )   \"bra eller hur?\""))


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

(def graphviz-state
  {::fsa/current :idle
   ::fsa/table {:idle (fsa/dispatcher
                       [fsa/end
                        ignore-whitespace
                        start-string])
                :string (fsa/dispatcher
                         [fsa/end
                          escape-char
                          end-string
                          read-char])
                :escape read-escaped}})



;(fsa/get-words (fsa/parse graphviz-state "   \"ka\\ntt\"  \"skit\"    "))


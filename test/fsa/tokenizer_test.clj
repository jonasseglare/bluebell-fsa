(ns fsa.tokenizer-test
  (:require [fsa.core :as fsa]
            [fsa.tokenizer :refer :all]
            [clojure.test :refer :all] :reload-all))

(deftest graphviz-test
  (let [s (remove-eol-comments "asdf // kattskit\n d")]
    (is (= s "asdf  d"))
    (is (fsa/state? graphviz-state))
    (is (=
         [[:string "katt"] [:string "skit"]]
         (fsa/get-words (fsa/parse graphviz-state "   \"katt\"  \"skit\"    "))))
    (is (= '([:bracket \(] [:string "kattskit"] [:bracket \)])
           (fsa/get-words (fsa/parse graphviz-state "( \"kattskit\" )"))))))


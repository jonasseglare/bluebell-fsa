(ns fsa.tokenizer-test
  (:require [fsa.core :as fsa]
            [fsa.tokenizer :refer :all]
            [clojure.test :refer :all] :reload-all))

(deftest graphviz-test
  (let [s (remove-eol-comments "asdf // kattskit\n d")]
    (is (= s "asdf  d"))))


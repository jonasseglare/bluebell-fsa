(ns fsa.core-test
  (:require [clojure.test :refer :all]
            [fsa.core :refer :all :as fsa]))


(deftest a-test
  (testing "FIXME, I fail."
    (is (whitespace? \tab))
    (is (whitespace? \space))
    (is (not (whitespace? \a)))))

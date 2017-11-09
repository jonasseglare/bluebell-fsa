(ns fsa.core-test
  (:require [clojure.test :refer :all]
            [fsa.core :refer :all :as fsa]))

(def init-state {::fsa/current :whitespace
                 ::fsa/table {:whitespace (dispatch
                                           whitespace? no-op
                                           non-whitespace? (combine
                                                            accumulate-word
                                                            (go-to :word)))
                              :word (dispatch
                                     whitespace? (combine flush-word (go-to :whitespace))
                                     non-whitespace? accumulate-word)}})

(deftest a-test
  (testing "FIXME, I fail."
    (is (whitespace? \tab))
    (is (whitespace? \space))
    (is (not (whitespace? \a)))))

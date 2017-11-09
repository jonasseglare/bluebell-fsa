(ns fsa.core-test
  (:require [clojure.test :refer :all]
            [fsa.core :refer :all :as fsa]))

(def terminate-word (combine flush-word (go-to :whitespace)))

(def init-state {::fsa/current :whitespace
                 ::fsa/table {:whitespace (dispatch
                                           end? no-op
                                           whitespace? no-op
                                           non-whitespace? (combine
                                                            accumulate-word
                                                            (go-to :word)))
                              :word (dispatch
                                     end? terminate-word
                                     whitespace? terminate-word
                                     non-whitespace? accumulate-word)}})

(deftest a-test
  (testing "FIXME, I fail."
    (is (whitespace? \tab))
    (is (whitespace? \space))
    (is (not (whitespace? \a)))
    (is (= ["Jonas" "Östlund"]
           (get-words (parse init-state "  Jonas Östlund    "))))
    (is (= ["Jonas" "Östlund"]
           (get-words (parse init-state "  Jonas Östlund"))))))

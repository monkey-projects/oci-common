(ns monkey.oci.common.test.martian-test
  (:require [clojure.test :refer [deftest testing is]]
            [monkey.oci.common.martian :as sut]))

(deftest make-context
  (testing "creates martian context object"
    (is (some? (sut/make-context {} (constantly "test-url") [])))))

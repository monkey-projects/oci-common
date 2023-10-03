(ns monkey.oci.common.test.pagination-test
  (:require [clojure.test :refer [deftest testing is]]
            [monkey.oci.common.pagination :as sut]))

(deftest paginate
  (testing "invokes target fn, returns body"
    (is (= [:result] (sut/paginate (constantly {:body [:result]})))))

  (testing "invokes target fn with page parameter from `opc-next-page` header"
    (is (= [:first :second] (sut/paginate
                             (fn [page]
                               (cond
                                 (nil? page)
                                 {:body [:first]
                                  :headers {:opc-next-page "next"}}
                                 (= "next" page)
                                 {:body [:second]}))))))

  (testing "when item-fn is given, applies it to the body"
    (is (= [:result] (sut/paginate (constantly {:body {:items [:result]}}) :items)))))

(deftest paged-request
  (testing "returns a fn"
    (is (fn? (sut/paged-request (constantly {})))))
  
  (testing "invokes and derefs target call"
    (let [r (sut/paged-request (constantly (future :test-response)))]
      (is (= :test-response (r nil)))))

  (testing "passes extra args to `f`"
    (let [r (sut/paged-request (fn [arg]
                                 (future arg))
                               {:key "value"})]
      (is (= {:key "value"} (r nil)))))

  (testing "passes page to `f`"
    (let [r (sut/paged-request (fn [arg]
                                 (future arg))
                               {:key "value"})]
      (is (= "test page" (:page (r "test page"))))))

  (testing "passes additional arguments to `f`"
    (let [r (sut/paged-request (fn [ctx _]
                                 (future ctx))
                               :test-context {})]
      (is (= :test-context (r nil))))))

(deftest paged-route
  (testing "adds `query-schema` to the route"
    (is (map? (:query-schema (sut/paged-route {})))))

  (testing "retains original query schema"
    (is (not-empty (-> {:query-schema {:key :value}}
                       (sut/paged-route)
                       :query-schema
                       (select-keys [:key]))))))

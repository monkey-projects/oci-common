(ns monkey.oci.common.test.martian-test
  (:require [clojure.test :refer [deftest testing is]]
            [cheshire.core :as json]
            [martian.core :as martian]
            [monkey.oci.common
             [martian :as sut]
             [utils :as u]]
            [org.httpkit.fake :as hf]
            [schema.core :as s]))

(deftest make-context
  (testing "creates martian context object"
    (is (some? (sut/make-context {} (constantly "test-url") []))))

  (testing "JSON encodes body to camelCase"
    (let [conf {:tenancy-ocid "tenancy"
                :user-ocid "user"
                :key-fingerprint "fingerprint"
                :private-key (u/generate-key)}
          routes [{:route-name :test-route
                   :method :post
                   :consumes #{"application/json"}
                   :body-schema {:test-body
                                 {:first-name s/Str
                                  :last-name s/Str}}}]
          ctx (sut/make-context conf (constantly "http://test") routes)]
      (hf/with-fake-http ["http://test" (fn [_ opts _]
                                          (let [body (json/parse-string (:body opts))]
                                            {:status (if (and (some? (get body "firstName"))
                                                              (some? (get body "lastName")))
                                                       200
                                                       400)}))]
        (is (= 200 (-> ctx
                       (martian/response-for :test-route {:test-body {:first-name "First"
                                                                      :last-name "Last"}})
                       (deref)
                       :status)))))))

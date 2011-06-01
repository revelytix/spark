(ns sherpa.test-avro-utils
  (:use [clojure.test]
        [sherpa.avro-utils])
  (:import [sherpa.protocol SherpaProtocol]))

(deftest test-to-map
  (testing "Roundtrip from to Avro record back to map"
    (let [m {:sparql "foo" :parameters {} :properties {:timeout "60"}}]
      (is (= m (to-map SherpaProtocol/PROTOCOL
                       (avro-record SherpaProtocol/PROTOCOL "QueryRequest" m)))))))

(ns sherpa.test-avro-utils
  (:use [clojure.test]
        [sherpa.avro-utils])
  (:import [sherpa.protocol SherpaProtocol]
           [org.apache.avro Protocol Schema Schema$Type Schema$Field]
           [org.apache.avro.generic GenericData$EnumSymbol GenericData$Record]))

(deftest test-keyword-to-ns
  (are [expected input] (= expected (keyword-to-ns input))
       "a.b" :a/b
       "a.b.c" :a.b/c
       "c" :c
       "sherpa.test-avro-utils.d" ::d))

(def schema-e1 (Schema/createEnum "e1" nil nil ["a" "b" "c"]))

(def schema-rec1
     (doto (Schema/createRecord "rec1" nil nil false)
       (.setFields [(Schema$Field. "f1" (Schema/create Schema$Type/INT) nil nil)
                    (Schema$Field. "f2" (Schema/create Schema$Type/STRING) nil nil)])))

(def schema-rec2
     (doto (Schema/createRecord "rec2" nil nil false)
       (.setFields [(Schema$Field. "f3" schema-rec1 nil nil)
                    (Schema$Field. "f4" (Schema/create Schema$Type/STRING) nil nil)])))

(def example-protocol
     (doto (Protocol. "test" "my.ns")
       (.setTypes [schema-e1 schema-rec1 schema-rec2])))

(deftest test-to-avro-enum
  (testing "Create enum with no namespace"
    (let [val (to-avro {:sherpa-type :e1 :symbol :a} example-protocol)]
      (is (= GenericData$EnumSymbol (class val)))
      (is (= "a" (.toString val)))
      (is (= schema-e1 (.getSchema val))))))

(deftest test-to-avro-record
  (testing "Create record with simple types"
    (let [val (to-avro {:sherpa-type :rec1 :f1 10 :f2 "abc"} example-protocol)]
      (is (= GenericData$Record (class val)))
      (is (= "{\"f1\": 10, \"f2\": \"abc\"}" (.toString val)))
      (is (= schema-rec1 (.getSchema val))))))

(deftest test-to-map
  (testing "Roundtrip from to Avro record back to map"
    (let [m {:sparql "foo" :parameters {} :properties {:timeout "60"}}]
      (is (= m (to-map SherpaProtocol/PROTOCOL
                       (avro-record SherpaProtocol/PROTOCOL "QueryRequest" m)))))))

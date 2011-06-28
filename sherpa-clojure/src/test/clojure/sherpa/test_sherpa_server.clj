(ns sherpa.test-sherpa-server
  (:require [sherpa.sherpa-client :as cl])
  (:use [clojure.test]
        [sherpa.sherpa-server])
  (:import [org.apache.avro.generic GenericRecord GenericEnumSymbol]
           [org.apache.avro AvroRemoteException]
           [sherpa.protocol ErrorResponse ReasonCode]
           [spark.api.exception SparqlException]))

(deftest test-error-response
  (testing "with message"
    (let [msg "blah blah"
          ex (#'sherpa.sherpa-server/error-response msg)
          err (.getValue ex)]
      (is (instance? AvroRemoteException ex))
      (is (instance? GenericRecord err))
      (is (= "blah blah" (.toString (.get err "message"))))
      (is (instance? GenericEnumSymbol (.get err "code")))))
  (testing "with err"
    (let [ex (#'sherpa.sherpa-server/error-response (Throwable. "abc"))
          err (.getValue ex)]
      (is (instance? AvroRemoteException ex))
      (is (instance? GenericRecord err))
      (is (= "abc" (.toString (.get err "message"))))
      (is (instance? GenericEnumSymbol (.get err "code"))))))

(defn- check-error [exception-to-throw expected-message]
  (let [listener (reify SherpaListener
                   (query [listener request] (throw exception-to-throw)))
        server (run-sherpa listener {:host "localhost" :port 0 :join? false})
        client (cl/sherpa-client {:host "localhost" :port (.getPort server)})]
    (try
      (cl/query client "SELECT..." {} {})
      (is (true? false))
      (catch SparqlException e
        (let [cause (.getCause e)]
          (is (instance? ErrorResponse cause))
          (is (= expected-message (.toString (.message cause))))
          (is (instance? ReasonCode (.code cause))))))))

(deftest test-sherpa-error
  (check-error (NullPointerException. "abcdef")
               "abcdef"))

(deftest test-sherpa-error-nested
  (check-error (RuntimeException. "fail"
                                  (NullPointerException. "abcdef"))
               "fail : abcdef"))

;; (run-tests)

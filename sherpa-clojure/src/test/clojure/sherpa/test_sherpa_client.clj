(ns sherpa.test-sherpa-client
  (:require [sherpa.sherpa-client :as client]
            [sherpa.sherpa-server :as server] 
            [sherpa.dummy-server :as dummy])
  (:use [clojure.test]))

(deftest test-client
  (let [server (server/run-sherpa (dummy/dummy-server 5) {:host "localhost" :port 0 :join? false})
        client (client/sherpa-client {:host "localhost" :port (.getPort server)})]
    (try
      (let [result (client/query client "SELECT goes here" {} {})
            tuple (first result)]
        (is (= 5 (count result)))
        (is (= #{:x :y} (into #{} (keys tuple))))
        
        ;; iri values are instances of Avro generic record for "IRI"
        ;; type in sherpa protocol
        (is (= "{\"iri\": \"http:\\/\\/foobar.baz\\/this\\/uri\\/1\"}" (str (:x tuple)))))
      (finally
       (client/close client)
       (.close server)))))

(deftest test-client-0-rows
  (let [server (server/run-sherpa (dummy/dummy-server 0) {:host "localhost" :port 0 :join? false})
        client (client/sherpa-client {:host "localhost" :port (.getPort server)})]
    (try
      (let [result (client/query client "SELECT goes here" {} {})]
        (is (= 0 (count result))))
      (finally
       (client/close client)
       (.close server)))))

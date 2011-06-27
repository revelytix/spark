(ns sherpa.test-sherpa-client
  (:require [sherpa.sherpa-client :as client]
            [sherpa.sherpa-server :as server] 
            [sherpa.dummy-server :as dummy])
  (:use [clojure.test]))

(deftest test-client
  (let [server (server/run-sherpa (dummy/dummy-server 5) {:host "localhost" :port 0 :join? false})
        client (client/sherpa-client {:host "localhost" :port (.getPort server)})]
    (try
      (let [r (client/query client "SELECT goes here" {} {})
            result (:results r)
            tuple (first result)]
        (is (= 5 (count result)))
        (is (= #{:x :y} (into #{} (keys tuple))))
        
        ;; iri values are instances of Avro generic record for "IRI"
        ;; type in sherpa protocol
        (is (= "{\"iri\": \"http:\\/\\/foobar.baz\\/this\\/uri\\/1\"}" (str (:x tuple)))))
      (finally
       (client/shutdown client)
       (.close server)))))

(deftest test-client-0-rows
  (let [server (server/run-sherpa (dummy/dummy-server 0) {:host "localhost" :port 0 :join? false})
        client (client/sherpa-client {:host "localhost" :port (.getPort server)})]
    (try
      (let [r (client/query client "SELECT goes here" {} {})
            result (:results r)]
        (is (= 0 (count result))))
      (finally
       (client/shutdown client)
       (.close server)))))

(defn call-tracker-decorator [base-server]
  (let [counts {:query (ref 0)
                :data (ref 0)
                :cancel (ref 0)
                :close (ref 0)}
        up (fn [msg] (dosync (alter (msg counts) inc)))]
    {:server (reify server/SherpaListener
               (server/query [server request] (up :query) (server/query base-server request))
               (server/data [server request] (up :data) (server/data base-server request))
               (server/cancel [server request] (up :cancel) (server/cancel base-server request))
               (server/close [server request] (up :close) (server/close base-server request)))
     :counts counts}))

(deftest test-client-cancel
  (let [base-server (dummy/dummy-server 100)
        {:keys (server counts)} (call-tracker-decorator base-server) 
        server-proc (server/run-sherpa server {:host "localhost" :port 0 :join? false})
        client (client/sherpa-client {:host "localhost" :port (.getPort server-proc)})]
    (try
      (let [r (client/query client "SELECT goes here" {} {})
            result (:results r)]
        (is (= 0 @(:cancel counts)))
        (client/cancel client (:query-handle r))
        (is (= 1 @(:cancel counts))))
      (finally
       (client/shutdown client)
       (.close server-proc)))))

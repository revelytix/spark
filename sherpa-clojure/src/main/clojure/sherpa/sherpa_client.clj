;; Copyright 2011 Revelytix, Inc.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;; 
;;     http://www.apache.org/licenses/LICENSE-2.0
;; 
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
(ns sherpa.sherpa-client
  (:import [java.net InetSocketAddress InetAddress]
           [org.apache.avro.ipc SaslSocketTransceiver]
           [org.apache.avro.ipc.specific SpecificRequestor]
           [sherpa.client QueryManager]
           [sherpa.protocol Query QueryRequest DataRequest]))

(defprotocol SparqlClient
  (query [client sparql params props])
  (close [client]))

(defn sherpa-client [connect-map]
  (let [addr (InetSocketAddress. (InetAddress/getByName (:host connect-map)) (:port connect-map))
        _ (println "client connecting to " addr)
        transceiver (SaslSocketTransceiver. addr)
        requestor (SpecificRequestor. Query transceiver)
        query-api (SpecificRequestor/getClient Query requestor)]
    (reify SparqlClient
      (query [_ sparql params props]
        (let [mgr (QueryManager. query-api)
              ;; each item in the iter is a collection of values in
              ;; the same order as the vars array
              _ (.query mgr sparql params props)
              data-iter (.iterator mgr) 
              tuple-generator (partial zipmap (map keyword (.getVars mgr)))]
          ;; turn each data array into a map for each tuple
          (map tuple-generator (iterator-seq data-iter))))
      (close [_] (.close transceiver)))))



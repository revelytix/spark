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
           [sherpa.client QueryExecution]
           [sherpa.protocol SherpaServer QueryRequest DataRequest]))

;; The SparqlClient protocol 
(defprotocol SparqlClient
  "Abstract access to a SPARQL processor."
  (query [client sparql params props]
    "Execute a query on a SPARQL processor and return a sequence of results for the query.
      sparql - query string
      params - map of param names and values (keys are expected to be keywords)
      props - map of query properties like: timeout (keys are expected to be keywords)
    The return value is a map of info:
      :results The result sequence
      :query-handle An opaque query handle that can be used to cancel the query")
  (cancel [client query-handle] "Cancel the query based on query-handle.")
  (close [client query-handle] "Close the query execution and release all resources.")
  (shutdown [client] "Close the client and release all resources."))

(defn sherpa-client
  "Create a client that implements the SparqlClient protocol and talks using the Sherpa protocol to 
   a Sherpa server.  The connect-map should contain properties to specify which Sherpa server to 
   connect to, such as :host and :port."
  [connect-map]
  (let [addr (InetSocketAddress. (InetAddress/getByName (:host connect-map)) (:port connect-map))
        _ (println "client connecting to " addr)
        transceiver (SaslSocketTransceiver. addr)
        requestor (SpecificRequestor. SherpaServer transceiver)
        query-api (SpecificRequestor/getClient SherpaServer requestor)]
    (reify SparqlClient
      (query [_ sparql params props]
        (let [execution (QueryExecution. query-api)
              ;; each item in the iter is a collection of values in
              ;; the same order as the vars array
              str-props (zipmap (map (comp name first) props) (map second props))
              _ (.query execution sparql params str-props)
              data-iter (.iterator execution) 
              tuple-generator (partial zipmap (map keyword (.getVars execution)))]
          {:query-handle execution
           :results (map tuple-generator (iterator-seq data-iter))}))
      (cancel [_ query-handle] (.cancel query-handle))
      (close [_ query-handle] (.close query-handle))
      (shutdown [_] (.close transceiver)))))

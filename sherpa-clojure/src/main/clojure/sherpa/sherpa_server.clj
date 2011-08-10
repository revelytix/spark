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
(ns sherpa.sherpa-server
  (:require [clojure.tools.logging :as log])
  (:use [sherpa.avro-utils :only (to-avro from-avro)])
  (:import [org.apache.avro.ipc SaslSocketServer]
           [org.apache.avro AvroRemoteException]
           [java.net InetAddress InetSocketAddress]
           [sherpa.protocol SherpaProtocol]
           [sherpa.server MessageResponder ClojureResponder]))

(def PROTOCOL SherpaProtocol/PROTOCOL)

(defprotocol SherpaListener
  "Server-side listener on the sherpa protocol."
  (query [listener query-req])
  (data [listener data-req])
  (cancel [listener cancel-req])
  (close [listener close-req]))

(def DEFAULT-PORT 41414)

(defmulti sherpa-rpc (fn [msg listener request] msg))
(defmethod sherpa-rpc :default [msg listener request]
  (throw (RuntimeException. (str "Unknown message type: " msg))))

(defmacro add-rpc [msg return-type]
  `(defmethod sherpa-rpc ~msg [m# listener# avro-request#]
     (log/debug "query rpc, msg=" ~msg ", avro-req=" avro-request#)
     (let [request# (from-avro avro-request# PROTOCOL)
           protocol-fn# ~(resolve (symbol msg))
           response# (protocol-fn# listener# request#)
           avro-resp# (to-avro (assoc response# :sherpa-type (keyword ~return-type)) PROTOCOL)]
       avro-resp#)))

(add-rpc "query" "QueryResponse")
(add-rpc "data" "DataResponse")
(add-rpc "cancel" "CloseResponse")
(add-rpc "close" "CloseResponse")

(defn root-cause [^Throwable e]
  (if-let [cause (.getCause e)]
    (root-cause cause)
    e))

(defn serialize-stack-trace [e]
  (let [bos (java.io.ByteArrayOutputStream.)
        oos (java.io.ObjectOutputStream. bos)]
    (.writeObject oos (.getStackTrace e))
    (.close oos)
    (.toByteArray bos)))

(defn- to-exception-map [e]
  (if (not (nil? e))
    (let [cause (.getCause e)]
      {:sherpa-type :ServerException
       :message (.getMessage e)
       :errorType (.getName (class e))
       :stackTrace (java.nio.ByteBuffer/wrap (serialize-stack-trace e))
       :cause (if (not (= e cause))
                (to-exception-map cause))})))

(defn- error-response [exception-or-message]
  (let [exception (if (instance? Throwable exception-or-message)
                    (to-exception-map exception-or-message)
                    {:message exception-or-message})
        err-response (to-avro {:sherpa-type :ErrorResponse
                               :code {:sherpa-type :ReasonCode
                                      :symbol :Error}

                               :serverException exception

                               } PROTOCOL)]
    (AvroRemoteException. err-response)))

(defn responder
  "Adapt a SherpaListener into an Avro Responder."
  [listener]
  (ClojureResponder. PROTOCOL
                     (reify MessageResponder
                       (respond [this msg request]
                         (try
                           (let [msg-name (.getName msg)]
                             (sherpa-rpc msg-name listener (.get request
                                                                 (str msg-name "Request"))))
                           (catch Throwable t
                             (throw (error-response t))))))))

(defn run-sherpa
  "Run a sherpa server that directs calls to the listener, which should
   implement SherpaListener.  Options is a map of parameters regarding
   server startup and behavior.

   The following option keys are supported:
     :host the host name (default = \"localhost\")
     :port the listen port (default = DEFAULT-PORT = 41414), 0 to let the server pick the port
     :join? block the caller (default = true)"
  [listener options]
  (let [host (if-let [host (:host options)]
               (InetAddress/getByName host)
               (InetAddress/getLocalHost))
        port (or (:port options) DEFAULT-PORT)
        address (InetSocketAddress. host port)
        responder (responder listener)
        server (SaslSocketServer. responder address)]
    (log/info "Starting sherpa server on " address)
    (.start server)
    (when (:join? options true)
      (.join server))
    server))



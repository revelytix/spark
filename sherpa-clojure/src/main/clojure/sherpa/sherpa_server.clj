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
  (:import [org.apache.avro.ipc SaslSocketServer]
           [java.net InetAddress InetSocketAddress]
           [sherpa.protocol SherpaProtocol]
           [sherpa.server MessageResponder ClojureResponder]))

(defprotocol SherpaListener
  "Protocol for a server-side listener on the sherpa protocol."
  (query [listener protocol query-req])
  (data [listener protocol data-req])
  (cancel [listener protocol cancel-req])
  (close [listener protocol close-req]))

(def DEFAULT-PORT 41414)

(defn responder
  "Adapter a SherpaListener into an Avro Responder."
  [listener protocol]
  (ClojureResponder. protocol
                     (reify MessageResponder
                            (respond [this msg request]
                                     (try 
                                       (case (.getName msg)
                                             "query" (query listener protocol request)
                                             "data" (data listener protocol request)
                                             "cancel" (cancel listener protocol request)
                                             "close" (close listener protocol request)
                                             (throw (RuntimeException. "Unknown message: " (.getName msg))))
                                       (catch Throwable t
                                         (do (.printStackTrace t)
                                             (throw t))))))))

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
        responder (responder listener SherpaProtocol/PROTOCOL)
        server (SaslSocketServer. responder address)]
    (println "Starting sherpa server on " address)
    (.start server)
    (when (:join? options true)
      (.join server))
    server))



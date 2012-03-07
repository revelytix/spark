(ns sherpa.dummy-server
  (:require [clojure.tools.logging :as log])
  (:use [sherpa.sherpa-server]
        [clojure.test]
        [sherpa.avro-utils :only (to-avro from-avro)]))

(defn iri
  [iri]
  {:sherpa-type :IRI :iri iri})

(defn batch
  "Create a batch of test data starting at row start with size tuples."
  [start size]
  (vec
   (for [i (range start (+ start size))]
     (vector (iri (str "http://foobar.baz/this/uri/" i)) i))))

(deftest test-batch
  (are [expected start size]
       (= expected (batch start size))
       [] 1 0
       [[(iri "http://foobar.baz/this/uri/1") 1]] 1 1
       [[(iri "http://foobar.baz/this/uri/11") 11]
        [(iri "http://foobar.baz/this/uri/12") 12]
        [(iri "http://foobar.baz/this/uri/13") 13]] 11 3))

(defn query-call
  "Handle a query protocol call"
  [request]
  (log/info "Server got query request for " (:sparql request))
  (log/info "Server sending query response")
  {:sherpa-type "QueryResponse" :queryId "1" :vars ["x" "y"]})

(defn data-call
  "Handle a data protocol call where rows is the total number of rows in the dummy result set."
  [{:keys (startRow queryId maxSize) :as request} rows]
  (log/info "Server got data request for" request "and" rows)
  (if (zero? rows)
    (do 
      (log/info "Server sending empty response for 0 rows.")
      {:sherpa-type "QueryResponse"
       :queryId queryId
       :startRow (Integer. 1)
       :data []
       :more false})
    
    (if (<= startRow rows)        
      (let [size maxSize
            last (+ startRow size -1)
            size (if (> last rows)
                   (+ (- rows startRow) 1)
                   size)]
        ;;(log/info size "last=" last)
        
        {:sherpa-type "DataResponse"
         :queryId queryId
         :startRow startRow
         :data (batch startRow size)
         :more (< (+ startRow size -1) rows)})
      {:sherpa-type :ErrorResponse
       :code {:sherpa-type :ReasonCode
              :symbol :Error} 
       :message "Invalid request for rows outside the result set."})))

(defn dummy-server
  "Implement a dummy Sherpa server that returns a fixed result set of size rows."
  [rows]
  (reify SherpaListener
    (query [server request] (query-call request))
    (data [server request] (data-call request rows))
    (cancel [server request] {:sherpa-type :CloseResponse :queryId (:queryId request)})
    (close [server request] {:sherpa-type :CloseResponse :queryId (:queryId request)})))


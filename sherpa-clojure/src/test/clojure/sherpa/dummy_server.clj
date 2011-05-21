(ns sherpa.dummy-server
  (:use [sherpa.sherpa-server]
        [clojure.test]
        [clojure.string :only (join)])
  (:import [org.apache.avro.generic GenericData$Record]
           [sherpa.protocol SherpaProtocol]
   [sherpa.protocol Query QueryRequest DataRequest QueryResponse DataResponse ErrorResponse ReasonCode IRI]))

(defn avro-record
  "Convert from a map of vars and values to an Avro generic record of the specified type in the protocol."
  [protocol type vars]
  (let [schema (.getType protocol type)
        rec (GenericData$Record. schema)]
    (when (nil? schema) (throw (RuntimeException. (str "Invalid record type: " type))))
    (doseq [[k v] vars]
      (let [field (name k)]
        (when (nil? (.getField schema field))
          (throw (RuntimeException.
                  (str "Invalid field for " type ": " field
                       ". Expected: " (join ", " (map #(.name %) (.getFields schema)))))))
        (.put rec field v)))
    rec))

(defn mapmap
  [kf vf s]
  (zipmap (map kf s)
          (map vf s)))

(defn to-map
  "Convert from an Avro generic record to a map keyed by keywords based on the record field names."
  [protocol avro-record]
  (let [schema (.getSchema avro-record)
        fields (.getFields schema)
        field-names (map #(.name %) fields)]
    (println "fields=" fields)
    (println "field-names=" field-names)
    (mapmap #(keyword %) #(.get avro-record %) field-names)))

(deftest test-to-map
  (let [m {:sparql "foo" :parameters {} :properties {:timeout "60"}}]
    (is (= m (to-map SherpaProtocol/PROTOCOL
                     (avro-record SherpaProtocol/PROTOCOL "QueryRequest" m))))))

(defn iri
  [protocol iri]
  (avro-record protocol "IRI" {:iri iri}))

(defn batch
  "Create a batch of test data starting at row start with size tuples."
  [protocol start size]
  (vec
   (for [i (range start (+ start size))]
     (vector (iri protocol (str "http://foobar.baz/this/uri/" i)) i))))

(deftest test-batch
  (let [protocol SherpaProtocol/PROTOCOL]
    (are [expected start size]
         (= expected (batch protocol start size))
         [] 1 0
         [[(iri protocol "http://foobar.baz/this/uri/1") 1]] 1 1
         [[(iri protocol "http://foobar.baz/this/uri/11") 11]
          [(iri protocol "http://foobar.baz/this/uri/12") 12]
          [(iri protocol "http://foobar.baz/this/uri/13") 13]] 11 3)))

(defn query-call
  "Handle a query protocol call"
  [protocol request]
  (let [request-map (to-map protocol (.get request "query"))]
    (println "Server got query request for " (:sparql request-map))
    (println "Server sending query response")
    (avro-record protocol "QueryResponse" {:queryId "1" :vars ["x" "y"]})))

(defn data-call
  "Handle a data protocol call where rows is the total number of rows in the dummy result set."
  [protocol request rows]
  (let [{:keys (startRow queryId maxSize) :as request-map} (to-map protocol (.get request "data"))]
    (println "Server got data request for" request "to" request-map "and" rows)
    (if (zero? rows)
      (do 
        (println "Server sending empty response for 0 rows.")
        (avro-record protocol "DataResponse" {:queryId queryId
                                              :startRow 1
                                              :data []
                                              :more false}))
      (if (<= startRow rows)        
        (let [size maxSize
              last (+ startRow size -1)
              size (if (> last rows)
                     (+ (- rows startRow) 1)
                     size)]
          ;;(println "size=" size "last=" last)
          (avro-record protocol "DataResponse"
                       {:queryId queryId
                        :startRow startRow
                        :data (batch protocol startRow size)
                        :more (< (+ startRow size -1) rows)}))
        (avro-record protocol "ErrorResponse"
                     {:code ReasonCode/Error
                      :message "Invalid request for rows outside the result set."})))))

(comment
  (deftest test-batch
    (let [protocol SherpaProtocol/PROTOCOL]
      (is (= (avro-record protocol "DataResponse"))))))

(defn dummy-server
  "Implement a dummy Sherpa server that returns a fixed result set of size rows."
  [rows]
  (reify SherpaListener
         (query [server protocol request] (query-call protocol request))
         (data [server protocol request] (data-call protocol request rows))
         (cancel [server protocol request] (throw (UnsupportedOperationException. "TBD")))
         (close [server protocol request] (throw (UnsupportedOperationException. "TBD")))))


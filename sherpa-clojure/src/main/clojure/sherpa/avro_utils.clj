(ns sherpa.avro-utils
  (:use [clojure.string :only (join)])
  (:import [org.apache.avro.generic GenericData$Record]))

(defn avro-record
  "Convert from a Clojure map of vars and values to an Avro generic
   record of the specified type in the protocol.

     protocol - Avro Protocol instance
     type - name of the Avro record in the protocol
     vars - a Clojure map with keyword keys that match the record field names"
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

(defn to-map
  "Convert from an Avro generic record to a map keyed by keywords based
   on the record field names.

     protocol - Avro Protocol instance
     avro-record - Avro record instance"
  [protocol avro-record]
  (let [schema (.getSchema avro-record)
        fields (.getFields schema)
        field-names (map #(.name %) fields)]
    (zipmap (map keyword field-names)
            (map #(.get avro-record %) field-names))))

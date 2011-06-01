(ns sherpa.avro-utils
  (:use [clojure.string :only (join)])
  (:import [org.apache.avro.generic GenericData$Record]))

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

;; copied from internal Revelytix utils
(defn- mapmap
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

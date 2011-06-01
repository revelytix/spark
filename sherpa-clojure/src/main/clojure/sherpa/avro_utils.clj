(ns sherpa.avro-utils
  (:use [clojure.string :only (join)])
  (:import [clojure.lang Keyword IPersistentMap]
           [org.apache.avro Schema$Type]
           [org.apache.avro.generic GenericData$Record GenericData$EnumSymbol]))

(defmulti to-avro (fn [data protocol] (class data)))

;; default to identity, which will work for nearly all simple types
(defmethod to-avro :default [data protocol] data)

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
        (.put rec field (to-avro v protocol))))
    rec))

(defn avro-enum
  "Create an Avro enum"
  [schema symbol-value]
  (GenericData$EnumSymbol. schema symbol-value))

(defn fqname [ns name]
  (if ns
    (str ns "." name)
    name))

(defn keyword-to-ns [kw]
  (fqname (namespace kw) (name kw)))

(defn protocol-identifier [protocol]
  (fqname (.getNamespace protocol) (.getName protocol)))

;; If a map is passed, there are three possibilities as indicated
;; by the :sherpa-type key, which should be a namespaced keyword
;; indicating the schema name in the Avro protocol.
;;
;; 1) Avro record - All keyword keys are mapped to string keys
;;    in the record.
;; 2) Avro enum - The :symbol key will hold a keyword that
;;    is converted to the enum symbol
;; 3) Avro map, left as is because Avro handles any Map as a map.
(defmethod to-avro IPersistentMap [data protocol]
           (let [sherpa-type (:sherpa-type data)]
             (if sherpa-type
               (let [ns-type (keyword-to-ns sherpa-type)
                     schema (.getType protocol ns-type)]
                 (if (nil? schema)
                   (throw (RuntimeException. (str "Unknown schema " ns-type " in protocol " (protocol-identifier protocol)))))
                 (condp = (.getType schema) 
                     Schema$Type/RECORD (avro-record protocol ns-type (dissoc data :sherpa-type))
                     Schema$Type/ENUM (avro-enum schema (name (:symbol data)))))
               data)))



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

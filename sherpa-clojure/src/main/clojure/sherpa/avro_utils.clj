(ns sherpa.avro-utils
  (:use [clojure.string :only (join split)])
  (:import [clojure.lang Keyword Associative Sequential]
           [org.apache.avro Protocol Schema Schema$Type Schema$Field]
           [org.apache.avro.generic GenericData$Record GenericData$EnumSymbol
            GenericRecord GenericEnumSymbol]
           [org.apache.avro.util Utf8]))

(defmulti to-avro (fn [data protocol] (class data)))

;; default to identity, which will work for nearly all simple types
(defmethod to-avro :default [data protocol] data)

(defn avro-record
  "Convert from a Clojure map of vars and values to an Avro generic
   record of the specified type in the protocol.
     protocol - Avro Protocol instance
     type - name of the Avro record in the protocol
     vars - a Clojure map with keyword keys that match the record field names"
  [^Protocol protocol ^String type vars]
  (let [schema (.getType protocol type)
        rec (GenericData$Record. schema)]
    (when (nil? schema) (throw (RuntimeException. (str "Invalid record type: " type))))
    (doseq [[k v] vars]
      (let [field (name k)]
        (when (nil? (.getField schema field))
          (throw (RuntimeException.
                  (str "Invalid field for " type ": " field
                       ". Expected: " (join ", " (map (fn [^Schema$Field field] (.name field)) (.getFields schema)))))))
        (.put rec field (to-avro v protocol))))
    rec))

(defn avro-enum
  "Create an Avro enum"
  [^Schema schema ^String symbol-value]
  (GenericData$EnumSymbol. schema symbol-value))

(defn avro-map
  "Create an Avro map"
  [data-map protocol]
  (reduce (fn [m [k v]] (merge m {(name k) (to-avro v protocol)})) {} data-map))

(defn fqname [ns name]
  (if ns
    (str ns "." name)
    name))

(defn keyword-to-ns [kw]
  (fqname (namespace kw) (name kw)))

(defn protocol-identifier [^Protocol protocol]
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
(defmethod to-avro Associative [data ^Protocol protocol]
  (let [sherpa-type (:sherpa-type data)]
    (if sherpa-type
      (let [ns-type (keyword-to-ns sherpa-type)
            schema (.getType protocol ns-type)]
        (if (nil? schema)
          (throw (RuntimeException. (str "Unknown schema " ns-type " in protocol " (protocol-identifier protocol)))))
        (condp = (.getType schema) 
          Schema$Type/RECORD (avro-record protocol ns-type (dissoc data :sherpa-type))
          Schema$Type/ENUM (avro-enum schema (name (:symbol data)))))
      (avro-map data protocol))))

(defmethod to-avro Sequential [data ^Protocol protocol]
  (map #(to-avro % protocol) data))

;; If something is both Associative and Sequential (vectors), prefer Sequential
(prefer-method to-avro Sequential Associative)

(defmulti from-avro (fn [avro-data protocol] (class avro-data)))

(defmethod from-avro :default [avro-data protocol] avro-data)

(defmethod from-avro Utf8 [^Utf8 avro-data protocol] (.toString avro-data))

(defn fqname-to-keyword [fqname]
  (let [parts (split fqname #"\.")]
    (if (> (count parts) 1)
      (let [name (last parts)
            schema-ns (join "." (butlast parts))]
        (keyword schema-ns name))
      (keyword fqname))))

(defmethod from-avro GenericRecord [^GenericRecord avro-record ^Protocol protocol]
           (let [schema (.getSchema avro-record)
                 fields (.getFields schema)
                 field-names (map (fn [^Schema$Field field] (.name field)) fields)]
             (assoc (zipmap (map fqname-to-keyword field-names)
                            (map (fn [^String field-name] (from-avro (.get avro-record field-name) protocol))
                                 field-names))
               :sherpa-type (fqname-to-keyword (.getFullName schema)))))

(defmethod from-avro Associative [avro-map protocol]
           (reduce (fn [m [k v]] (merge m {(fqname-to-keyword k) (from-avro v protocol)}))
                   {}
                   avro-map))

(defmethod from-avro GenericEnumSymbol [^GenericEnumSymbol avro-enum ^Protocol protocol]
           {:sherpa-type (fqname-to-keyword (.getFullName (.getSchema avro-enum)))
            :symbol (keyword (.toString avro-enum))})

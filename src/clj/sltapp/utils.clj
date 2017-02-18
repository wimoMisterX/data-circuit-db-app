(ns sltapp.utils
  (:require [ring.util.response :refer [redirect]]
            [sltapp.validators :as validators]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.string :as string]))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn get-next-url [request]
  (get (:query-params request) "next" "/"))

(defn perform-action-and-redirect [url func alert]
  (if func (func))
  (-> (redirect url)
      (assoc-in [:flash :alerts] [alert])))

(defn verbose-name-to-db-field [name]
  (string/lower-case (string/replace name #" " "_")))

(defn db-field-to-verbose-name [name]
  (string/replace (string/replace name #"_" " ")  #"\b(.)" #(.toUpperCase (%1 1))))

(defn db-field-map-to-verbose-name [kw fields]
  {(keyword (db-field-to-verbose-name kw)) (map db-field-to-verbose-name fields)})

(defn validate-form-redirect [url validate-func db-func alert & error_url]
  (let [cleaned-obj (validate-func)]
    (if (validators/valid? cleaned-obj)
      (do
        (db-func)
        (-> (redirect url)
            (assoc-in [:flash :alerts] [alert])))
      (-> (redirect (or (first error_url) url))
          (assoc-in [:flash :form_errors] (validators/get-errors cleaned-obj))))))

(defn sql-time-to-local-time [dt]
  (f/unparse (f/formatter-local "yyyy-MM-dd HH:mm:ss") (t/to-time-zone (c/from-sql-time dt) (t/time-zone-for-id "Asia/Colombo"))))

(defn format-values [value_map]
  (reduce
    #(merge
      %1
      {(first %2) (if (and (not (nil? (last %2))) (string/includes? (name (first %2)) "_date")) (sql-time-to-local-time (last %2)) (last %2))})
    {}
    (seq value_map)))

(ns sltapp.utils
  (:require [ring.util.response :refer [redirect]]
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

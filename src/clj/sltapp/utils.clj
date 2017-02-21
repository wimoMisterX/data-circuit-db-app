(ns sltapp.utils
  (:use [hiccup.form :only (drop-down text-field)])
  (:require [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [sltapp.constants :refer [permissions]]
            [clojure.string :as string]))

(defn valid? [validated]
  (= (get validated 0) nil))

(defn get-errors [validated]
  (-> validated last(get :bouncer.core/errors)))

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
    (if (valid? cleaned-obj)
      (do
        (db-func)
        (-> (redirect url)
            (assoc-in [:flash :alerts] [alert])))
      (-> (redirect (or (first error_url) url))
          (assoc-in [:flash :form_errors] (get-errors cleaned-obj))))))

(defn get-or-create-app-settings []
  (if (empty? (db/get-app-settings))
    (db/insert-app-settings {:timezone "Asia/Colombo"
                             :datetime_format "yyyy-MM-dd HH:mm:ss"
                             :form_dropdowns (json/write-str {:qos_profile ["Platinum T2" "Bronze" "Gold T1"]
                                                              :current_vpls_id ["28291" "45556" "52361"]
                                                              :status ["Live" "Commissioned" "Decomissioned"]})}))
  (db/get-app-settings))

(defn sql-time-to-local-time [settings dt]
  (f/unparse (f/formatter-local (:datetime_format settings)) (t/to-time-zone (c/from-sql-time dt) (t/time-zone-for-id (:timezone settings)))))

(defn format-values [value_map]
  (let [format-time (partial sql-time-to-local-time (select-keys (get-or-create-app-settings) [:timezone :datetime_format]))]
    (reduce
      #(merge
        %1
        {(first %2) (if (and (not (nil? (last %2))) (string/includes? (name (first %2)) "_date")) (str (format-time (last %2))) (str (last %2)))})
      {}
      (seq value_map))))

(defn exception-occured? [func]
  (not (= false (try
                  (func)
                  (catch Exception e false)))))

(defn get-form-element [field value disabled]
  (if disabled
    (text-field {:class "form-control" :disabled true} "" value)
    (get
      (reduce
        #(merge
          %1
          {(first %2) (drop-down {:class "form-control" :disabled disabled} (name (first %2)) (last %2))})
        {}
        (seq (json/read-str (:form_dropdowns (get-or-create-app-settings)) :key-fn keyword)))
      (keyword field)
      (text-field {:class "form-control" :disabled disabled} field value))))

(defn format-search [q]
  (str "{" (string/join " " (for [x (string/split q #" ")] (str "\"" x "\""))) "}"))

(defn valid-search? [q]
  (exception-occured? #(json/read-str (format-search q) :key-fn keyword)))

(defn get-user-perms [identity]
  (if (:admin identity)
    permissions
    (:permissions identity)))

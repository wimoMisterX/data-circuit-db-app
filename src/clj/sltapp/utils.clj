(ns sltapp.utils
  (:use [hiccup.form :only (drop-down text-field)])
  (:require [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [sltapp.constants :refer [permissions auto_fill_fields form_to_field_map]]
            [clojure.string :as string]))

(defn not-nil? [v]
  (not (nil? v)))

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
          (assoc-in [:flash :alerts] [{:class "danger" :message "An error has been detected. Please scroll down and fix it before saving."}])
          (assoc-in [:flash :form_errors] (get-errors cleaned-obj))))))

(defn get-or-create-app-settings []
  (if (empty? (db/get-app-settings))
    (db/insert-app-settings {:form_dropdowns (json/write-str {:qos_profile ["Platinum T2" "Bronze" "Gold T1"]
                                                              :current_vpls_id ["28291" "45556" "52361"]
                                                              :status ["Requested" "Commissioned" "Decomissioned"]
                                                              :type ["VPLS" "VLL" "COPPER-VPLS"]
                                                              :connected_device ["ATN" "CTN6150" "CTN6200"]})}))
  (db/get-app-settings))

(defn format-sql-date [d]
  (f/unparse (f/formatter-local "YYYY-MM-dd") (l/to-local-date-time d)))

(defn format-values [value_map]
  (reduce
    (fn [formatted_value_map [kw v]]
      (merge formatted_value_map
             {kw (if (and (not-nil? v) (string/includes? (name kw) "_date")) (format-sql-date v) v)}))
    {}
    (seq value_map)))

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
          {(first %2) (drop-down {:class "form-control" :disabled disabled} (name (first %2)) (last %2) value)})
        {}
        (seq (json/read-str (:form_dropdowns (get-or-create-app-settings)) :key-fn keyword)))
      (keyword field)
      (text-field {:class (if (string/includes? field "_date") "form-control datetime-field" "form-control") :disabled disabled} field value))))

(defn format-search [q]
  (str "{" (string/join " " (for [x (string/split q #" ")] (str "\"" x "\""))) "}"))

(defn valid-search? [q]
  (exception-occured? #(json/read-str (format-search q) :key-fn keyword)))

(defn get-user-perms [identity]
  (if (:admin identity)
    permissions
    (:permissions identity)))

(defn get-editable-fields [form]
  (remove (set (form auto_fill_fields)) (form form_to_field_map)))

(defn format-value-for-db [field value]
  (if (string/includes? field "_date")
    (c/to-sql-date value)
    value))

(defn build-form-update-map [form params user_id & {:keys [initial_value] :or {initial_value {}}}]
  (reduce (fn [update_map field]
            (merge update_map {(keyword field) (format-value-for-db field (get params (keyword field)))}))
          (merge initial_value {(keyword (str (first (form auto_fill_fields)) "_id")) user_id})
          (get-editable-fields form)))

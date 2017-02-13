(ns sltapp.utils
  (:require [ring.util.response :refer [redirect]]))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn get-next-url [request]
  (get (:query-params request) "next" "/"))

(defn perform-action-and-redirect [url func alert]
  (if func (func))
  (-> (redirect url)
      (assoc-in [:flash :alerts] [alert])))

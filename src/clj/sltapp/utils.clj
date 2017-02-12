(ns sltapp.utils)

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn get-next-url [request]
  (get (:query-params request) "next" "/"))


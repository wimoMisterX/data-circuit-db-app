(ns sltapp.layout
  (:require [ring.util.http-response :refer [content-type ok]]
            [sltapp.templates.base :as base-templates]))

(declare ^:dynamic *app-context*)

(defn render [template & [params]]
  (content-type
    (ok template)
    "text/html; charset=utf-8"))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (base-templates/error-page error-details)})

(defn base-context-authenticated-access [request]
  {:admin (-> request :session :identity :admin)
   :full_name (str (-> request :session :identity :first_name) " " (-> request :session :identity :last_name))
   :flash_alerts (-> request :flash :alerts)})

(defn base-context-any-access [request]
  {:flash_alerts (-> request :flash :alerts)})

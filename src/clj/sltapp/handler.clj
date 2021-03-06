(ns sltapp.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [sltapp.layout :refer [error-page]]
            [sltapp.routes.home :refer [home-routes]]
            [sltapp.routes.auth :refer [auth-routes]]
            [compojure.route :as route]
            [sltapp.env :refer [defaults]]
            [mount.core :as mount]
            [sltapp.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (-> #'auth-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))

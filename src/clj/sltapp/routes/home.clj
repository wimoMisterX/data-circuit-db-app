(ns sltapp.routes.home
  (:require [sltapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.service.auth :as auth]
            [buddy.hashers :as hashers]
            [sltapp.templates.home :as home-templates]
            [clojure.java.io :as io]))

(defn home-page [request]
  (layout/render-new (home-templates/home {:name "Wimodya"})))

(defroutes home-routes
  (GET "/" [] home-page))


(ns sltapp.routes.home
  (:require [sltapp.layout :refer [render base-context]]
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
  (render (home-templates/home (base-context request))))

(defroutes home-routes
  (GET "/" [] home-page))


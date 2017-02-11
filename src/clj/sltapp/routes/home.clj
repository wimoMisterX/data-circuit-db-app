(ns sltapp.routes.home
  (:require [sltapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.service.auth :as auth]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]))

(defn home-page [request]
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] home-page)
  (GET "/about" [] about-page))


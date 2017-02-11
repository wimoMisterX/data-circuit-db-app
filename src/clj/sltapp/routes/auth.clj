(ns sltapp.routes.auth
  (:require [sltapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.service.auth :as auth]
            [sltapp.templates.base :as template]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]))

(defn login-page [request]
  (layout/render-new (template/login {})))

(defn register-page [request]
  (layout/render "register.html"))

(defn register-user [request]
  (let [user (validators/validate-user-register (:params request))]
    (if (validators/valid? user)
      (let [user-fields (last user)]
        (db/create-user!
          {:first_name (:first_name user-fields)
           :last_name (:last_name user-fields)
           :email (:email user-fields)
           :password (auth/encrypt-password (:password user-fields))
           :admin false
           :is_active true})
        (redirect "/login"))
      (layout/render "register.html" {:errors (validators/get-errors user)}))))

(defn login-user [request]
  (let [cleaned-user (validators/validate-user-login (:params request))]
    (if (validators/valid? cleaned-user)
      (let [user (db/get-user (last cleaned-user))]
        (if (and user (hashers/check (:password (last cleaned-user)) (:password user)))
          (-> (redirect (get-in request [:query-params :next] "/"))
              (assoc-in [:session :identity] (:email user)))
          (layout/render-new (template/login {:errors {:form ["Inavalid username/password"]}}))))
      (layout/render-new (template/login {:errors (validators/get-errors cleaned-user)})))))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

(defroutes auth-routes
  (POST "/login" [] login-user)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/logout" [] logout)
  (GET "/login" [] login-page))


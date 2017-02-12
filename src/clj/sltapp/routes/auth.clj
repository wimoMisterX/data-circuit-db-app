(ns sltapp.routes.auth
  (:require [sltapp.layout :refer [render]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.service.auth :as auth]
            [sltapp.templates.auth :as auth-templates]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]))

(defn login-page [request]
  (render (auth-templates/login {})))

(defn register-page [request]
  (render (auth-templates/register {})))

(defn register-user [request]
  (let [user (validators/validate-user-register (:params request))]
    (if (validators/valid? user)
      (let [user-fields (last user)]
        (let [email (:email user-fields)
              password (auth/generate-random-password 8)]
          (db/create-user!
            {:first_name (:first_name user-fields)
             :last_name (:last_name user-fields)
             :email email
             :password (auth/encrypt-password password)
             :admin (= "Admin" (:role user-fields))
             :is_active true})
          (render (auth-templates/register-success {:email email
                                                              :password password}))))
      (render (auth-templates/register {:errors (validators/get-errors user)})))))

(defn login-user [request]
  (let [cleaned-user (validators/validate-user-login (:params request))]
    (if (validators/valid? cleaned-user)
      (let [user (db/get-user (last cleaned-user))]
        (if (and user (hashers/check (:password (last cleaned-user)) (:password user)))
          (-> (redirect (get-in request [:query-params :next] "/"))
              (assoc-in [:session :identity] (:email user)))
          (render (auth-templates/login {:errors {:form ["Inavalid email/password"]}}))))
      (render (auth-templates/login {:errors (validators/get-errors cleaned-user)})))))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

(defroutes auth-routes
  (POST "/login" [] login-user)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/logout" [] logout)
  (GET "/login" [] login-page))


(ns sltapp.routes.home
  (:require [sltapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]))

(defn validate-user-login [user]
  (b/validate
    user
    :email v/email
    :password v/required))

(defn validate-user-register [user]
  (b/validate
    user
    :first_name v/required
    :last_name v/required
    :email [v/required v/email]
    :password v/required))

(defn encrypt-password [password]
  (hashers/encrypt password {:alg :pbkdf2+sha256
                             :salt "wimo"}))

(defn valid? [validated]
  (= (get validated 0) nil))

(defn get-errors [validated]
  (-> validated last(get :bouncer.core/errors)))

(defn home-page [request]
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render "about.html"))

(defn login-page [request]
  (layout/render "login.html"))

(defn register-page [request]
  (layout/render "register.html"))

(defn register-user [request]
  (let [user (validate-user-register (:params request))]
    (if (valid? user)
      (let [user-fields (last user)]
        (db/create-user!
          {:first_name (:first_name user-fields)
           :last_name (:last_name user-fields)
           :email (:email user-fields)
           :password (encrypt-password (:password user-fields))
           :admin false
           :is_active true})
        (redirect "/login"))
      (layout/render "register.html" {:errors (get-errors user)}))))

(defn login-user [request]
  (let [cleaned-user (validate-user-login (:params request))]
    (if (valid? cleaned-user)
      (let [user (db/get-user (last cleaned-user))]
        (if (and user (hashers/check (:password (last cleaned-user)) (:password user)))
          (-> (redirect (get-in request [:query-params :next] "/"))
              (assoc-in [:session :identity] (:email user)))
          (layout/render "login.html" {:errors {:form "Inavalid username/password"}})))
      (layout/render "login.html" {:errors (get-errors cleaned-user)}))))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

(defroutes home-routes
  (GET "/" [] home-page)
  (GET "/about" [] about-page)
  (POST "/login" [] login-user)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/logout" [] logout)
  (GET "/login" [] login-page))


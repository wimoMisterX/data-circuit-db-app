(ns sltapp.routes.auth
  (:require [sltapp.layout :refer [render base-context]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.service.auth :as auth]
            [sltapp.templates.auth :as auth-templates]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]))

(defn get-next-url [request]
  (get (:query-params request) "next" "/"))

(defn login-page [request]
  (render (auth-templates/login {:next (get-next-url request)})))

(defn register-page [request]
  (render (auth-templates/register (base-context request))))

(defn profile-page [request]
  (render (auth-templates/profile (base-context request))))

(defn manage-users-page [request]
  (render (auth-templates/manage-users (merge
                                         (base-context request)
                                         {:user_list (db/get-user-list {:email (-> request :identity :email)})}))))

(defn reset-password [request id]
  (let [password (auth/generate-random-password 8)
        user (db/get-user-by-id {:id id})]
    (db/update-user-password-id! {:password (auth/encrypt-password password)
                                  :id id})
    (render (auth-templates/reset-password (merge
                                             (base-context request)
                                             {:alerts [{:class "success" :message "Password reset successfully"}]
                                              :email (:email user)
                                              :password password})))))

(defn change-password [request]
  (let [form (validators/validate-change-password-form (:params request))]
    (let [valid_form (and
                       (validators/valid? form)
                       (validators/validate-change-password (:params request) (:password (db/get-user {:email (-> request :identity :email)}))))]
      (if valid_form (db/update-user-password-email! {:password (auth/encrypt-password (-> request :params :new_password))
                                                      :email (-> request :identity :email)}))
      (render (auth-templates/profile (merge
                                        (base-context request)
                                        {:alerts [{:class (if valid_form "success" "danger")  :message (if valid_form "Password updated successfully" "Invalid")}]
                                         :errors (validators/get-errors form)}))))))

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
          (render (auth-templates/register-success {:alerts [{:class "success" :message "User registerd successfully"}]
                                                    :email email
                                                    :password password}))))
      (render (auth-templates/register (merge
                                         (base-context request)
                                         {:errors (validators/get-errors user)}))))))

(defn login-user [request]
  (let [cleaned-user (validators/validate-user-login (:params request))]
    (if (validators/valid? cleaned-user)
      (let [user (db/get-user (last cleaned-user))]
        (if (and user (hashers/check (:password (last cleaned-user)) (:password user)))
          (-> (redirect (get-next-url request))
              (assoc-in [:session :identity] (select-keys user [:email :admin :first_name :last_name])))
          (render (auth-templates/login {:next (get-next-url request)
                                         :alerts [{:class "danger" :message "Invalid email/password"}]}))))
      (render (auth-templates/login {:next (get-next-url request)
                                     :errors (validators/get-errors cleaned-user)})))))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

(defroutes auth-routes
  (POST "/login" [] login-user)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/logout" [] logout)
  (GET "/profile" [] profile-page)
  (GET "/manage-users" [] manage-users-page)
  (GET "/reset-password/:id{[0-9]+}" [request id] (reset-password request id))
  (POST "/change-password" [] change-password)
  (GET "/login" [] login-page))


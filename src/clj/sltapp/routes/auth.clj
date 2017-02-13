(ns sltapp.routes.auth
  (:require [sltapp.layout :refer [render base-context-authenticated-access base-context-any-access]]
            [compojure.core :refer [defroutes GET POST]]
            [sltapp.utils :refer [get-next-url perform-action-and-redirect]]
            [ring.util.response :refer [redirect]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.service.auth :as auth]
            [sltapp.templates.auth :as auth-templates]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]))

(defn login-page [request next]
  (render (auth-templates/login (merge
                                  (base-context-any-access request)
                                  {:next next
                                   :errors (-> request :flash :form_errors)}))))

(defn register-page [request]
  (render (auth-templates/register (merge
                                     (base-context-authenticated-access request)
                                     {:errors (-> request :flash :form_errors)}))))

(defn profile-page [request]
  (render (auth-templates/profile (base-context-authenticated-access request))))

(defn manage-users-page [request]
  (render (auth-templates/manage-users (merge
                                         (base-context-authenticated-access request)
                                         {:user_list (db/get-user-list {:email (-> request :identity :email)})}))))

(defn modify-user [request id field value]
  (let [return-fn (partial perform-action-and-redirect "/manage-users")]
    (cond
      (= field "role") (return-fn #(db/update-user {:id-field "id" :id-value id :col "admin" :value (= value "admin")}) {:class "success" :message "User role changed succesfully!"})
      (= field "is_active") (return-fn #(db/update-user {:id-field "id" :id-value id :col "is_active" :value value}) {:class "success" :message "User status changed successfully!"})
    :else
      (return-fn nil {:class "danger" :message "This is a flash message"}))))

(defn reset-password [request id]
  (let [password (auth/generate-random-password 8)
        user (db/get-user {:cols ["email"] :id-field "id" :id-value id})]
    (db/update-user {:id-field "id" :id-value id :col "password" :value (auth/encrypt-password password)})
    (render (auth-templates/reset-password (merge
                                             (base-context-authenticated-access request)
                                             {:alerts [{:class "success" :message "Password reset successfully"}]
                                              :email (:email user)
                                              :password password})))))

(defn change-password [request]
  (let [form (validators/validate-change-password-form (:params request))]
    (let [valid_form (and
                       (validators/valid? form)
                       (validators/validate-change-password (:params request) (:password (db/get-user {:cols ["password"] :id-field "id" :id-value (-> request :identity :id)}))))]
      (if valid_form (db/update-user {:id-field "id" :id-value (str (-> request :identity :id)) :col "password" :value (auth/encrypt-password (-> request :params :new_password))}))
      (render (auth-templates/profile (merge
                                        (base-context-authenticated-access request)
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
          (render (auth-templates/register-success (merge
                                                     (base-context-authenticated-access request)
                                                     {:alerts [{:class "success" :message "User registerd successfully"}]
                                                      :email email
                                                      :password password})))))
      (-> (redirect "/register")
          (assoc-in [:flash :form_errors] (validators/get-errors user))))))

(defn login-user [request next]
  (let [cleaned-user (validators/validate-user-login (:params request))]
    (if (validators/valid? cleaned-user)
      (let [user (db/get-user {:id-field "email" :id-value (:email (last cleaned-user)) :cols ["id" "email" "first_name" "last_name" "is_active" "password" "admin"]})]
        (if (and user (hashers/check (:password (last cleaned-user)) (:password user)) (:is_active user))
          (-> (redirect (if (clojure.string/blank? next) "/" next))
              (assoc-in [:session :identity] (select-keys user [:id :email :admin :first_name :last_name])))
          (-> (redirect (str "/login?next=" next))
              (assoc-in [:flash :alerts] [{:class "danger" :message (if (:is_active user) "Invalid email/password" "User has been deactivated")}]))))
      (-> (redirect (str "/login?next=" next))
          (assoc-in [:flash :form_errors] (validators/get-errors cleaned-user))))))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})
      (assoc-in [:flash :alerts] [{:class "success" :message "Logged out successfully!"}])))

(defroutes auth-routes
  (POST "/login" [next :as r] (login-user r next))
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/logout" [] logout)
  (GET "/profile" [] profile-page)
  (GET "/manage-users" [] manage-users-page)
  (GET "/reset-password/:id{[0-9]+}" [id :as r] (reset-password r id))
  (GET "/modify-user/:id{[0-9]+}/:field{[a-z_]+}/:value{[0-9a-zA-Z]+}" [id field value :as r] (modify-user r id field value))
  (POST "/change-password" [] change-password)
  (GET "/login" [next :as r] (login-page r next)))


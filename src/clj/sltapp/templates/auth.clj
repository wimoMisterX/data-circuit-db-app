(ns sltapp.templates.auth
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
           [sltapp.templates.home :refer [base-home]]
           [sltapp.templates.base :refer [base-app render-error form-group]])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.form :only (label text-field password-field submit-button form-to drop-down)]))

(defn login [params]
  (base-app
    "Login"
    {:extrahead (include-css "/css/login.css")
     :content [:div {:class "wrapper"}
               (form-to {:class "form-signin"} [:post (str "/login?next=" (:next params))]
                (anti-forgery-field)
                [:h2 {:class "form-signin-heading"} "Please Login"]
                [:div {:class "input-group margin-bottom-sm"}
                 [:span {:class "input-group-addon"}
                  [:i {:class "fa fa-envelope-o fa-fw"}]]
                 (text-field {:class "form-control" :placeholder "Email Address"} "email")
                 (render-error (-> params :errors :email))]
                [:div {:class "input-group"}
                 [:span {:class "input-group-addon"}
                  [:i {:class "fa fa-key fa-fw"}]]
                 (password-field {:class "form-control" :placeholder "Password"} "password")
                 (render-error (-> params :errors :password))]
                (submit-button {:class "btn btn-lg btn-primary btn-block"} "Login"))]}))

(defn register [params]
  (base-home (merge
              params
              {:title "Register"
              :active_page "Register a User"
              :page_header "Register a User"
              :main_content (form-to {:class "form-horizontal"} [:post ""]
                             (anti-forgery-field)
                             (form-group "First Name" (text-field {:class "form-control"} "first_name") (-> params :errors :first_name))
                             (form-group "Last Name" (text-field {:class "form-control"} "last_name") (-> params :errors :last_name))
                             (form-group "Email" (text-field {:class "form-control"} "email") (-> params :errors :email))
                             (form-group "Role" (drop-down {:class "form-control"} "role" ["Admin" "User"]) (-> params :errors :role))
                             (form-group "" (submit-button {:class "btn btn-primary"} "Register") nil))})))

(defn register-success [params]
  (base-home (merge
              params
              {:title "Register"
              :active_page "Register a User"
              :page_header "Register a User"
              :main_content [:div
                             [:table {:class "table"}
                              [:caption "The login details of the user"]
                              [:tbody
                               [:tr
                                [:td "Email"]
                                [:td (:email params)]]
                               [:tr
                                [:td "Password"]
                                [:td (:password params)]]]]]})))

(defn profile [params]
  (base-home (merge
              params
              {:title "Profile"
              :active_page "Profile"
              :page_header "Change Password"
              :main_content (form-to {:class "form-horizontal"} [:post "/change-password"]
                             (anti-forgery-field)
                             (form-group "Current Password" (password-field {:class "form-control"} "current_password") (-> params :errors :current_password))
                             (form-group "New Password" (password-field {:class "form-control"} "new_password") (-> params :errors :new_password))
                             (form-group "Confirm New Password" (password-field {:class "form-control"} "confirm_new_password") (-> params :errors :confirm_new_password))
                             (form-group "" (submit-button {:class "btn btn-primary"} "Change Password") nil))})))

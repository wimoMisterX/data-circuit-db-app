(ns sltapp.templates.auth
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
           [sltapp.templates.home :refer [base-home]]
           [sltapp.templates.base :refer [base-app render-error form-group]])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.form :only (label text-field password-field submit-button form-to drop-down)]))

(defn base-auth [params]
  (base-app
    (:title params)
    {:extrahead (include-css "/css/login.css")
     :content [:div {:class "wrapper"}
               (form-to {:class "form-signin"} [:post (:post_url params)]
                (anti-forgery-field)
                [:h2 {:class "form-signin-heading"} (:form_heading params)]
                (render-error (-> params :errors :form))
                (concat (:fields params))
                (submit-button {:class "btn btn-lg btn-primary btn-block"} (:submit_button params)))]}))

(defn login [context]
  (base-auth (merge
               context
               {:title "Login"
                :post_url "/login"
                :form_heading "Please Login"
                :submit_button "Login"
                :fields [(text-field {:class "form-control" :placeholder "Email Address"} "email")
                         (render-error (-> context :errors :email))
                         (password-field {:class "form-control" :placeholder "Password"} "password")
                         (render-error (-> context :errors :password))]})))

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

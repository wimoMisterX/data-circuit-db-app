(ns sltapp.templates.auth
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
           [sltapp.templates.base :refer [base-app render-error]])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.form :only (text-field password-field submit-button form-to drop-down)]))

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

(defn register [context]
  (base-auth (merge
               context
               {:title "Register"
                :post_url "/register"
                :form_heading "Register a User"
                :submit_button "Register"
                :fields [(text-field {:class "form-control" :placeholder "First Name"} "first_name")
                         (render-error (-> context :errors :first_name))
                         (text-field {:class "form-control" :placeholder "Last Name"} "last_name")
                         (render-error (-> context :errors :last_name))
                         (text-field {:class "form-control" :placeholder "Email Address"} "email")
                         (render-error (-> context :errors :email))
                         (drop-down {:class "form-control"} "role" ["Admin" "User"])
                         (render-error (-> context :errors :role))]})))

(defn register-success [context]
  (base-app
    "Register Success"
    {:extrahead (include-css "/css/login.css")
     :content [:div {:class "wrapper"}
               [:div {:class "form-signin"}
                [:h2 "Registration Success"]
                [:table {:class "table no-borders"}
                 [:tr
                  [:td "Email"]
                  [:td (:email context)]]
                 [:tr
                  [:td "Password"]
                  [:td (:password context)]]]]]}))

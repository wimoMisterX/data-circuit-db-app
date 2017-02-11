(ns sltapp.templates.base
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.form :only (text-field password-field submit-button form-to)]))

(defn base-app [title context]
  (html5 {:lang "en"}
         [:head
          [:title (str "SLT APP - " title)]
          (include-css "/assets/bootstrap/css/bootstrap.min.css")
          (include-css "/assets/font-awesome/css/font-awesome.min.css")
          (:extrahead context)]
         [:body
          (:content context)
          (include-js "/assets/jquery/jquery.min.js")
          (include-js "/assets/tether/dist/js/tether.min.js")
          (include-js "/assets/bootstrap/js/bootstrap.min.js")
          (:extrabottom context)]))

(defn login [params]
  (base-app
    "Login"
    {:extrahead (include-css "/css/login.css")
     :content [:div {:class "wrapper"}
               (form-to {:class "form-signin"} [:post "/login"]
                (anti-forgery-field)
                [:h2 {:class "form-signin-heading"} "Please Login"]
                (render-error (-> params :errors :form))
                (text-field {:class "form-control" :placeholder "Email Address"} "email")
                (render-error (-> params :errors :email))
                (password-field {:class "form-control" :placeholder "Password"} "password")
                (render-error (-> params :errors :password))
                (submit-button {:class "btn btn-lg btn-primary btn-block"} "Login"))]}))

(defn render-error [errors]
  (if errors
    (map #(-> [:span {:class "alert alert-danger"} %]) errors)))

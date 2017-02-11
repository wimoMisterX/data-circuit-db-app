(ns sltapp.templates.base
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

(defn render-error [errors]
  (if errors
    (map #(-> [:span {:class "alert alert-danger"} %]) errors)))

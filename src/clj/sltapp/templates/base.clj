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

(defn error-page [error-details]
  (base-app
    "Something bad happened"
    {:extrahead (include-css "/css/error.css")
     :content [:div {:class "container-fluid"}
               [:div {:class "row-fluid"}
                [:div {:class "col-lg-12"}
                 [:div {:class "centering text-center"}
                  [:div {:class "text-center"}
                   [:h1
                    [:span {:class "text-danger"} (str "Error:" (:status error-details))]]
                   [:hr]
                   (if (:title error-details)
                     [:h2 {:class "without-margin"} (:title error-details)])
                   (if (:message error-details)
                     [:h4 {:class "text-danger"} (:message error-details)])]]]]]}))

(ns sltapp.templates.home
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
           [sltapp.templates.base :refer [base-app render-error]])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.form :only (text-field password-field submit-button form-to drop-down)]))

(defn nav-pills [active pills]
  (map #(-> [:li {:class (if (= active (:value %)) "active")}
             [:a {:href (:href %)} (:value %)]]) pills))

(defn base-home [params]
  (base-app
    (:title params)
    {:extrahead (include-css "/css/home.css")
     :content [:div
                [:nav {:class "navbar navbar-inverse navbar-fixed-top"}
                 [:div {:class "container-fluid"}
                  [:div {:class "navbar-header"}
                   [:button {:class "navbar-toggle collapsed" :type "button" :data-toggle "collapse" :data-target "#navbar" :aria-expanded "false"}
                    [:span {:class "sr-only"} "Toggle navigation"]
                    [:span {:class "icon-bar"}]
                    [:span {:class "icon-bar"}]
                    [:span {:class "icon-bar"}]]
                   [:a {:class "navbar-brand" :href "#"} "Slt App"]]
                  [:div {:id "navbar" :class "collapse navbar-collapse"}
                   [:ul {:class "nav navbar-nav navbar-right"}
                    (nav-pills (:active_page params) [{:href "/profile" :value "Profile"}
                                                      {:href "/logout" :value "Logout"}])]]]]
                [:div {:class "container-fluid"}
                 [:div {:class "row"}
                  [:div {:class "col-sm-3 col-md-2 sidebar"}
                   [:ul {:class "nav nav-sidebar"}
                    (nav-pills (:active_page params) [{:href "/" :value "Home"}
                                                      {:href "/form1" :value "Form 1"}
                                                      {:href "/form2" :value "Form 2"}
                                                      {:href "/form3" :value "Form 3"}
                                                      {:href "/form4" :value "Form 4"}
                                                      {:href "/form5" :value "Form 5"}])]]
                  [:div {:class "col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main"}
                   [:h1 {:class "page-header"} (:page_header params)]
                   (:main_content params)]]]]}))

(defn home [params]
  (base-home {:title "Home"
              :active_page "Home"
              :page_header (str "Welcome " (:name params))
              :main_content [:div {:class "row"}]}))


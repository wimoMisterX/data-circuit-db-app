(ns sltapp.templates.home
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
           [sltapp.templates.base :refer [base-app render-error render-alerts]])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.util :only (escape-html)]
       [hiccup.form :only (text-field password-field submit-button form-to drop-down)]))

(defn nav-pills [active pills]
  (map #(-> [:li {:class (if (= active (:value %)) "active")}
             [:a {:href (:href %)} (:value %)]]) (filter #(not (empty? %)) pills)))

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
                                                      {:href "/logout" :value "Logout"}])]
                   [:p {:class "navbar-text navbar-right"} (str "Signed in as " (:full_name params))]]]]
                [:div {:class "container-fluid"}
                 [:div {:class "row"}
                  [:div {:class "col-sm-3 col-md-2 sidebar"}
                   [:ul {:class "nav nav-sidebar"}
                    (nav-pills (:active_page params) [{:href "/" :value "Home"}
                                                      {:href "/form1" :value "New Circuit Commissioning"}
                                                      {:href "/form2" :value "BW Changing"}
                                                      {:href "/form3" :value "VPLS Changing"}
                                                      {:href "/form4" :value "Device Changing"}
                                                      {:href "/form5" :value "Disconnecting"}
                                                      (if (:admin params) {:href "/register" :value "Register a User"})
                                                      (if (:admin params) {:href "/manage-users" :value "Manage Users"})])]]
                  [:div {:class "col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main"}
                   [:h1 {:class "page-header"} (:page_header params)]
                   (render-alerts (:alerts params))
                   (:main_content params)]]]]}))

(defn home [params]
  (base-home (merge
               params
               {:title "Home"
                :active_page "Home"
                :page_header (str "Welcome " (first (clojure.string/split (:full_name params) #"\s")))
                :main_content [:div {:class "row"}]})))


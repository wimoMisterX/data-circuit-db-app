(ns sltapp.templates.home
 (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
           [sltapp.templates.base :refer [base-app render-error render-alerts form-group]]
           [clj-time.core :as t]
           [sltapp.utils :as utils])
 (:use [hiccup.page :only (html5 include-css include-js)]
       [hiccup.util :only (escape-html)]
       [hiccup.form :only (text-area text-field password-field submit-button form-to drop-down)]))

(defn nav-pills [active pills]
  (map #(-> [:li {:class (if (= active (:value %)) "active")}
             [:a {:href (:href %)} (:value %)]]) (filter #(not (empty? %)) pills)))

(defn has-permission [is_admin required_perms user_perms]
  (or is_admin (some required_perms user_perms)))

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
                   [:a {:class "navbar-brand" :href "#"} "Data Circuit Database App"]]
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
                                                      (if (has-permission (:admin params) #{"new_circuit_connecting"} (:user_permissions params)) {:href "/new-circuit-connecting" :value "New Circuit Connecting"})
                                                      {:href "/connected-circuits" :value "Connected Circuits"}
                                                      {:href "/disconnected-circuits" :value "Disconnected Circuits"}
                                                      (if (:admin params) {:href "/register" :value "Register a User"})
                                                      (if (:admin params) {:href "/manage-users" :value "Manage Users"})
                                                      (if (:admin params) {:href "/app-settings" :value "App Settings"})])]]
                  [:div {:class "col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main"}
                   [:h1 {:class "page-header"} (:page_header params)]
                   (render-alerts (concat (:alerts params) (:flash_alerts params)))
                   (:main_content params)]]]]}))

(defn home [params]
  (base-home (merge
               params
               {:title "Home"
                :active_page "Home"
                :page_header (str "Welcome " (first (clojure.string/split (:full_name params) #"\s")))
                :main_content [:div {:class "row"}]})))

(defn new-circuit-connecting [params]
  (base-home (merge
               params
               {:title "New Circuit Connecting"
                :active_page "New Circuit Connecting"
                :page_header "New Circuit Connecting"
                :main_content (form-to {:class "form-horizontal"} ["POST" "/new-circuit-connecting"]
                               (anti-forgery-field)
                               (for [field (:fields params)]
                                (form-group
                                  (utils/db-field-to-verbose-name field)
                                  (utils/get-form-element field "" false)
                                  (get (:errors params) (keyword field))))
                               (form-group "" (submit-button {:class "btn btn-primary"} "Save") nil))})))

(defn edit-circuit [params]
  (base-home (merge
               params
               {:title "Edit Circuit"
                :active_page "Connected Circuits"
                :page_header "Edit Circuit"
                :main_content [:div
                               [:div {:class "form-horizontal info-form"}
                                (for [field (:info_fields params)]
                                 (form-group (utils/db-field-to-verbose-name field) (text-field {:class "form-control" :disabled true} "" (get (:values params) (keyword field))) nil))]
                               (for [form-type (seq (:form_to_fields params))]
                                (let [title (name (first form-type)) fields (last form-type)]
                                 (form-to {:class "form-horizontal"} ["PUT" (str "/edit-circuit/" (-> params :values :id) "/" title)]
                                  (anti-forgery-field)
                                  [:h3 (utils/db-field-to-verbose-name title)]
                                  (for [field fields]
                                   (form-group
                                     (utils/db-field-to-verbose-name field)
                                     (utils/get-form-element field (get (:values params) (keyword field)) (contains? (set (:disabled_fields params)) field))
                                     (get (:errors params) (keyword field))))
                                  (form-group "" (submit-button {:class "btn btn-primary"} "Save") nil))))]})))

(defn connected-circuits [params]
  (base-home (merge
               params
               {:title "Connected Circuits"
                :active_page "Connected Circuits"
                :page_header "Connected Circuits"
                :main_content [:div
                               [:div {:class "pull-left"}
                                [:a {:class "btn btn-primary pull-left" :href (str "/export-csv/connected?q=" (:search params))} "Export to CSV"]]
                               (form-to {:class "form-inline search-box"} ["GET" "/circuit-search/connected"]
                                 (anti-forgery-field)
                                 [:div {:class "input-group"}
                                  (text-field {:class "form-control" :placeholder "Search"} "q" (:search params))
                                  [:span {:class "input-group-btn"} (submit-button {:class "btn btn-primary"} "Search")]])
                               [:table {:class "table table-bordered table-hover"}
                                [:thead (for [header (:table_headers params)] [:th {:class "fit-column"} (utils/db-field-to-verbose-name header)])]
                                [:tbody
                                 (for [row (:rows params)]
                                  [:tr
                                   [:td [:a {:href (str "/edit-circuit/" (:id row))} (:site_id row)]]
                                   (for [header (remove #{"site_id"} (:table_headers params))]
                                    [:td (get row (keyword header))])])]]]})))

(defn disconnected-circuits [params]
  (base-home (merge
               params
               {:title "Disconnected Circuits"
                :active_page "Disconnected Circuits"
                :page_header "Disconnected Circuits"
                :main_content [:div
                               [:div {:class "pull-left"}
                                [:a {:class "btn btn-primary pull-left" :href (str "/export-csv/disconnected?q=" (:search params))} "Export to CSV"]]
                               (form-to {:class "form-inline search-box"} ["GET" "/circuit-search/disconnected"]
                                 (anti-forgery-field)
                                 [:div {:class "input-group"}
                                  (text-field {:class "form-control" :placeholder "Search"} "q" (:search params))
                                  [:span {:class "input-group-btn"} (submit-button {:class "btn btn-primary"} "Search")]])
                               [:table {:class "table table-bordered table-hover"}
                                [:thead (for [header (:table_headers params)] [:th {:class "fit-column"} (utils/db-field-to-verbose-name header)])]
                                [:tbody
                                 (for [row (:rows params)]
                                  [:tr
                                   (for [header (:table_headers params)]
                                    [:td (get row (keyword header))])])]]]})))

(defn app-settings [params]
  (base-home (merge
               params
               {:title "App Settings"
                :active_page "App Settings"
                :page_header "App Settings"
                :main_content (form-to {:class "form-horizontal"} ["PUT" "/update-app-settings"]
                               (anti-forgery-field)
                               (form-group "Timezone" (drop-down {:class "form-control"} "timezone" (t/available-ids) (-> params :values :timezone)) (-> params :errors :timezone))
                               (form-group "Date Time Format" (text-field {:class "form-control"} "datetime_format" (-> params :values :datetime_format)) (-> params :errors :datetime_format))
                               (form-group "Form Dropdowns" (text-area {:class "form-control"} "form_dropdowns" (-> params :values :form_dropdowns)) (-> params :errors :form_dropdowns))
                               (form-group "" (submit-button {:class "btn btn-primary"} "Save") nil))})))


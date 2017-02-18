(ns sltapp.routes.home
  (:require [sltapp.layout :refer [render base-context-authenticated-access error-page]]
            [compojure.core :refer [defroutes GET POST PUT]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.constants :refer [form_to_field_map auto_fill_fields]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.utils :as utils]
            [sltapp.templates.home :as home-templates]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.string :as string]))

(defn home-page [request]
  (render (home-templates/home (base-context-authenticated-access request))))

(defn connected-circuits-page [request]
  (render (home-templates/connected-circuits (merge
                                              (base-context-authenticated-access request)
                                              {:table_headers (distinct (apply concat (vals (dissoc form_to_field_map :disconnecting))))
                                               :rows (map utils/format-values (db/get-connected-circuit-list))}))))

(defn disconnected-circuits-page [request]
  (render (home-templates/disconnected-circuits (merge
                                                  (base-context-authenticated-access request)
                                                  {:table_headers (distinct (apply concat (vals form_to_field_map)))
                                                   :rows (map utils/format-values (db/get-disconnected-circuit-list))}))))

(defn new-circuit-connecting-page [request]
  (render (home-templates/new-circuit-connecting (merge
                                                  (base-context-authenticated-access request)
                                                  {:fields (remove (set (:new_circuit_connecting auto_fill_fields)) (:new_circuit_connecting form_to_field_map))
                                                   :errors (-> request :flash :form_errors)}))))

(defn edit-circuit-page [request id]
  (if (nil? (db/circuit-in-state {:id id :state "connected"}))
    (error-page {:status 404 :title "Not found"})
    (render (home-templates/edit-circuit (merge
                                          (base-context-authenticated-access request)
                                          {:values (utils/format-values (db/get-circuit-info {:id id}))
                                           :errors (-> request :flash :form_errors)
                                           :info_fields (:new_circuit_connecting form_to_field_map)
                                           :disabled_fields (apply concat (vals auto_fill_fields))
                                           :form_to_fields (dissoc form_to_field_map :new_circuit_connecting)})))))

(defn add-circuit [request]
  (let [cleaned-circuit (validators/validate-new-circuit (:params request))]
    (if (validators/valid? cleaned-circuit)
      (let [circuit (last cleaned-circuit)]
        (db/insert-new-circuit (merge circuit {:commissioned_by_id (-> request :session :identity :id)
                                               :commissioned_date (c/to-sql-time (t/now))
                                               :state "connected"}))
        (-> (redirect "/new-circuit-connecting")
          (assoc-in [:flash :alerts] [{:class "success" :message "Circuit added successfully"}])))
      (-> (redirect "/new-circuit-connecting")
          (assoc-in [:flash :form_errors] (validators/get-errors cleaned-circuit))))))

(defn update-circuit [request id form]
  (let [return-fn (partial utils/validate-form-redirect (str "/edit-circuit/" id))]
    (cond
      (= form "bw_changing") (return-fn
                               #(validators/validate-bw-changing (:params request))
                               #(db/clj-expr-generic-update {:id id :table "circuit" :updates {:bandwidth_change_reason (-> request :params :bandwidth_change_reason)
                                                                                               :current_bandwidth_mpbs (-> request :params :current_bandwidth_mpbs)
                                                                                               :bandwidth_change_date (c/to-timestamp (t/now))
                                                                                               :bandwidth_update_by_id (-> request :session :identity :id)}})
                               {:class "success" :message "Bw Changing details saved!"})
      (= form "vpls_changing") (return-fn
                                 #(validators/validate-vpls-changing (:params request))
                                 #(db/clj-expr-generic-update {:id id :table "circuit" :updates {:vpls_changed_reason (-> request :params :vpls_changed_reason)
                                                                                                 :current_vpls_id (-> request :params :current_vpls_id)
                                                                                                 :vpls_changed_date (c/to-sql-time (t/now))
                                                                                                 :vpls_changed_by_id (-> request :session :identity :id)}})
                                 {:class "success" :message "Vpls Changing details saved!"})
      (= form "device_changing") (return-fn
                                   #(validators/validate-device-changing (:params request))
                                   #(db/clj-expr-generic-update {:id id :table "circuit" :updates {:new_device_connected_reason (-> request :params :new_device_connected_reason)
                                                                                                   :new_device_connected_date (c/to-sql-time (t/now))
                                                                                                   :new_device_connected_by_id (-> request :session :identity :id)}})
                                   {:class "success" :message "Device Changing details saved!"})
      (= form "disconnecting") (utils/validate-form-redirect
                                 "/disconnected-circuits"
                                 #(validators/validate-disconnecting (:params request))
                                 #(db/clj-expr-generic-update {:id id :table "circuit" :updates {:state "disconnected"
                                                                                                 :disconnected_reason (-> request :params :disconnected_reason)
                                                                                                 :comments (-> request :params :comments)
                                                                                                 :disconnected_date (c/to-sql-time (t/now))
                                                                                                 :disconnected_by_id (-> request :session :identity :id)}})
                                 {:class "success" :message "Circuit is disconneted!"}
                                 (str "/edit-circuit/" id))
    :else
      (-> (redirect (str "/edit-circuit/" id))
          (assoc-in [:flash :alerts] [{:class "danger" :message "Invalid circuit edit type"}])))))

(defroutes home-routes
  (GET "/" [] home-page)
  (GET "/new-circuit-connecting" [] new-circuit-connecting-page)
  (POST "/new-circuit-connecting" [] add-circuit)
  (GET "/edit-circuit/:id{[0-9]+}" [id :as r] (edit-circuit-page r id))
  (PUT "/edit-circuit/:id{[0-9]+}/:form{[a-z_]+}" [id form :as r] (update-circuit r id form))
  (GET "/connected-circuits" [] connected-circuits-page)
  (GET "/disconnected-circuits" [] disconnected-circuits-page))

(ns sltapp.routes.home
  (:require [sltapp.layout :refer [render base-context-authenticated-access]]
            [compojure.core :refer [defroutes GET POST PUT]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.constants :refer [form_to_field_map auto_fill_fields]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.utils :as utils]
            [sltapp.templates.home :as home-templates]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clojure.string :as string]))

(defn home-page [request]
  (render (home-templates/home (base-context-authenticated-access request))))

(defn connected-circuits-page [request]
  (render (home-templates/connected-circuits (merge
                                              (base-context-authenticated-access request)
                                              {:table_headers (apply concat (vals (dissoc form_to_field_map :disconnecting)))
                                               :rows (db/get-connected-circuit-list)}))))

(defn disconnected-circuits-page [request]
  (render (home-templates/disconnected-circuits (merge
                                                  (base-context-authenticated-access request)
                                                  {:table_headers (apply concat (vals form_to_field_map))
                                                   :rows (db/get-disconnected-circuit-list)}))))

(defn new-circuit-connecting-page [request]
  (render (home-templates/new-circuit-connecting (merge
                                                  (base-context-authenticated-access request)
                                                  {:fields (remove (set (:new_circuit_connecting auto_fill_fields)) (:new_circuit_connecting form_to_field_map))
                                                   :errors (-> request :flash :form_errors)}))))

(defn edit-circuit-page [request id]
  (render (home-templates/edit-circuit (merge
                                        (base-context-authenticated-access request)
                                        {:values (db/get-circuit-info {:id id})
                                         :info_fields (:new_circuit_connecting form_to_field_map)
                                         :disabled_fields (apply concat (vals auto_fill_fields))
                                         :form_to_fields (dissoc form_to_field_map :new_circuit_connecting)}))))

(defn add-circuit [request]
  (let [cleaned-circuit (validators/validate-new-circuit (:params request))]
    (if (validators/valid? cleaned-circuit)
      (let [circuit (last cleaned-circuit)]
        (db/insert-new-circuit (merge circuit {:commissioned_by_id (-> request :session :identity :id)
                                               :commissioned_date (c/to-sql-time (l/local-now))
                                               :state "connected"}))
        (-> (redirect "/new-circuit-connecting")
          (assoc-in [:flash :alerts] [{:class "success" :message "Circuit added successfully"}])))
      (-> (redirect "/new-circuit-connecting")
          (assoc-in [:flash :form_errors] (validators/get-errors cleaned-circuit))))))

(defn update-circuit [request id form]
  (-> (redirect (str "/edit-circuit/" id))))

(defroutes home-routes
  (GET "/" [] home-page)
  (GET "/new-circuit-connecting" [] new-circuit-connecting-page)
  (POST "/new-circuit-connecting" [] add-circuit)
  (GET "/edit-circuit/:id{[0-9]+}" [id :as r] (edit-circuit-page r id))
  (PUT "/edit-circuit/:id{[0-9]+}/:form{[a-z_]+}" [id form :as r] (update-circuit r id form))
  (GET "/connected-circuits" [] connected-circuits-page)
  (GET "/disconnected-circuits" [] disconnected-circuits-page))

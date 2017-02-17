(ns sltapp.routes.home
  (:require [sltapp.layout :refer [render base-context-authenticated-access]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.constants :refer [form_to_field_map auto_fill_fields]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.utils :as utils]
            [sltapp.templates.home :as home-templates]
            [clojure.string :as string]))

(defn home-page [request]
  (render (home-templates/home (base-context-authenticated-access request))))

(defn connected-circuits-page [request]
  (render (home-templates/connected-circuits (merge
                                              (base-context-authenticated-access request)
                                              {:table_headers (map utils/db-field-to-verbose-name (apply concat (vals form_to_field_map)))
                                               :circuit-list (db/get-connected-circuit-list)}))))

(defn disconnected-circuits-page [request]
  (render (home-templates/disconnected-circuits (merge
                                                  (base-context-authenticated-access request)
                                                  {:table_headers (map utils/db-field-to-verbose-name (apply concat (vals form_to_field_map)))
                                                   :circuit-list (db/get-disconnected-circuit-list)}))))

(defn new-circuit-connecting-page [request]
  (render (home-templates/new-circuit-connecting (merge
                                                  (base-context-authenticated-access request)
                                                  {:fields (remove (set (:new_circuit_connecting auto_fill_fields)) (:new_circuit_connecting form_to_field_map))}))))

(defn edit-circuit-page [request]
  (render (home-templates/edit-circuit (merge
                                        (base-context-authenticated-access request)
                                        {:info_fields (:new_circuit_connecting form_to_field_map)
                                         :disabled_fields (apply concat (vals auto_fill_fields))
                                         :form_to_fields (dissoc form_to_field_map :new_circuit_connecting)}))))

(defroutes home-routes
  (GET "/" [] home-page)
  (GET "/new-circuit-connecting" [] new-circuit-connecting-page)
  (GET "/edit-circuit" [] edit-circuit-page)
  (GET "/connected-circuits" [] connected-circuits-page)
  (GET "/disconnected-circuits" [] disconnected-circuits-page))

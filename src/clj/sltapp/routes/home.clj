(ns sltapp.routes.home
  (:require [sltapp.layout :refer [render base-context-authenticated-access error-page]]
            [compojure.core :refer [defroutes GET POST PUT]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [sltapp.constants :refer [form_to_field_map auto_fill_fields editable_new_circuit_fields_admin editable_new_circuit_fields_user]]
            [sltapp.db.core :as db]
            [sltapp.validators :as validators]
            [sltapp.utils :as utils]
            [sltapp.templates.home :as home-templates]
            [sltapp.service.auth :refer [unauthorized-handler]]
            [clojure.data.json :as json]
            [clojure-csv.core :as csv]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer :all]
            [clojure.string :as string]))

(defn home-page [request]
  (render (home-templates/home (base-context-authenticated-access request))))

(defn search-circuits [state q]
  (db/search-circuits {:state state
                       :where (json/read-str (if (utils/valid-search? q) (utils/format-search q) "{}") :key-fn keyword)}))

(defn connected-circuits-page [request q]
  (render (home-templates/connected-circuits (merge
                                              (base-context-authenticated-access request)
                                              {:table_headers (distinct (apply concat (vals (dissoc form_to_field_map :disconnecting))))
                                               :rows (map utils/format-values (search-circuits "connected" q))
                                               :search q}))))

(defn disconnected-circuits-page [request q]
  (render (home-templates/disconnected-circuits (merge
                                                  (base-context-authenticated-access request)
                                                  {:table_headers (distinct (apply concat (vals form_to_field_map)))
                                                   :rows (map utils/format-values (search-circuits "disconnected" q))
                                                   :search q}))))

(defn new-circuit-page [request]
  (if (some #{"new_circuit"} (utils/get-user-perms (:identity request)))
    (render (home-templates/new-circuit (merge
                                          (base-context-authenticated-access request)
                                          {:fields (remove (set (:new_circuit auto_fill_fields)) (:new_circuit form_to_field_map))
                                           :errors (-> request :flash :form_errors)})))
    (unauthorized-handler request "Only authenticated users allowed")))

(defn edit-circuit-page [request id]
  (if (nil? (db/circuit-in-state {:id id :state "connected"}))
    (error-page {:status 404 :title "Not found"})
    (render (home-templates/edit-circuit (merge
                                          (base-context-authenticated-access request)
                                          {:values (utils/format-values (db/get-circuit-info {:id id}))
                                           :errors (-> request :flash :form_errors)
                                           :info_fields (:new_circuit form_to_field_map)
                                           :editable_info_fields (if (-> request :identity :admin) editable_new_circuit_fields_admin editable_new_circuit_fields_user)
                                           :disabled_fields (apply concat (vals auto_fill_fields))
                                           :form_to_fields (select-keys (dissoc form_to_field_map :new_circuit) (map keyword (utils/get-user-perms (:identity request))))})))))

(defn add-circuit [request]
  (let [cleaned-circuit (validators/validate-new-circuit (:params request))]
    (if (utils/valid? cleaned-circuit)
      (let [circuit (last cleaned-circuit)
            unique (empty? (db/get-circuit-id-site-id circuit))]
        (if unique
          (db/insert-new-circuit (merge circuit {:commissioned_by_id (-> request :session :identity :id)
                                                 :commissioned_date (c/to-sql-date (-> request :params :commissioned_date))
                                                 :state "connected"})))
        (-> (redirect "/new-circuit")
          (assoc-in [:flash :alerts] [{:class (if unique "success" "danger") :message (if unique "Circuit added successfully" "Site Id already exists for the entered Circuit No")}])))
      (-> (redirect "/new-circuit")
          (assoc-in [:flash :form_errors] (utils/get-errors cleaned-circuit))))))

(defn update-circuit [request id form]
  (let [return-fn (partial utils/validate-form-redirect (str "/edit-circuit/" id))]
    (cond
      (= form "info_fields") (return-fn
                               #((if (-> request :identity :admin) validators/validate-new-circuit validators/validate-circuit-user-edit) (:params request))
                               #(db/clj-expr-generic-update {:id id :table "circuit" :updates (reduce (fn [update_map field]
                                                                                                        (merge update_map {(keyword field) (utils/format-value-for-db field (get (:params request) (keyword field)))}))
                                                                                                      {}
                                                                                                      (if (-> request :identity :admin) editable_new_circuit_fields_admin editable_new_circuit_fields_user))})
                               {:class "success" :message "Circuit details saved!"})
      (= form "bw_changing") (return-fn
                               #(validators/validate-bw-changing (:params request))
                               #(db/clj-expr-generic-update {:id id :table "circuit" :updates (utils/build-form-update-map :bw_changing (:params request) (-> request :identity :id))})
                               {:class "success" :message "Bw Changing details saved!"})
      (= form "vpls_changing") (return-fn
                                 #(validators/validate-vpls-changing (:params request))
                                 #(db/clj-expr-generic-update {:id id :table "circuit" :updates (utils/build-form-update-map :vpls_changing (:params request) (-> request :identity :id))})
                                 {:class "success" :message "Vpls Changing details saved!"})
      (= form "device_changing") (return-fn
                                   #(validators/validate-device-changing (:params request))
                                   #(db/clj-expr-generic-update {:id id :table "circuit" :updates (utils/build-form-update-map :device_changing (:params request) (-> request :identity :id))})
                                   {:class "success" :message "Device Changing details saved!"})
      (= form "disconnecting") (utils/validate-form-redirect
                                 "/disconnected-circuits"
                                 #(validators/validate-disconnecting (:params request))
                                 #(db/clj-expr-generic-update {:id id :table "circuit" :updates (utils/build-form-update-map :disconnecting (:params request) (-> request :identity :id) :initial_value {:state "disconnected"})})
                                 {:class "success" :message "Circuit is disconnected!"}
                                 (str "/edit-circuit/" id))
    :else
      (-> (redirect (str "/edit-circuit/" id))
          (assoc-in [:flash :alerts] [{:class "danger" :message "Invalid circuit edit type"}])))))

(defn app-settings-page [request]
  (render (home-templates/app-settings (merge
                                         (base-context-authenticated-access request)
                                         {:values (utils/get-or-create-app-settings)
                                          :errors (-> request :flash :form_errors)}))))

(defn update-app-settings [request]
  (utils/validate-form-redirect
    "/app-settings"
    #(validators/validate-app-settings (:params request))
    #(db/clj-expr-generic-update {:id 1 :table "app_settings" :updates {:form_dropdowns (-> request :params :form_dropdowns)}})
    {:class "success" :message "App settings updated successfully!"}))

(defn circuit-search [state q]
  (redirect (str "/" state "-circuits?q=" q)))

(defn export-csv [request state q]
  (let [table_headers (distinct (apply concat (vals (if (= state "connected") (dissoc form_to_field_map :disconnecting) form_to_field_map))))
        rows (map utils/format-values (search-circuits state q))]
    {:status  200
     :headers {"Content-Type" "text/csv"
               "Content-Disposition" (str "attachment;filename=" (-> (hash/sha256 (c/to-string (t/now))) (bytes->hex))  ".csv")}
     :body    (csv/write-csv (into [(map utils/db-field-to-verbose-name table_headers)]
                                   (for [row rows]
                                     (for [header table_headers]
                                       (str (get row (keyword header)))))))}))

(defn import-csv [request]
  (let [data (csv/parse-csv (clojure.java.io/reader (:tempfile (-> request :params :file))))]
    (db/generic-insert {:table "circuit"
                        :cols (into [] (first data))
                        :vals (map (fn [val_set] (map utils/format-value-for-db (into [] (first data)) val_set)) (into [] (drop 1 data)))})
    (-> (redirect "/import-data")
        (assoc-in [:flash :alerts] [{:class "success" :message "Data imported successfully!"}]))))

(defn import-data-page [request]
  (render (home-templates/import-data-page (base-context-authenticated-access request))))

(defroutes home-routes
  (GET "/" [] home-page)
  (GET "/import-data" [] import-data-page)
  (POST "/import-data" [] import-csv)
  (GET "/new-circuit" [] new-circuit-page)
  (POST "/new-circuit" [] add-circuit)
  (GET "/edit-circuit/:id{[0-9]+}" [id :as r] (edit-circuit-page r id))
  (PUT "/edit-circuit/:id{[0-9]+}/:form{[a-z_]+}" [id form :as r] (update-circuit r id form))
  (GET "/connected-circuits" [q :as r] (connected-circuits-page r q))
  (GET "/app-settings" [] app-settings-page)
  (GET "/disconnected-circuits" [q :as r] (disconnected-circuits-page r q))
  (GET "/circuit-search/:state{(connected|disconnected)}" [state q] (circuit-search state q))
  (GET "/export-csv/:state{(connected|disconnected)}" [state q :as r] (export-csv r state q))
  (PUT "/update-app-settings" [] update-app-settings))

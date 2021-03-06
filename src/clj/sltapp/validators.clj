(ns sltapp.validators
 (:require [bouncer.core :as b]
           [sltapp.utils :as utils]
           [clojure.data.json :as json]
           [clj-time.core :as t]
           [clj-time.format :as f]
           [buddy.hashers :as hashers]
           [bouncer.validators :as v])
 (:use [bouncer.validators :only [defvalidator]]))

(defn validate-user-login [user]
  (b/validate
    user
    :email v/email
    :password v/required))

(defn validate-user-register [user]
  (b/validate
    user
    :first_name v/required
    :last_name v/required
    :email [v/required v/email]
    :role v/required))

(defn validate-change-password-form [form]
  (b/validate
    form
    :current_password v/required
    :new_password v/required
    :confirm_new_password v/required))

(defn validate-change-password [form current_password]
  (and
    (hashers/check (:current_password form) current_password)
    (= (:new_password form) (:confirm_new_password form))))

(defn validate-new-circuit [circuit]
  (b/validate
    circuit
    :site_id v/required
    :site_name v/required
    :circuit_no v/required
    :type v/required
    :bandwidth [[#(not (nil? (re-find #"^\d+$" %))) :message "%s must be a positive number"] v/required]
    :qos_profile v/required
    :vpls_id v/required
    :connected_device v/required
    :status v/required
    :commissioned_date v/required
    :commissioned_under_project v/string))

(defn validate-circuit-user-edit [circuit]
  (b/validate
    circuit
    :status v/required))

(defn validate-bw-changing [circuit]
  (b/validate
    circuit
    :bandwidth_changed_date v/required
    :bandwidth_changed_reason v/required
    :bandwidth [[#(not (nil? (re-find #"^\d+$" %))) :message "%s must be a positive number"] v/required]))

(defn validate-vpls-changing [circuit]
  (b/validate
    circuit
    :vpls_changed_date v/required
    :vpls_changed_reason v/required
    :vpls_id v/required))

(defn validate-device-changing [circuit]
  (b/validate
    circuit
    :new_device_connected_date v/required
    :new_device_connected_reason v/required
    :connected_device v/required))

(defn validate-disconnecting [circuit]
  (b/validate
    circuit
    :disconnected_date v/required
    :disconnected_reason v/required))

(defn validate-app-settings [app_settings]
  (b/validate
    app_settings
    :form_dropdowns [v/required [#(utils/exception-occured? (partial json/read-str % :key-fn keyword)) :message "%s must be valid JSON"]]))

(defn valid-user-perms? [user]
  (if (= (:role user) "User")
    (not (empty? (:permissions user)))
    true))


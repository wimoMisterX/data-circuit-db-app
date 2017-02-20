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

(defn is-positive-number? [n]
  (and (number? n) (> n 0)))

(defn validate-new-circuit [circuit]
  (b/validate
    circuit
    :site_id v/required
    :site_name v/required
    :slt_ip_circuit_no v/required
    :type v/required
    :current_bandwidth_mpbs [[#(is-positive-number? (read-string %)) :message "%s must be a positive number"] v/required]
    :qos_profile v/required
    :current_vpls_id v/required
    :connected_device v/required
    :status v/required
    :commissioned_under_project v/string))

(defn validate-bw-changing [circuit]
  (b/validate
    circuit
    :bandwidth_change_reason v/required
    :current_bandwidth_mpbs v/required))

(defn validate-vpls-changing [circuit]
  (b/validate
    circuit
    :vpls_changed_reason v/required
    :current_vpls_id v/required))

(defn validate-device-changing [circuit]
  (b/validate
    circuit
    :new_device_connected_reason v/required
    :connected_device v/required))

(defn validate-disconnecting [circuit]
  (b/validate
    circuit
    :disconnected_reason v/required
    :comments v/required))

(defn validate-app-settings [app_settings]
  (b/validate
    app_settings
    :timezone [v/required [#(contains? (t/available-ids) %) :message "Invalid Timezone"]]
    :datetime_format [v/required [#(utils/exception-occured? (partial f/formatter-local %)) :message "%s must be a valid date time format"]]
    :form_dropdowns [v/required [#(utils/exception-occured? (partial json/read-str % :key-fn keyword)) :message "%s must be valid JSON"]]))

(ns sltapp.validators
 (:require [bouncer.core :as b]
           [buddy.hashers :as hashers]
           [bouncer.validators :as v]))

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

(defn valid? [validated]
  (= (get validated 0) nil))

(defn get-errors [validated]
  (-> validated last(get :bouncer.core/errors)))

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
    :slt_ip_circuit_no v/required
    :type v/required
    :current_bandwidth_mpbs [v/number v/positive v/required]
    :qos_profile v/required
    :current_vpls_id v/required
    :status v/required
    :commissioned_under_project v/string))

(defn validate-bw-changing [circuit]
  (b/validate
    circuit
    :bandwidth_change_reason v/required))

(defn validate-vpls-changing [circuit]
  (b/validate
    circuit
    :vpls_changed_reason v/required))

(defn validate-device-changing [circuit]
  (b/validate
    circuit
    :new_device_connected_reason v/required))

(defn validate-disconnecting [circuit]
  (b/validate
    circuit
    :disconnected_reason v/required
    :comments v/required))

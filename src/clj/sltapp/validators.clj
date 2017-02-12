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


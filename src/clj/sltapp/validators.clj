(ns sltapp.validators
 (:require [bouncer.core :as b]
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
    :password v/required))

(defn valid? [validated]
  (= (get validated 0) nil))

(defn get-errors [validated]
  (-> validated last(get :bouncer.core/errors)))


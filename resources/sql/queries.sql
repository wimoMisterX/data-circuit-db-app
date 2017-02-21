-- :name create-user :i!
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, email, password, admin, is_active)
VALUES (:first_name, :last_name, :email, :password, :admin, :is_active)

-- :name get-user :? :1
-- :doc retrieve a user
SELECT :i*:cols
FROM users
WHERE :i:id-field = :id-value

-- :name get-user-permissions :? :*
-- :doc get all non admins
SELECT codename FROM permissions
WHERE user_id = :user_id

-- :name get-user-list :? :*
-- :doc get all non admins
SELECT * FROM users
WHERE email <> :email AND admin = false

-- :name update-user :! :n
-- :doc update an existing user's
UPDATE users
SET :i:col = :value
WHERE :i:id-field = :id-value

-- :name get-circuit-info :? :1
-- :doc retrieve a circuit
SELECT *,
    CONCAT_WS(' ', u1.first_name, u1.last_name) as commissioned_by,
    CONCAT_WS(' ', u2.first_name, u2.last_name) as bandwidth_update_by,
    CONCAT_WS(' ', u3.first_name, u3.last_name) as vpls_changed_by,
    CONCAT_WS(' ', u4.first_name, u4.last_name) as new_device_connected_by,
    CONCAT_WS(' ', u5.first_name, u5.last_name) as disconnected_by
FROM circuit
LEFT JOIN users AS u1 ON circuit.commissioned_by_id = u1.id
LEFT JOIN users AS u2 ON circuit.bandwidth_update_by_id = u2.id
LEFT JOIN users AS u3 ON circuit.vpls_changed_by_id = u3.id
LEFT JOIN users AS u4 ON circuit.new_device_connected_by_id = u4.id
LEFT JOIN users AS u5 ON circuit.disconnected_by_id = u5.id
WHERE circuit.id = :id

-- :name get-circuit-id-site-id  :? :1
-- :doc creates a new circuit
SELECT *
FROM circuit
WHERE site_id = :site_id AND slt_ip_circuit_no = :slt_ip_circuit_no

-- :name insert-new-circuit ! :! :n
-- :doc creates a new circuit
INSERT INTO circuit
(site_id, site_name, slt_ip_circuit_no, type, current_bandwidth, qos_profile, current_vpls_id, status, commissioned_date, commissioned_by_id, commissioned_under_project, connected_device, state)
VALUES (:site_id, :site_name, :slt_ip_circuit_no, :type, :current_bandwidth, :qos_profile, :current_vpls_id, :status, :commissioned_date, :commissioned_by_id, :commissioned_under_project, :connected_device, :state)

-- :name clj-expr-generic-update :! :n
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
update :i:table set
/*~
(string/join ","
  (for [[field _] (:updates params)]
    (str (identifier-param-quote (name field) options)
      " = :v:updates." (name field))))
~*/
where id = :id

-- :name circuit-in-state :? :1
-- :doc check if circuit in state
SELECT id
FROM circuit
WHERE state = :state AND id = :id

-- :name insert-app-settings ! :! :n
-- :doc creates a new user record
INSERT INTO app_settings
(timezone, datetime_format, form_dropdowns)
VALUES (:timezone, :datetime_format, :form_dropdowns)

-- :name get-app-settings :? :1
-- :doc check if circuit in state
SELECT *
FROM app_settings
WHERE id = 1

-- :name search-circuits :? :*
-- :doc get all disconnected circuits
SELECT *,
    CONCAT_WS(' ', u1.first_name, u1.last_name) as commissioned_by,
    CONCAT_WS(' ', u2.first_name, u2.last_name) as bandwidth_update_by,
    CONCAT_WS(' ', u3.first_name, u3.last_name) as vpls_changed_by,
    CONCAT_WS(' ', u4.first_name, u4.last_name) as new_device_connected_by,
    CONCAT_WS(' ', u5.first_name, u5.last_name) as disconnected_by
FROM circuit
LEFT JOIN users AS u1 ON circuit.commissioned_by_id = u1.id
LEFT JOIN users AS u2 ON circuit.bandwidth_update_by_id = u2.id
LEFT JOIN users AS u3 ON circuit.vpls_changed_by_id = u3.id
LEFT JOIN users AS u4 ON circuit.new_device_connected_by_id = u4.id
LEFT JOIN users AS u5 ON circuit.disconnected_by_id = u5.id
WHERE state = :state
/*~
(let [q (string/join " AND " (for [[field value] (:where params)]
                               (str (name field) " LIKE '%" value "%'")))]
  (if (string/blank? q) " AND 1=1" (str " AND " q)))
~*/
ORDER BY slt_ip_circuit_no

-- :name insert-user-perms ! :! :n
-- :doc creates a new user record
INSERT INTO permissions
(user_id, codename)
VALUES (:user_id, :codename)

-- :name delete-user-perms :! :n
DELETE FROM permissions
WHERE user_id = :user_id AND codename = :codename

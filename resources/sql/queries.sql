-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, email, password, admin, is_active)
VALUES (:first_name, :last_name, :email, :password, :admin, :is_active)

-- :name get-user :? :1
-- :doc retrieve a user
SELECT :i*:cols
FROM users
WHERE :i:id-field = :id-value

-- :name get-user-list :? :*
-- :doc get all non admins
SELECT * FROM users
WHERE email <> :email AND admin = false

-- :name update-user :! :n
-- :doc update an existing user's
UPDATE users
SET :i:col = :value
WHERE :i:id-field = :id-value

-- :name get-connected-circuit-list :? :*
-- :doc get all connected circuits
SELECT *,
    CONCAT_WS(' ', u1.first_name, u1.last_name) as commissioned_by,
    CONCAT_WS(' ', u2.first_name, u2.last_name) as bandwidth_update_by,
    CONCAT_WS(' ', u3.first_name, u3.last_name) as vpls_changed_by,
    CONCAT_WS(' ', u4.first_name, u4.last_name) as new_device_connected_by
FROM circuit
LEFT JOIN users AS u1 ON circuit.commissioned_by_id = u1.id
LEFT JOIN users AS u2 ON circuit.bandwidth_update_by_id = u2.id
LEFT JOIN users AS u3 ON circuit.vpls_changed_by_id = u3.id
LEFT JOIN users AS u4 ON circuit.new_device_connected_by_id = u4.id
WHERE state = 'connected'

-- :name get-disconnected-circuit-list :? :*
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
WHERE state = 'disconnected'

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

-- :name insert-new-circuit ! :! :n
-- :doc creates a new circuit
INSERT INTO circuit
(site_id, site_name, slt_ip_circuit_no, type, current_bandwidth_mpbs, qos_profile, current_vpls_id, status, commissioned_date, commissioned_by_id, commissioned_under_project, state)
VALUES (:site_id, :site_name, :slt_ip_circuit_no, :type, :current_bandwidth_mpbs, :qos_profile, :current_vpls_id, :status, :commissioned_date, :commissioned_by_id, :commissioned_under_project, :state)


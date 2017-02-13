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

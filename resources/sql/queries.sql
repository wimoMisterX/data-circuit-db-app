-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, email, password, admin, is_active)
VALUES (:first_name, :last_name, :email, :password, :admin, :is_active)

-- :name get-user-by-id :? :1
-- :doc retrieve a user given the id.
SELECT id, first_name, last_name, email, password, is_active, admin
FROM users
WHERE id = :id

-- :name get-user-by-email :? :1
-- :doc retrieve a user given the email
SELECT id, first_name, last_name, email, password, is_active, admin
FROM users
WHERE email = :email

-- :name get-user-list :? :*
-- :doc get all non admins
SELECT * FROM users
WHERE email <> :email AND admin = false

-- :name update-user :! :n
-- :doc update an existing user's
UPDATE users
SET :i:col = :value
WHERE :i:id-field = :i:id-value

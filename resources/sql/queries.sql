-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, email, password, admin, is_active)
VALUES (:first_name, :last_name, :email, :password, :admin, :is_active)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT first_name, last_name, email, password, is_active, admin FROM users
WHERE email = :email

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id

-- :name get-users :? :*
-- :doc selects all available users
SELECT * FROM users

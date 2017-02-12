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

-- :name update-user-password-email! :! :n
-- :doc update an existing user's password
UPDATE users
SET password = :password
WHERE email = :email

-- :name update-user-password-id! :! :n
-- :doc update an existing user's password
UPDATE users
SET password = :password
WHERE id = :id

-- :name get-user-list :? :*
-- :doc selects all available users execpt for current user
SELECT * FROM users
WHERE email <> :email

-- :name get-user-by-id :? :1
-- :doc retrieve a user given the id.
SELECT email FROM users
WHERE id = :id


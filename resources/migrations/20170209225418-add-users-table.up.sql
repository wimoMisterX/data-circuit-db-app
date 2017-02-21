CREATE TABLE users
(id SERIAL PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIMESTAMP,
 is_active BOOLEAN,
 registered_on TIMESTAMP,
 password VARCHAR(300));

CREATE TABLE permissions
(id SERIAL PRIMARY KEY,
 user_id NUMERIC references users(id),
 codename VARCHAR(30));

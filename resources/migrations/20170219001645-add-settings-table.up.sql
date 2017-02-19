CREATE TABLE app_settings
(id SERIAL PRIMARY KEY,
 timezone VARCHAR(30),
 form_dropdowns NVARCHAR2(4000),
 datetime_format VARCHAR(30));

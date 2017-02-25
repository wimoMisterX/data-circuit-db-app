CREATE TABLE circuit(
    id SERIAL PRIMARY KEY,
    site_id VARCHAR(30) NOT NULL,
    site_name VARCHAR(30),
    circuit_no VARCHAR(30) NOT NULL,
    type VARCHAR(30),
    bandwidth NUMERIC,
    qos_profile VARCHAR(30),
    vpls_id VARCHAR(30),
    status VARCHAR(30),
    connected_device VARCHAR(30),
    commissioned_date TIMESTAMP,
    commissioned_by_id NUMERIC references users(id),
    commissioned_under_project VARCHAR(30),
    bandwidth_changed_date TIMESTAMP,
    bandwidth_update_by_id NUMERIC references users(id),
    bandwidth_changed_reason VARCHAR(300),
    vpls_changed_date TIMESTAMP,
    vpls_changed_by_id NUMERIC references users(id),
    vpls_changed_reason VARCHAR(300),
    new_device_connected_date TIMESTAMP,
    new_device_connected_by_id NUMERIC references users(id),
    new_device_connected_reason VARCHAR(300),
    disconnected_date TIMESTAMP,
    disconnected_by_id NUMERIC references users(id),
    disconnected_reason VARCHAR(300),
    comments VARCHAR(300),
    state VARCHAR(30),
);

(ns sltapp.constants)

(def form_to_field_map {:new_circuit ["circuit_no"
                                      "site_id"
                                      "site_name"
                                      "type"
                                      "bandwidth"
                                      "qos_profile"
                                      "vpls_id"
                                      "connected_device"
                                      "status"
                                      "commissioned_date"
                                      "commissioned_by"
                                      "commissioned_under_project"]
                        :bw_changing ["bandwidth_changed_date"
                                      "bandwidth_changed_by"
                                      "bandwidth"
                                      "bandwidth_changed_reason"]
                        :vpls_changing ["vpls_changed_date"
                                        "vpls_changed_by"
                                        "vpls_id"
                                        "vpls_changed_reason"]
                        :device_changing ["new_device_connected_date"
                                          "new_device_connected_by"
                                          "connected_device"
                                          "new_device_connected_reason"]
                        :disconnecting ["disconnected_date"
                                        "disconnected_by"
                                        "disconnected_reason"
                                        "comments"]})

(def auto_fill_fields {:new_circuit ["commissioned_by"]
                       :bw_changing ["bandwidth_changed_by"]
                       :vpls_changing ["vpls_changed_by"]
                       :device_changing ["new_device_connected_by"]
                       :disconnecting ["disconnected_by"]})

(def permissions ["new_circuit"
                  "bw_changing"
                  "vpls_changing"
                  "device_changing"
                  "disconnecting"])

(def editable_new_circuit_fields_user ["status"])

(def editable_new_circuit_fields_admin ["circuit_no"
                                        "site_id"
                                        "site_name"
                                        "type"
                                        "bandwidth"
                                        "qos_profile"
                                        "vpls_id"
                                        "connected_device"
                                        "status"
                                        "commissioned_date"
                                        "commissioned_under_project"])

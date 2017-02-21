(ns sltapp.constants)

(def form_to_field_map {:new_circuit_connecting ["site_id"
                                                 "site_name"
                                                 "slt_ip_circuit_no"
                                                 "type"
                                                 "current_bandwidth_mpbs"
                                                 "qos_profile"
                                                 "current_vpls_id"
                                                 "connected_device"
                                                 "status"
                                                 "commissioned_date"
                                                 "commissioned_by"
                                                 "commissioned_under_project"]
                        :bw_changing ["bandwidth_change_date"
                                      "bandwidth_update_by"
                                      "current_bandwidth_mpbs"
                                      "bandwidth_change_reason"]
                        :vpls_changing ["vpls_changed_date"
                                        "vpls_changed_by"
                                        "current_vpls_id"
                                        "vpls_changed_reason"]
                        :device_changing ["new_device_connected_date"
                                          "new_device_connected_by"
                                          "connected_device"
                                          "new_device_connected_reason"]
                        :disconnecting ["disconnected_date"
                                        "disconnected_by"
                                        "disconnected_reason"
                                        "comments"]})

(def auto_fill_fields {:new_circuit_connecting ["commissioned_date"
                                               "commissioned_by"]
                       :bw_changing ["bandwidth_change_date"
                                    "bandwidth_update_by"]
                       :vpls_changing ["vpls_changed_date"
                                       "vpls_changed_by"]
                       :device_changing ["new_device_connected_date"
                                         "new_device_connected_by"]
                       :disconnecting ["disconnected_date"
                                       "disconnected_by"]})

(def permissions ["new_circuit_connecting"
                  "bw_changing"
                  "vpls_changing"
                  "device_changing"
                  "disconnecting"])

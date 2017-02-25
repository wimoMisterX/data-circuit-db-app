(ns sltapp.config
  (:import [org.h2.tools Server])
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [mount.core :refer [args defstate]]))

(defstate env :start (load-config
                       :merge
                       [(args)
                        (source/from-system-props)
                        (source/from-env)]))

(defstate dbserver :start (.start (Server/createTcpServer (into-array String [])))
                   :stop (.stop dbserver))

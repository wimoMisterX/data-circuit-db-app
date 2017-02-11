(ns user
  (:require [mount.core :as mount]
            sltapp.core))

(defn start []
  (mount/start-without #'sltapp.core/http-server
                       #'sltapp.core/repl-server))

(defn stop []
  (mount/stop-except #'sltapp.core/http-server
                     #'sltapp.core/repl-server))

(defn restart []
  (stop)
  (start))



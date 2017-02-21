(ns sltapp.core
  (:require [sltapp.handler :as handler]
            [sltapp.service.auth :as auth]
            [sltapp.validators :as validators]
            [sltapp.utils :as utils]
            [sltapp.utils :refer [contains-many? get-or-create-app-settings]]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            [luminus-migrations.core :as migrations]
            [sltapp.config :refer [env]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [sltapp.db.core :as db]
            [mount.core :as mount])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(def createadmin-cli-options
  [["-f" "--first_name FIRSTNAME" "First Name"]
   ["-l" "--last_name LASTNAME" "Last Name"]
   ["-e" "--email EMAIL" "Email Address"]
   ["-p" "--password PASSWORD" "Password"]])

(mount/defstate ^{:on-reload :noop}
                http-server
                :start
                (http/start
                  (-> env
                      (assoc :handler (handler/app))
                      (update :port #(or (-> env :options :port) %))))
                :stop
                (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
                repl-server
                :start
                (when-let [nrepl-port (env :nrepl-port)]
                  (repl/start {:port nrepl-port}))
                :stop
                (when repl-server
                  (repl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (get-or-create-app-settings)
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (cond
    (some #{"migrate" "rollback"} args)
    (do
      (mount/start #'sltapp.config/env)
      (migrations/migrate args (select-keys env [:database-url]))
      (System/exit 0))
    (some #{"createadmin"} args)
    (do
      (let [parsed-args (-> args (parse-opts createadmin-cli-options))]
        (if (and
              (contains-many? (:options parsed-args) :first_name :last_name :email :password)
              (utils/valid? (validators/validate-user-register (merge
                                                                      (:options parsed-args)
                                                                      {:role "Admin"}))))
          (do
            (mount/start #'sltapp.config/env #'sltapp.db.core/*db*)
            (db/create-user (merge
                              (assoc-in (:options parsed-args) [:password] (auth/encrypt-password (-> parsed-args :options :password)))
                              {:admin true
                               :is_active true}))
            (mount/stop #'sltapp.db.core/*db*)
            (println "User created successfully"))
          (println (or (:errors parsed-args) (:summary parsed-args)))))
      (System/exit 0))
    :else
    (start-app args)))


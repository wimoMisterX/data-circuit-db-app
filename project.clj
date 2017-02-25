(defproject sltapp (get (System/getenv) "PROJECT_VERSION")

  :description "Data Circuit Database Application"
  :url "https://github.com/wimoMisterX/data-circuit-db-app"

  :dependencies [[bouncer "1.0.0"]
                 [com.h2database/h2 "1.4.193"]
                 [compojure "1.5.2"]
                 [conman "0.6.3"]
                 [cprop "0.1.10"]
                 [luminus-immutant "0.2.3"]
                 [luminus-migrations "0.2.9"]
                 [luminus-nrepl "0.1.4"]
                 [markdown-clj "0.9.94"]
                 [metosin/ring-http-response "0.8.1"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.webjars/bootstrap "3.3.7"]
                 [org.webjars/font-awesome "4.7.0"]
                 [org.webjars/jquery "3.1.1"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [org.webjars.npm/eonasdan-bootstrap-datetimepicker "4.17.42"]
                 [ring-middleware-format "0.7.2"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-defaults "0.2.2"]
                 [hiccup "1.0.5"]
                 [buddy/buddy-auth "1.4.1"]
                 [clj-time "0.13.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [buddy/buddy-hashers "1.2.0"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main sltapp.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.4.3"]
            [lein-immutant "2.1.0"]]

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "sltapp.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.4"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.5.1"]
                                 [selmer "1.10.6"]
                                 [pjstadig/humane-test-output "0.8.1"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.18.1"]]

                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})

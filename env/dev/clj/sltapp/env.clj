(ns sltapp.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [sltapp.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[sltapp started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[sltapp has shut down successfully]=-"))
   :middleware wrap-dev})

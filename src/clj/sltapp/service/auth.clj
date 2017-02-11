(ns sltapp.service.auth
  (:require [sltapp.layout :refer [*app-context* error-page]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.accessrules :refer [error]]
            [buddy.hashers :as hashers]
            [ring.util.response :refer [redirect]]))

(defn unauthorized-handler [request metadata]
  (cond
    (authenticated? request)
    (error-page {:status 403
                 :title "Permission denied"
                 :message (or metadata "You dont have access!")})
    :else
    (redirect (format "/login?next=%s" (:uri request)))))

(defn authenticated-user [request]
  (if (:identity request)
    true
    (error "Only authenticated users allowed")))

(defn on-error [request value]
  (unauthorized-handler request value))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(def rules [{:pattern #"^/$"
             :handler authenticated-user}])

; Auth Helpers

(defn encrypt-password [password]
  (hashers/encrypt password {:alg :pbkdf2+sha256
                             :salt "wimo"}))


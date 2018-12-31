(ns demo.index
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [lilrouter.settings :refer [settings]]
            [lilrouter.router :as router
                              :refer (->Router)]))

(use 'demo.routes)
(use 'ring.adapter.jetty)

(defn my-logger [msg]
  (do
    (log/info (str "Routes cached: "
                (count @router/cache)))
    (log/info (with-out-str (pprint msg)))))

(router/set-logger my-logger)

(defn handler [request]
  (let [router-instance (->Router (atom {}))]
    (router/handle-req request)))

(defn -main [& args]
  (run-jetty handler {:port 9001}))
(ns demo.index
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [lilrouter.router :as router]))

(use 'demo.routes)
(use 'ring.adapter.jetty)

(defn my-logger [msg]
  (do
    (log/info (str "Routes cached: "
                (count @router/cache)))
    (log/info (with-out-str (pprint msg)))))

(swap! router/settings assoc
  :logger my-logger)

(defn handler [request]
  (router/handle-req request))

(defn -main
  [& args]
  (run-jetty handler {:port 9001}))
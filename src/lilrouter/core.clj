(ns lilrouter.core
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [lilrouter.router :as router]))

(use 'lilrouter.repl-helpers)
(use 'demo.routes)

(use 'ring.adapter.jetty)

(defn- log [req]
  (with-out-str (pprint req)))

(defn -main [&args]
  (prn "hello world"))


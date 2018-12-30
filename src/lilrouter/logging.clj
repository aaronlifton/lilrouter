(ns lilrouter.logging
  (:require [clojure.tools.logging :as clojure-log]
            [lilrouter.settings :refer [settings]]))

; logging

(defmacro apply-log-info [& args]
  `(clojure-log/info ~@args))

(defn default-logger [& args]
  (case {:env @settings}
    "dev" (apply-log-info args)
    "test" identity))

(defn- resolve-logger [logger]
  "Tries to resolve a logger fn from
  a var or symbol. Otherwise logger
  must implement Fn."
  (cond
    (var? logger) (var-get logger)
    (symbol? logger) (resolve logger)
    :else (if (fn? logger) logger nil)))

(defn get-logger []
  (let [logger (resolve-logger (:logger @settings))]
    (if logger
      logger
      (do
        (clojure-log/warn (str "Cannot find logger.\n"
          "Current setting: " (:logger @settings)))
        identity))))

; /logging
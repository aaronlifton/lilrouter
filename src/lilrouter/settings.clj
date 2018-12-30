(ns lilrouter.settings)

(def default-settings
  {:env "dev"
   :cache-limit 200
   :logger 'lilrouter.logging/default-logger
   :log {:request 'default-log-request}})

(def settings
  "Router settings.
  Logger should be a symbol of a function path
  that can be resolved to a var, a var itself,
  or a function."
  (atom default-settings))

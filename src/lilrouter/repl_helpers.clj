(ns lilrouter.repl-helpers)

(defn reload-router []
  "Reloads router source.
  Useful for working on router"
  (do
    (require 'lilrouter.router)
    (use 'lilrouter.router :reload)
    (use 'demo.routes :reload)))

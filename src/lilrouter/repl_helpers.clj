(ns lilrouter.repl-helpers)
; for working on router

(defn reload-router []
  (do
    (require 'lilrouter.router)
    (use 'lilrouter.router :reload)
    (use 'demo.routes :reload)))

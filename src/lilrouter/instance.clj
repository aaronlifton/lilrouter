(ns lilrouter.instance)

(defprotocol IRouter
  (set-state [_ k v])
  (handle-req [_ req])
  (routes [_]))

; (defn handle-req-impl [req req-handler & [optroutes]]
;   (req-handler req state optroutes))

(defrecord Router [req-handler state]
  IRouter
  (set-state [t k v] (swap! (:state t) assoc k v))
  (handle-req [t req]
    (req-handler req))
  (routes [_] (:routes state)))


; (defprotocol Meow
;   (meow [_ t]))
; (defrecord Cat [name]
;   Meow
;   (meow [_ t] (str name " says meow with type " t)))

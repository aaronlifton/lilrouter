(ns demo.routes
  (:require [lilrouter.router :as router]))

(defn index [req state]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello world"})

(defn about [req state]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Welcome to about page"})


(defn show-post [req state]
  (prn state)
  (let [params (:path-params state)]
    {:status 200
      :headers {"Content-Type" "text/html"}
      :body (str "Params: " params "<br>"
              "You're looking at post #" (:id params) "<br>"
              "Querystring: " (:query-params state))}))

(defn my404 [req state]
  {:status 404
    :headers {"Content-Type" "text/html"}
    :body "custom 404"})

(router/set-routes {
  "/" index
  "/about" about
  "/posts/:id" show-post
  "404" my404
  })
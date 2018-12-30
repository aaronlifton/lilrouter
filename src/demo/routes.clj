(ns demo.routes
  (:require [lilrouter.router :as router]))

(defn index [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello world"})

(defn about [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Welcome to about page"})


(defn show-post [req]
  (let [params (:match-params @router/state)]
    {:status 200
      :headers {"Content-Type" "text/html"}
      :body (str "Params: " params "<br>"
              "You're looking at post #" (:id params) "<br>"
              "Querystring: " (:query-params @router/state))}))

(router/set-routes {
  "/" index
  "/about" about
  "/posts/:id" show-post
  })
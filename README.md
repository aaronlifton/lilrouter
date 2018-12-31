# lilrouter

A simple clojure router

## Setup
1. Package coming soon

## Usage

### Defining routes

See `src/demo/routes`.

```clojure
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
    :body "4̶͈̫̘͖̲̳̌̈̑0̸͉̠̘͓͎͇́͐̈͗͆̑͘͝͝4̵͓͔̀̈̀͒̽̍̓͑̌͘"})

(router/set-routes {
  "/" index
  "/about" about
  "/posts/:id" show-post
  "404" my404
  })

; To run: lein ring server 9001
```

### Logging

- You can plug in your own logger

```clojure
(require '[lilrouter.router :as router])

(defn my-logger [msg]
  "Logger which will say hello world
  before every message."
  (pprint (str "hello world" msg)))

(router/set-logger mylogger)
```

### Environment support

```clojure
(require '[lilrouter.router :as router])

(router/set-env "dev")
```
- Logging is disabled in the `test` environment.


## Query strings
- Complex query strings are supported
  - such as `x=a/&a[b]=2&a[c]=3.3&a[b][d]=%275%27`
- Deeply nested arrays are supported
- Param values can be coerced into
  - String
  - Integer
  - Double
  - IPersistentMap

## Route params
- Route params are always read as strings
- You can define your route as having a named route param
by inserting a keyword in it:
  - "/posts/:id"

## Dev setup
1. Run dev server
  - `lein ring server 9001`
2. See testing section

## Testing
- You can run tests with leiningin by running `lein test`
- You can run tests in the REPL using `lein with-profile +test repl`
- To run all tests, run `run-all-tests`
- To run router tests, run `run-router-tests`

## Demo
- You can run the demo by running `lein ring server 9001`
- You can view the demo code in `src/demo`

## TODO
- add support for nested routes
- change data structure routes are stored in
- implement test-ns-hook to clear router state before every test

## License
MIT License

Copyright © 2018 - 2019 Aaron Lifton

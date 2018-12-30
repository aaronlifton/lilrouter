# lilrouter

A simple clojure router

## Setup
1. Package coming soon

## Usage

### Defining routes

See `src/demo/routes`.

```
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

; To run: lein ring server 9001
```

### Logging

- You can plug in your own logger

```
(defn my-logger [msg]
  "Logger which will say hello world
  before every message."
  (pprint (str "hello world" msg)))

(swap! router/settings assoc :logger my-logger)
```

### Environment support

```

(swap! router/settings assoc :env "dev")
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
You can run tests with leiningin by running `lein test`
You can run tests in the REPL using `lein with-profile +test repl`
To run all tests, run `run-all-tests`
To run router tests, run `run-router-tests`

## Demo
- You can run the demo by running `lein ring server 9001`
- You can view the demo code in `src/demo`

## TODO
- add support for nested routes
- change data structure routes are stored in

## License
MIT License

Copyright © 2018 - 2019 Aaron Lifton

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

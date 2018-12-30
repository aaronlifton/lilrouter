(ns lilrouter.test.router-test
  (:require [lilrouter.test :as t]
            [lilrouter.router :as router]
            [lilrouter.test.mock :as mock]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [demo.routes :as demo-routes]))

; using demo data as fixtures
; (load "/demo/routes")
(use 'demo.routes)


; how does one run a single test?
; (defn test-ns-hook []
;   (do
;     (print 1)
;     (print @t/test-holder)
;     (map (fn [x] ((var-get x)) @t/test-holder))))

(defmacro create-test-route [path expected]
  (let [test-name (str "does the route " path " work?")]
    `(deftest ~(gensym "test-route")
      (testing ~test-name
        (is (= (#'router/match-handler ~path) ~expected))))))


(defmacro defroutetest [path expected]
  (let [res `(create-test-route ~path ~expected)]
    `(do
      (let [evald-res# ~res]
        (swap! t/test-holder conj evald-res#)
        evald-res#))))

(defn reload []
  (require '[clojure.test :refer :all])
  (require 'lilrouter.router-test :reload)
  (in-ns 'lilrouter.router-test))

; tests

(deftest handler-must-be-fn []
  (testing "AssertionError is thrown when handler is not a function"
    (let [routes {"/" 1}]
          (is (thrown? AssertionError (router/set-routes routes))))))

(defroutetest "?" nil)
(defroutetest "/?" nil)
(defroutetest "/?/" nil)

(defroutetest "/" demo-routes/index)
(defroutetest "/about" demo-routes/about)
(defroutetest "/posts/1" demo-routes/show-post)

(deftest render-index-page
  (testing "index page should render hello world"
    (let [res (demo-routes/index mock/mock-request)]
      (str/includes? (:body res) "Hello world"))))

(deftest coercer
  (testing "do numbers get coerced correctly"
    (is (= 1 (router/coerce-param "1"))))
  (testing "do floats get coerced correctly"
    (is (= 1.0 (router/coerce-param "1.0")))
    (is (= "1." (router/coerce-param "1.")))
    (is (= 0.1 (router/coerce-param "0.1")))
    (is (= 0.1 (router/coerce-param ".1"))))
  (testing "do embedded strings get coerced correctly"
    (is (= "1" (router/coerce-param "'1'")))
    (is (= "1" (router/coerce-param "\"1\""))))
  (testing "do bools get coerced correctly"
    (is (= true (router/coerce-param "true")))
    (is (= false (router/coerce-param "false")))))

(deftest parse-query-string1
  (testing "query strings should get parsed correctly"
    (let [res (router/parse-query-string "x=1&a=test%2F")]
      (is (= res {:x 1 :a "test/"})))))

(deftest parse-query-string2
  (testing "query strings with objects should get parsed correctly"
    (let [res (router/parse-query-string
                "a[one]=1&a[two]=2.0&b=%273%27&d=%221%22")]
      (is (= res {:a {:one 1 :two 2.0} :b "3" :d "1"})))))

(deftest parse-deep-object-query-string
  (testing "query strings with deeply nested objects"
    (let [res (router/parse-query-string
                "&a[two][foo]=1&a[one][bar]=2&c=3")]
      (is (= res {:a {:two {:foo 1} :one {:bar 2}} :c 3})))))


(deftest path-params
  (testing "path params in route settings should get parsed correctly"
    (let [res (#'router/parse-path-params "/posts/:id")]
      (is (= res (seq '(:id)))))))

(deftest generated-path-regex
  (testing "is correct regex generated for a path"
    (let [res (#'router/gen-regex-for-route "/posts/:id")]
      (is (= "\\/posts\\/(\\w+)" (str res))))))

(deftest matches-path-to-route
  (testing "should match path based on generated regex"
    (do
      (let [routepath "/posts/:id"
            regex (#'router/gen-regex-for-route routepath)
            routes {"/posts/:id" {:path "/posts/:id" :pattern regex}}
            path "/posts/1"
            res (#'router/match-route path routes)]
        (is (= routepath (:path res)))))))

(defmacro defpathmatchtest [routepath reqpath]
  (let [test-name (str "path " reqpath
          " should be matched with route " routepath)
        handler (fn [req] true)]
    `(deftest ~(gensym "matches-route-with-regex")
      (testing ~test-name
        (let [routes# (#'router/map-with-info {~routepath ~handler})
              route# (#'router/match-route ~reqpath routes#)
              match# (:path route#)]
          (is (= match#
               ~routepath)))))))

(defpathmatchtest "/posts/:id" "/posts/1")
(defpathmatchtest "/posts/:id/comments/:id" "/posts/1/comments/2")
(defpathmatchtest "/posts" "/posts")

(deftest matches-route-with-regex
  (testing "matches the correct route with the associated
    generated regex"
    (let [routes (#'router/map-with-info {"/posts/:id" show-post})
          path "/posts/2"]
          (is (= (#'router/match-handler path routes)
                 show-post)))))

(deftest route-not-cached
  (testing "routes not found should not be cached"
    (do
      (swap! router/cache empty)
      (router/handle-req {:uri "/foobar" :query-string nil})
      (is (empty? @router/cache)))))

(deftest route-cached
  (testing "routes that have handlers should be cached"
    (do
      (swap! router/cache empty)
      (router/handle-req {:uri "/about" :query-string nil})
      (is (= true (contains? @router/cache "/about")))
      (is (= (-> @router/cache
                vals first (get ,,, :handler))
             demo-routes/about)))))

(deftest custom-logger
  (testing "custom logger should work"
    (letfn [(mylogger [x] "foobar")]
      (do
        (swap! router/settings assoc :logger mylogger)
        (is (= (router/log "hello") "foobar"))))))

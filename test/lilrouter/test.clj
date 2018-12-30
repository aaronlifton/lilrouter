(ns lilrouter.test
  (:require [clojure.test :refer :all]
            [lilrouter.test.mock :as mock]
            [lilrouter.router :refer [settings]]))

(defonce test-holder (atom []))

(swap! settings assoc :env "test")

(defn load-tests []
  (do
    (swap! test-holder empty)
    (require '[lilrouter.router :as router] :reload)
    (use 'lilrouter.test.router-test :reload)))

(defn run-router-tests []
  (do
    (require 'lilrouter.test.router-test :reload)
    (run-tests 'lilrouter.test.router-test)))

(defn run-proj-tests []
  (do (load-tests)
      (run-router-tests)))
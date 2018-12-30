(ns lilrouter.router
  (:require [clojure.string :as str]
            [lilrouter.logging :refer [get-logger]]
            [lilrouter.settings :refer [settings]]
            [lilrouter.util :as util]))

(defonce state (atom {}))

(defonce cache (atom {}))

; logging

(defn default-log-request [logger]
  "Logger is passed in as arg"
  (logger (:current-path @state)))

(defn log [msg]
  ((get-logger) msg))

; regexs
(def word-regex #"^(\w+)$")

(def string-regex (re-pattern "['\"](.*)['\"]"))

(def float-regex #"[+-]?([0-9]*[.])[0-9]+")

(def num-regex #"^(\d+)+$")

(def bool-regex #"\btrue\b|\bfalse\b")

(def obj-param-regex #"(\w+)((\[\w+\])+)")

; this should be faster than regex
(defn get-param-map-keys [array-str]
  "This turns str `[a][b][c]` into seq `(a b c)`.
  It assumes a, b, and c are valid regex words."
  (let [pos-pairs (map vector
              (util/find-indices #(= % \[) array-str)
              (util/find-indices #(= % \]) array-str))]
    (map (fn [[a b]] (subs array-str (inc a) b))
         pos-pairs)))

; param parser
(defn coerce-param [v]
  "Coerces param into a bool, num, double, or,
  as a default, a string"
  (let [found-embedded-str (re-matches string-regex v)]
    (cond
      found-embedded-str (second found-embedded-str)
      (re-matches bool-regex v) (if (= v "true") true false)
      (re-matches num-regex v) (Integer/parseInt v)
      (re-matches float-regex v) (Double/parseDouble v)
      :else v)))

(defn- decode-uri-component [uri]
   #?(:clj (java.net.URLDecoder/decode uri)
      :cljs (js/decodeURIComponent uri)))

(defn parse-query-param [p]
  "Decodes URI component, and then
  coerces the value into clojure
  type."
  (-> p decode-uri-component
        coerce-param))

(defn coerce-map [param val]
  "Turns `{:param a[b][c][d] :val 1}`
  into `{:a {:b {:c {:d 1}}}}`"
  (let [matches (re-matches obj-param-regex param)]
    (if-let [res (nth matches 2)]
      (let [topkey (second matches)
            keys (map keyword
                  (cons topkey
                    (get-param-map-keys res)))]
        (reduce (fn [a [i k]]
          (assoc-in {} (take i keys)
            (if (= (inc i) (count keys))
              {k (parse-query-param val)}
              k)))
          {} (map-indexed vector keys))))))

(defn parse-query-string [query-string]
  "Parses a query string into a map.
   Coerces values, and handles nested
   arrays."
  (if (and query-string (> (count query-string) 0))
    (->> (str/split query-string #"&")
         (remove #(= (count %) 0))
         (map #(str/split % #"="))
         (reduce (fn [a [k v]]
           (cond
             (re-matches obj-param-regex k)
               (util/deep-merge a (coerce-map k v))
             :else
               (assoc a (keyword k)
                 (parse-query-param v))))
           {})
         (into {}))))

; /param parser

; default routes
(defn default404 [request]
  "default 404 route"
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body "404"})

(defn handle404 [request]
  "404 handler. If you set a custom 404,
  it uses that. Otherwise uses the default 404 route.
  See `default404`"
  (if-let [route (get (:routes @state) "404")]
    (if-let [handler (:handler route)]
      (handler request)
      (default404 request))
    (default404 request)))

; path parser/matcher
(defn- is-route-param [part]
  (= (get part 0) \:))

(defn- parse-path-params [path]
  (->> (str/split path #"/")
       (filter (fn [p] (is-route-param p)))
       (map (fn [p] (keyword (subs p 1))))))

(defn- get-path-param-mapping [route-info matches]
  (let [params (:path-params route-info)]
    (->> (map vector params matches)
         (into {}))))

(defn- match-route [path routes]
  "Find the route whose regex matches the
  given path"
  (if-let [match (first (filter (fn [[p r]]
            (when (contains? r :pattern)
              (if-let [matches (re-matches (:pattern r) path)]
                (swap! state assoc :match-params
                  (get-path-param-mapping r (rest matches))))))
            routes))]
        (val match)))

(defn- cache-route [path route]
  (do
    (when (>= (count @cache)
            (:cache-limit @settings))
      (swap! cache empty))
    (swap! cache assoc path route)))

(defn match-handler [path & [routes]]
  (let [routes (or routes (:routes @state))
        cached-route (get @cache path)]
    (if cached-route
      (:handler cached-route))
      (if-let [route (match-route path routes)]
        (do
          (cache-route path route)
          (:handler route)))))

(defn- gen-regex-for-route [route]
  "Replaces parts of the path denoted
  as path params with a word regex pattern"
  (let [parts (case route
          "/" ["/"]
          (str/split route #"/"))
        regex-parts (map (fn [[i p]]
          (let [res
            (if (is-route-param p)
              #"(\w+)"
              (re-pattern p))]
          (if (= i (dec (count parts)))
            res
            (str res #"\/"))))
          (map-indexed vector parts))]
    (re-pattern (str/join nil regex-parts))))

; (defmacro is-ref-fn? [r]
;   `(fn? (resolve #'~r)))

(defn- get-route-info [[k h]]
  "Gets route info for a route.
  This includes the regex pattern, and the
  names of the path params from the provided
  route path.
  Input `[k h]` should a pair of a route path,
  and a handler."
  (if (fn? h)
    {k {:handler h
        :pattern (gen-regex-for-route k)
        :path-params (parse-path-params k)
        :path k}}
    (throw (AssertionError.
      (str k ": handler must be a Function.\n"
        "You supplied: " (str h))))))

(defn- map-with-info [routes]
  "Maps routes with their route info"
  (->> (map get-route-info routes)
       (into {})))

; public fns
(defn set-routes [routes]
  "public router setter function that allows
   user to set-routes. this sets regex matchers
   and other info for each route
   `routes` should be a Map<key, handler>"
  (let [routes (map-with-info routes)]
    (swap! state assoc :routes routes))
    routes)

(defn- run-req-logger []
  "Runs the logger. You can access route info
  through the state atom."
  (when-let [logger (get-logger)]
    (let [log-req (get-in @settings [:log :request])]
      (do
        (log-req logger)))))

(defn handle-req [request & [routes]]
  "public request handler function that you
  `request` should be an incoming jetty request"
  (swap! state assoc :current-path (:uri request))
  (swap! state assoc :query-params
    (parse-query-string (:query-string request)))
  (run-req-logger)
  (if-let [handler (match-handler (:current-path @state) routes)]
    (do (log (str "Using handler: " handler))
        (handler request))
    (handle404 request)))

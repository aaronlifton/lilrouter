(ns lilrouter.test.mock)

(defn gen-mock-request [& {:keys [uri] :or {uri "/"}}]
  {:ssl-client-cert nil,
   :protocol "HTTP/1.1",
   :remote-addr "0:0:0:0:0:0:0:1",
   :headers
   {"cookie"
    "guest_token=qwerty",
    "cache-control" "max-age=0",
    "accept"
    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
    "upgrade-insecure-requests" "1",
    "connection" "keep-alive",
    "user-agent"
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36",
    "host" "localhost:9001",
    "accept-encoding" "gzip, deflate, br",
    "accept-language" "en-US,en;q=0.9"},
   :server-port 9001,
   :content-length nil,
   :content-type nil,
   :character-encoding nil,
   :uri uri,
   :server-name "localhost",
   :query-string nil,
   :body nil,
   :scheme :http,
   :request-method :get})
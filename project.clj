(defproject lilrouter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                  [ring/ring-core "1.6.3"]
                  [ring/ring-jetty-adapter "1.6.3"]
                  [ring/ring-devel "1.6.3"]
                  [ragtime "0.8.0"]
                  [org.xerial/sqlite-jdbc "3.7.2"]
                  [org.clojure/tools.logging "0.4.1"]]
  :plugins [[lein-ring "0.12.1"]]
  :main lilrouter.core
  :repl-options {:init-ns lilrouter.core}
  :ring {:handler demo.index/handler}
  :profiles {:test {:main lilrouter.test
                    :repl-options {:init-ns lilrouter.test}}
             :demo {:main demo.index}})

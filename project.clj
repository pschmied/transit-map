(defproject transit-map "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :plugins [[lein-localrepo "0.5.3"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [quil "2.2.5"]
                 [unfolding "0.9.6"]    ; Installed in local repo
                 [json4proc "0.9.6"]    ; Ditto; shipped with unfolding
                 [log4j "1.2.15"]
                 [yesql "0.4.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]]
  :aot [transit-map.core]
  :main transit-map.core)

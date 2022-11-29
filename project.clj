(defproject weatherchart "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "AGPL-3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0.txt"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "2.4.0"]
                 [clojure.java-time "1.1.0"]
                 [org.threeten/threeten-extra "1.2"] ; needed by clojure.java-time for intervals
                 [clj-http "3.12.3"]
                 ]
  :repl-options {:init-ns weatherchart.core}
  :global-vars {*warn-on-reflection* true}
  :main weatherchart.core
  :aot [weatherchart.core]
  :plugins [[lein-cljfmt "0.9.0"]
            [cider/cider-nrepl "0.24.0"]])

(ns weatherchart.data
  (:require [clojure.data.json :as json])
  (:require [java-time.api])
  (:require [clj-http.client]))

(def raw-json
  ;(slurp "gridpoints2.json")) ; DEBUG
  (:body (clj-http.client/get "https://api.weather.gov/gridpoints/PHI/45,77")))

(def gridpoint-data
  ((json/read-str raw-json) "properties"))

(defn raw-to-datapoints
  "Return a hashmap (TODO make it a record) containing the datetime and value of the beginning and
  end of each datapoint."
  [raw-data]
  (def interval (java-time.api/interval (raw-data "validTime")))
  [{:datetime (java-time.api/zoned-date-time (java-time.api/start interval) "UTC") :value (raw-data "value")}
   {:datetime (java-time.api/zoned-date-time (java-time.api/end interval) "UTC") :value (raw-data "value")}])

(defn points-for-layer
  [layer-name]
  (flatten (map #(raw-to-datapoints %)
                (get-in gridpoint-data [layer-name "values"]))))

(def earliest-datetime
  (:datetime (first (points-for-layer "temperature"))))

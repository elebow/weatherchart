(ns weatherchart.data
  (:require [clojure.data.json :as json])
  (:require [java-time.api])
  (:require [clj-http.client]))

; See https://weather-gov.github.io/api/gridpoints for explanation of each layer

(def raw-json
  ;(slurp "gridpoints2.json")) ; DEBUG
  (:body (clj-http.client/get "https://api.weather.gov/gridpoints/PHI/45,77")))

(def gridpoint-data
  ((json/read-str raw-json) "properties"))

(defn raw-to-datapoints
  "Return a hashmap (TODO make it a record) containing the instant and value of the beginning and
  end of each datapoint."
  [raw-data]
  (def interval (java-time.api/interval (raw-data "validTime")))
  [{:instant (java-time.interval/start interval) :value (raw-data "value")}
   {:instant (java-time.interval/end interval) :value (raw-data "value")}])

(defn points-for-layer
  [layer-name]
  (flatten (map #(raw-to-datapoints %)
                (get-in gridpoint-data [layer-name "values"]))))

(def earliest-instant
  (:instant (first (points-for-layer "temperature"))))

(ns weatherchart.data
  (:require [clojure.data.json :as json])
  (:require [java-time.api])
  (:require [clj-http.client]))


(defn store-and-return-blob
  "Store a blob on disk. Returns the blob."
  [blob]
  (do
    (with-open [outfile (clojure.java.io/writer "file:/tmp/weatherchart-last-gridpoints.json")]
      (.write outfile blob))
    {:from-storage-timestamp false :json blob}))

(defn retrieve-stored-blob
  "Retrieves a blob from disk."
  []
  {:from-storage-timestamp (str (java-time.api/instant (.lastModified (clojure.java.io/file "/tmp/weatherchart-last-gridpoints.json"))))
   :json (slurp "/tmp/weatherchart-last-gridpoints.json")})

(def fetched-data
  ;{:from-storage-timestamp (str (java-time.api/instant (.lastModified (clojure.java.io/file "gridpoints.json"))))
  ; :error "400 some error"
  ; :json (slurp "gridpoints.json")}) ; DEBUG
  (let [response (clj-http.client/get "https://api.weather.gov/gridpoints/PHI/45,77" {:throw-exceptions false})]
    (if (clj-http.client/unexceptional-status? (:status response))
        (store-and-return-blob (:body response))
        (assoc (retrieve-stored-blob) :error (str (:status response) " " (java.net.URLEncoder/encode (subs (:body response) 0 100)))))))

;(def from-storage-timestamp (:from-storage-timestamp fetched-data))

(def gridpoint-data
  ((json/read-str (:json fetched-data)) "properties"))

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

; two irregular layers
(def points-for-weather-layer
  (->> (map #(let [interval (java-time.api/interval (% "validTime"))
                  value (first (% "value"))] ; TODO does value ever have more than one element?
             (hash-map :datetime-start (java-time.api/zoned-date-time (java-time.api/start interval) "UTC")
                       :datetime-end (java-time.api/zoned-date-time (java-time.api/end interval) "UTC")
                       :coverage (value "coverage")
                       :weather (value "weather")
                       :intensity (value "intensity")
                       :attributes (value "attributes")))
             (get-in gridpoint-data ["weather" "values"]))
       (remove #(nil? (:weather %)))))
(def points-for-hazards-layer [])

(def earliest-datetime (:datetime (first (points-for-layer "temperature"))))
(def latest-datetime (:datetime (last (points-for-layer "temperature"))))

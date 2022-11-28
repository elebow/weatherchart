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
  ;{:from-storage-timestamp false
  ; :json (slurp "gridpoints.json")}) ; DEBUG
  (let [response (clj-http.client/get "https://api.weather.gov/gridpoints/PHI/45,77" {:throw-exceptions false})]
    (if (clj-http.client/unexceptional-status? (:status response))
        (store-and-return-blob (:body response))
        (retrieve-stored-blob))))

(def from-storage-timestamp (:from-storage-timestamp fetched-data))

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

(def earliest-datetime
  (:datetime (first (points-for-layer "temperature"))))

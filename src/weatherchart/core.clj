(ns weatherchart.core
  (:require weatherchart.data)
  (:require weatherchart.chart)
  (:require weatherchart.weatherconditions)
  (:gen-class))

; See https://weather-gov.github.io/api/gridpoints for explanation of each layer

(defn -main
  [& args]
  (println (str
             "<html>
                <head>
                  <title>weatherchart</title>
                  <link rel='icon' href='https://weatherchart.eddielebow.com/favicon.ico' />
                  <style>"
                  (slurp (clojure.java.io/resource "style.css"))
                  "</style>
                </head>
                <body>"
                   (let [timestamp (:from-storage-timestamp weatherchart.data/fetched-data)]
                        (if timestamp (str "Failed to fetch gridpoints. Using stored data from " timestamp)))
                  "<h2 class='chart-title'>weather conditions and hazards</h2>"
                  weatherchart.weatherconditions/render-elements
                  "<h2 class='chart-title'><span style='color:var(--color-temperature)'>temperature (°C)</span> and <span style='color:var(--color-apparent-temperature)'>apparent temperature (°C)</span></h2>"
                  (weatherchart.chart/render-chart {:min -10 :max 40 :step 10} [{:points (weatherchart.data/points-for-layer "temperature") :colorname "temperature"}
                                                                                {:points (weatherchart.data/points-for-layer "apparentTemperature") :colorname "apparent-temperature"}])
                  "<h2 class='chart-title' style='color:var(--color-relative-humidity)'>relative humidity (%)</h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "relativeHumidity") :colorname "relative-humidity"}])
                  "<h2 class='chart-title' style='color:var(--color-probability-of-precipitation)'>probability of precipitation (%)</h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "probabilityOfPrecipitation") :colorname "probability-of-precipitation"}])
                  "<h2 class='chart-title'><span style='color:var(--color-quantitative-precipitation)'>rain amount (mm)</span> and <span style='color:var(--color-snowfall-amount)'>snowfall ammount (mm)</span></h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 50 :step 10} [{:points (weatherchart.data/points-for-layer "snowfallAmount") :colorname "snowfall-amount"}
                                                                              {:points (weatherchart.data/points-for-layer "quantitativePrecipitation") :colorname "quantitative-precipitation"}])
                  "<h2 class='chart-title'><span style='color:var(--color-wind-speed)'>wind speed (km/h)</span> and <span style='color:var(--color-wind-gust)'>wind gust (km/h)</span></h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "windGust") :colorname "wind-gust"}
                                                                               {:points (weatherchart.data/points-for-layer "windSpeed") :colorname "wind-speed"}])
                  "<h2 class='chart-title' style='color:var(--color-sky-cover)'>sky cover (%)</h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "skyCover") :colorname "sky-cover"}])

                  ; Temporary display to aid development
                  "<br><br>debug:<br>weather attributes: "
                  (clojure.string/join ";" (map #(:attributes %) weatherchart.data/points-for-weather-layer))
                  "<br>hazards: "
                  (str (get-in weatherchart.data/gridpoint-data ["hazards" "values"]))

                  (let [error (:error weatherchart.data/fetched-data)]
                       (if error (str "<br>error from API:<br>" error)))
                "</body></html>")))






   ;"pressure" ; no datapoints from API
   ;"windDirection" (degree of angle)
   ;"hazards" ; non-numeric

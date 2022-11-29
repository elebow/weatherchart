(ns weatherchart.core
  (:require weatherchart.data)
  (:require weatherchart.chart)
  (:require weatherchart.weatherconditions)
  (:gen-class))

; See https://weather-gov.github.io/api/gridpoints for explanation of each layer

(defn -main
  []
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
                   (if weatherchart.data/from-storage-timestamp (str "Failed to fetch gridpoints. Using stored data from " weatherchart.data/from-storage-timestamp))
                  "<h2 class='chart-title'>weather conditions and hazards</h2>"
                  weatherchart.weatherconditions/render-elements
                  "<h2 class='chart-title'><span style='color:var(--color-temperature)'>temperature (°C)</span> and <span style='color:var(--color-apparent-temperature)'>apparent temperature (°C)</span></h2>"
                  (weatherchart.chart/render-chart {:min -10 :max 40 :step 10} [{:points (weatherchart.data/points-for-layer "temperature") :colorname "temperature"}
                                                                                {:points (weatherchart.data/points-for-layer "apparentTemperature") :colorname "apparent-temperature"}])
                  "<h2 class='chart-title' style='color:var(--color-relative-humidity)'>relative humidity (%)</h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "relativeHumidity") :colorname "relative-humidity"}])
                  "<h2 class='chart-title' style='color:var(--color-probability-of-precipitation)'>probability of precipitation (%)</h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "probabilityOfPrecipitation") :colorname "probability-of-precipitation"}])
                  "<h2 class='chart-title' style='color:var(--color-quantitative-precipitation)'>rain amount (mm)</h2>"
                  (weatherchart.chart/render-chart {:min 0 :max 20 :step 10} [{:points (weatherchart.data/points-for-layer "quantitativePrecipitation") :colorname "quantitative-precipitation"}])
                "</body></html>")))






   ;"snowfallAmount" (mm)
   ;"pressure"
   ;"skyCover" (%)
   ;"windSpeed" (km/h)
   ;"hazards" ; non-numeric

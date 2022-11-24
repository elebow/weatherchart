(ns weatherchart.core
  (:require weatherchart.data)
  (:require weatherchart.chart)
  (:gen-class))

(defn -main
  []
  (println (str
             "<html>
                <head>
                  <style>"
                  (slurp (clojure.java.io/resource "style.css"))
                  "</style>
                </head>
                <body>
                  <p class='chart-title'>temperature and apparent temperature</p>"
                  (weatherchart.chart/render-chart {:min -10 :max 40 :step 10} [{:points (weatherchart.data/points-for-layer "temperature") :color "purple"}
                                                                                {:points (weatherchart.data/points-for-layer "apparentTemperature") :color "red"}])
                  "<p class='chart-title'>relative humidity</p>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "relativeHumidity") :color "green"}])
                  "<p class='chart-title'>probability of precipitation</p>"
                  (weatherchart.chart/render-chart {:min 0 :max 100 :step 10} [{:points (weatherchart.data/points-for-layer "probabilityOfPrecipitation") :color "blue"}])
                  "<p class='chart-title'>rain amount</p>"
                  (weatherchart.chart/render-chart {:min 0 :max 20 :step 10} [{:points (weatherchart.data/points-for-layer "quantitativePrecipitation") :color "darkgreen"}])
                "</body></html>")))






   ;"snowfallAmount"
   ;"pressure"
   ;"skyCover"
   ;"weather" ; non-numeric
   ;"windSpeed"

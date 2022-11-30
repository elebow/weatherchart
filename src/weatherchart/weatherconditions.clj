(ns weatherchart.weatherconditions
  (:require weatherchart.chart)
  (:require weatherchart.data))

; Render non-quantitative weather conditions.
; We use SVG to ease vertical alignment with the quantitative charts.

(def render-weather-elements
  (apply str (map #(let [x1 (weatherchart.chart/x-for-datetime (:datetime-start %))
                         x2 (weatherchart.chart/x-for-datetime (:datetime-end %))
                         y-textorigin (- weatherchart.chart/chart-height 5)
                         y-top (+ 2 y-textorigin)
                         y-bottom weatherchart.chart/chart-height]
                        (str "<polyline points='" x1 "," y-top " " x1 "," y-bottom " " x2 "," y-bottom " " x2 "," (- y-bottom 2) "' class='weathercondition-marker' />"
                             "<text x='" x1 "' y='" y-textorigin "' transform='rotate(-40, " x1 ", " y-textorigin ")' class='weathercondition-label'>" (:intensity %) " " (:weather %) " " (:coverage %) "</text>"))
       weatherchart.data/points-for-weather-layer)))

(def render-elements
  (str "<svg viewBox='0 -1 " (weatherchart.chart/x-for-datetime weatherchart.data/latest-datetime) " " (+ 8 weatherchart.chart/chart-height) "' height='" (* 6 weatherchart.chart/chart-height) "px' xmlns='http://www.w3.org/2000/svg'>"
        weatherchart.chart/render-grid-vert-midnights
        weatherchart.chart/render-grid-vert-hours
        weatherchart.chart/render-current-hour-highlight
        render-weather-elements
      "</svg>"))

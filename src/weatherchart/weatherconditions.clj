(ns weatherchart.weatherconditions
  (:require weatherchart.chart)
  (:require weatherchart.data))

; Render non-quantitative weather conditions.
; We use SVG to ease vertical alignment with the quantitative charts.

(def render-weather-elements
  (apply str (map #(let [x (weatherchart.chart/x-for-datetime (:datetime-start %))
                         y-textorigin (- weatherchart.chart/chart-height 5)]
                        (str "<line x1='" x "' y1='" (+ 2 y-textorigin) "' x2='" x "' y2='" weatherchart.chart/chart-height "' class='weathercondition-marker' />"
                             "<text x='" x "' y='" y-textorigin "' transform='rotate(-40, " x ", " y-textorigin ")' class='weathercondition-label'>" (:intensity %) " " (:weather %) " " (:coverage %) "</text>"))
       weatherchart.data/points-for-weather-layer)))
  ; TODO visually show :datetime-end,  and include :attributes

(def render-elements
  (str "<svg viewBox='0 0 " (weatherchart.chart/x-for-datetime weatherchart.data/latest-datetime) " " (+ 15 weatherchart.chart/chart-height) "' height='400px' xmlns='http://www.w3.org/2000/svg'>"
        weatherchart.chart/render-grid-vert-midnights
        weatherchart.chart/render-grid-vert-hours
        weatherchart.chart/render-current-hour-highlight
        render-weather-elements
      "</svg>"))

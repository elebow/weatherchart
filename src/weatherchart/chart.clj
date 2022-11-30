(ns weatherchart.chart)

(def chart-grid-margin-left 10)
(def chart-height 40)
(def time-scale-factor 600.0)

(def midnight-today (java-time.api/truncate-to (java-time.api/offset-date-time) :days))
(def midnights (map #(java-time.api/plus midnight-today (java-time.api/days %)) (range)))
(defn seconds-until-datetime
  [datetime]
  (java-time.api/as (java-time.api/duration weatherchart.data/earliest-datetime datetime) :seconds))
(def hours (map #(java-time.api/plus weatherchart.data/earliest-datetime (java-time.api/hours %)) (range)))

(defn x-for-seconds
  "Returns the SVG x-coordinate for a given number of seconds from the current time."
  [seconds]
  (+ (double (/ seconds time-scale-factor))
     chart-grid-margin-left))

(defn x-for-datetime
  "Returns the SVG x-coordinate for a given datetime."
  [datetime]
  (x-for-seconds (seconds-until-datetime datetime)))

(defn y-in-scale
  "Scales a `y` in [`range-min`, `range-max`] to [0, `chart-height`]"
  [range-min range-max y]
  (let [offset (- 0 range-min)
        ratio (double (/ chart-height (- range-max range-min)))]
    (* ratio (+ offset y))))

(defn y-for-value
  "Returns the SVG y-coordinate for a given value and scale. SVG's y-axis increases _downward_."
  [range-min range-max y]
  (- chart-height (y-in-scale range-min range-max y)))

(defn y-label-offset
  "Returns an offset for a label. Above the data (negative offset) unless it would overflow the chart."
  [y]
  (cond
    (< y 8) (+ y 6)
    :else (- y 4)))

(defn polyline-points
  "Returns `x,y` pair strings suitable for an SVG polyline, scaled and oriented correctly."
  [range-min range-max points]
  (map #(str (x-for-datetime (:datetime %)) "," (y-for-value range-min range-max (:value %)) " ")
       points))

(defn render-line-for-data
  [range-min range-max points colorname]
  (str "<polyline points='" (apply str (polyline-points range-min range-max points)) "' style='stroke:var(--color-" colorname ")' class='data-line' />"))

(defn render-labels-for-data
  [range-min range-max points colorname]
  (apply str (map #(let [x (x-for-datetime (:datetime %))
                         y (y-label-offset (y-for-value range-min range-max (:value %)))]
                    (str "<rect x='" x " 'y='" (- y 5) "' width='5' height='5' stroke='none' class='data-label-mask' />"
                         "<text x='" x "' y='" y "' style='fill:var(--color-" colorname ")' class='data-label-text'>" (int( :value %)) "</text>"))
            points)))

(defn render-grid-horiz
  [range-min range-max ys]
  (apply str (map #(let [y (y-for-value range-min range-max %)]
         (str "<text x='0' y='" (+ y 2) "' class='grid-label-text'>" % "</text>"
              "<line x1='" chart-grid-margin-left "' y1='" y "' x2='100%' y2='" y "' class='grid-line' />"))
       ys)))

(def render-grid-vert-midnights
  (apply str (map
               #(let [x (x-for-datetime %)]
                 (str "<text x='" x "' y='" (+ 14 chart-height)  "' class='grid-label-text-midnight'>" (java-time.api/format "EEE Y-M-d" %) "</text>"
                     "<line x1='" x "' y1='0%' x2='" x "' y2='" (+ 8 chart-height) "' class='grid-line-vert-midnight' />"))
              (take 9 midnights))))
(def render-grid-vert-hours
  (apply str (map
              #(let [ x (x-for-datetime %)]
                (str "<text x='" x "' y='" (+ 6 chart-height) "' class='grid-label-text'>" (java-time.api/format "H" (java-time.api/with-zone-same-instant % "America/New_York")) "</text>"
                     "<line x1='" x "' y1='0%' x2='" x "' y2='" (+ 3 chart-height) "' class='grid-line' />"))
              (take (* 9 24) hours))))

(def render-current-hour-highlight
  (let [beginning-of-current-hour (java-time.api/truncate-to (java-time.api/offset-date-time) :hours)
        end-of-current-hour (java-time.api/plus beginning-of-current-hour (java-time.api/hours 1))]
       (str "<polygon points='" (x-for-datetime beginning-of-current-hour) ",0 "
                                (x-for-datetime beginning-of-current-hour) "," chart-height " "
                                (x-for-datetime end-of-current-hour) "," chart-height " "
                                (x-for-datetime end-of-current-hour) ",0' class='current-hour-highlight' />")))

(defn render-chart
  [range-limits data-series]
  (str "<svg viewBox='0 -1 " (x-for-datetime weatherchart.data/latest-datetime) " " (+ 8 chart-height) "' height='" (* 6 chart-height) "px' xmlns='http://www.w3.org/2000/svg'>"
        render-grid-vert-hours
        render-grid-vert-midnights
        render-current-hour-highlight
        (render-grid-horiz (:min range-limits) (:max range-limits) (range (:min range-limits) (+ 1 (:max range-limits)) (:step range-limits)))
        (apply str (map #(seq [(render-line-for-data (:min range-limits) (:max range-limits) (:points %) (:colorname %))
                              (render-labels-for-data (:min range-limits) (:max range-limits) (take-nth 2 (:points %)) (:colorname %))])
                        data-series))
      "</svg>"))

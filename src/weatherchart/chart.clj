(ns weatherchart.chart)

(def chart-grid-margin-left 10)
(def chart-height 50)
(def time-scale-factor 500.0)

; TODO move these to a `time` module
(def midnight-today (java-time.api/offset-date-time (java-time.api/year) (java-time.api/month) (java-time.api/day-of-month) 0 0 0 0))
;(def midnight-today (java-time.api/zoned-date-time (java-time.api/year 2022) (java-time.api/month 10) (java-time.api/day-of-month 27) 0 0 0 0)) ; DEBUG
(def midnights (map #(java-time.api/plus midnight-today (java-time.api/days %)) (range)))
(defn seconds-until-instant
  [instant]
  ;(java-time.api/as (java-time.api/duration (java-time.api/instant) instant) :seconds)); TODO should grid start at current time?
  (java-time.api/as (java-time.api/duration (java-time.api/instant midnight-today) instant) :seconds)) ; TODO should grid start at most recent midnight?
  ; TODO should grid start at earliest datapoint, which is often several hours old?
  ;(java-time.api/as (java-time.api/duration (java-time.api/instant "2022-10-27T20:00:00Z") instant) :seconds)) ;DEBUG
(def midnights-seconds
  (map #(hash-map :label (java-time.api/format "EEE Y-M-d" %)
                  :seconds (seconds-until-instant %))
       midnights))
(def hours (map #(java-time.api/plus midnight-today (java-time.api/hours %)) (range)))
(def hours-seconds
  (map #(hash-map :label (java-time.api/format "H" %)
                  :seconds (seconds-until-instant %))
       hours))

(defn x-for-seconds
  "Returns the SVG x-coordinate for a given number of seconds from the current time."
  [seconds]
  (+ (double (/ seconds time-scale-factor))
     chart-grid-margin-left))

(defn x-for-instant
  "Returns the SVG x-coordinate for a given `java.time.Instant`."
  [instant]
  (x-for-seconds (seconds-until-instant instant)))

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
  (map #(str (x-for-instant (:instant %)) "," (y-for-value range-min range-max (:value %)) " ")
       points))

(defn render-line-for-data
  [range-min range-max points colorname]
  (str "<polyline points='" (apply str (polyline-points range-min range-max points)) "' style='stroke:var(--color-" colorname ")' class='data-line' />"))

(defn render-labels-for-data
  [range-min range-max points colorname]
  (apply str (map #(let [x (x-for-instant (:instant %))
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
              #(str "<text x='" (:x %) "' y='" (+ 14 chart-height)  "' class='grid-label-text-midnight'>" (:label %) "</text>"
                    "<line x1='" (:x %) "' y1='0%' x2='" (:x %) "' y2='" (+ 8 chart-height) "' class='grid-line-vert-midnight' />")
              (take 9 (map #(hash-map :x (x-for-seconds (:seconds %)) :label (:label %)) midnights-seconds)))))
(def render-grid-vert-hours
  (apply str (map
              #(str "<text x='" (:x %) "' y='" (+ 6 chart-height) "' class='grid-label-text'>" (:label %) "</text>"
                    "<line x1='" (:x %) "' y1='0%' x2='" (:x %) "' y2='" (+ 3 chart-height) "' class='grid-line' />")
              (take (* 9 24) (map #(hash-map :x (x-for-seconds (:seconds %)) :label (:label %)) hours-seconds)))))

(defn chart-width
  [data]
  ; TODO take all data series and find the max last
  (x-for-instant (:instant (last data))))

(defn render-chart
  [range-limits data-series]
  (str "<svg viewBox='0 0 " (chart-width (:points (first data-series))) " " (+ 15 chart-height) "' height='400px' xmlns='http://www.w3.org/2000/svg'>"
        render-grid-vert-hours
        render-grid-vert-midnights
        (render-grid-horiz (:min range-limits) (:max range-limits) (range (:min range-limits) (+ 1 (:max range-limits)) (:step range-limits)))
        (apply str (map #(seq [(render-line-for-data (:min range-limits) (:max range-limits) (:points %) (:colorname %))
                              (render-labels-for-data (:min range-limits) (:max range-limits) (take-nth 2 (:points %)) (:colorname %))])
                        data-series))
      "</svg>"))

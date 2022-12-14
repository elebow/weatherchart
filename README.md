# weatherchart

Fetches forecast data from the National Weather Service public data API and presents some charts.

## Build instructions

1. Install Leiningen (https://codeberg.org/leiningen/leiningen)
1. Use `lein run` to execute
1. Use `lein uberjar` to build a standalone executable JAR

## Run instructions

1. Find the desired grid according to the instructions at https://www.weather.gov/documentation/services-web-api. It should be of the pattern `LLL/XX,YY`, where `LLL` identifies the forecast office and `XX` and `YY` identify the grid square. The documentation provides instructions for finding this from latitude,longitude coordinates.
1. `WEATHERCHART_GRIDPOINT_ID="PHI/49,75" java -jar weatherchart.jar > chart.html`

(ns transit-map.core
  (:import (de.fhpotsdam.unfolding UnfoldingMap)
           (de.fhpotsdam.unfolding.geo Location)
           (de.fhpotsdam.unfolding.data GeoJSONReader)
           (de.fhpotsdam.unfolding.providers StamenMapProvider)
           (de.fhpotsdam.unfolding.marker SimplePointMarker)
           (de.fhpotsdam.unfolding.utils MapUtils))
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [yesql.core :as sql :refer [defqueries]])
  (:gen-class))
(sql/defqueries "transit_map/queries.sql")

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost/transit"})

(def myroute (rt-json db))

(def odsize 30)                         ; Size of our OD symbol circle

(defn draw-o [state]
  (q/stroke 0 255 0)
  (q/fill 0 255 0 75)
  (q/ellipse (:ox state) (:oy state) (:odrad state) (:odrad state)))

(defn draw-d [state]
  (q/stroke 255 0 0)
  (q/fill 255 0 0 75)
  (let [rad (Math/abs (- (:odrad state) odsize))]
    (q/ellipse (:dx state) (:dy state) rad rad)))

(defn setup []
  (q/frame-rate 30)
  (q/smooth)
  ;; setup function returns initial state.
  ;; State is one big happy hashmap
  (def bgmap
    (doto (UnfoldingMap.
           (quil.applet/current-applet)
           (de.fhpotsdam.unfolding.providers.StamenMapProvider$TonerBackground.))
      (.setZoomRange 10 13)
      (.zoomToLevel 13)
      (.draw)))
  (def myfeat (GeoJSONReader/loadDataFromJSON
               (quil.applet/current-applet)
               (.toString (:row_to_json (first myroute)))))
  (def mymark (MapUtils/createSimpleMarkers myfeat))
  (q/stroke-weight 2)
  {:zoomlevel 10
   :lat 47.628 :lon -122.33
   :ox -100 :oy -100 :odrad odsize
   :dx -100 :dy -100})

(defn update-state [state]
  (-> state
   (update-in [:odrad] #(if (<= % odsize) (+ 1.5 %) 0))))

(defn mouse-clicked [state event]
  (case (:button event)
    :left (-> state (assoc :ox (:x event) :oy (:y event)))
    :right (-> state (assoc :dx (:x event) :dy (:y event)))
    ))

(defn draw-state [state]
  (.panTo bgmap (Location. (:lat state) (:lon state)))
  (.draw bgmap)
  ;(.addMarkers bgmap mymark)
  (draw-o state)
  (draw-d state)
  )

;; (defn -main [& args]
;;   (q/sketch
(q/defsketch transit-map
  :title "Transit map"
  :size [600 725]
  ;; setup function called only once, during sketch initialization.
  :setup setup
  :renderer :opengl
  :features [;:exit-on-close
             :keep-on-top]
  ;; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :mouse-clicked mouse-clicked
  ;; This sketch uses functional-mode middleware.
  :middleware [m/fun-mode m/pause-on-error])


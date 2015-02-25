(ns transit-map.core
  (:import (de.fhpotsdam.unfolding UnfoldingMap)
           (de.fhpotsdam.unfolding.geo Location)
           (de.fhpotsdam.unfolding.providers StamenMapProvider)
           (de.fhpotsdam.unfolding.marker SimplePointMarker))
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  ;; Only has close to enough time to plot if the framerate is very
  ;; low, and even then doesn't quite get the job done.
  (q/frame-rate 1)
  (q/smooth)
  ;; setup function returns initial state.
  ;; State is one big happy hashmap
  (def bgmap
    (doto (UnfoldingMap.
           (quil.applet/current-applet)
           (de.fhpotsdam.unfolding.providers.StamenMapProvider$TonerBackground.))
      (.setZoomRange 10 13)
      (.zoomToLevel 12)
      (.panTo (Location. 47.625 -122.34))
      (.draw)))
  {:zoomlevel 10
   :location [47.628 -122.25]})

(defn update-state [state])

(defn draw-state [state]
  (.draw bgmap))

(q/defsketch transit-map
  :title "Transit map"
  :size [470 710]
  ;; setup function called only once, during sketch initialization.
  :setup setup
  :renderer :java2d
  :features [:keep-on-top]
  ;; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  ;; This sketch uses functional-mode middleware.
  :middleware [m/fun-mode m/pause-on-error])

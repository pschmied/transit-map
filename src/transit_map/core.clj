(ns transit-map.core
  (:import (de.fhpotsdam.unfolding UnfoldingMap)
           (de.fhpotsdam.unfolding.geo Location)
           (de.fhpotsdam.unfolding.data GeoJSONReader)
           (de.fhpotsdam.unfolding.providers StamenMapProvider)
           (de.fhpotsdam.unfolding.marker SimplePointMarker)
           (de.fhpotsdam.unfolding.utils MapUtils))
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [yesql.core :as sql :refer [defqueries]]
            [clojure.data.json :as json]
            [geo.spatial :as spatial])
  (:gen-class))

(sql/defqueries "transit_map/queries.sql")

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost/transit"})

(def myroutes
  (let [recs (apply (fn [x] (json/read-str (x :geom)))
                    (routes-connecting-1000m db))]
    (recs "coordinates")))

(defn pairs [xs] (map vector xs (rest xs)))

(defn coord-to-xy [bgmap coord]
  (let [lon (first coord)
        lat (last coord)]
    (let [position (.getScreenPosition (SimplePointMarker. (Location. lat lon)) bgmap)]
      [(.x position) (.y position)])))

(defn route-to-lines [bgmap route]
  (pairs (map (fn [x] (coord-to-xy bgmap x)) route)))

(defn routes-to-lines [bgmap routes]
  (apply concat (map (fn [x] (route-to-lines bgmap x)) routes)))

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

(defn draw-routes [state]
  (doseq [l (state :lines)]
    (q/stroke 0 200 0)
    (apply q/line (flatten l))))

(defn setup []
  (q/frame-rate 30)
  (q/smooth)
  (q/stroke-weight 2)
  (def bgmap
    (doto (UnfoldingMap.
           (quil.applet/current-applet)
           (de.fhpotsdam.unfolding.providers.StamenMapProvider$TonerBackground.))
      (.setZoomRange 13 14)
      (.zoomToLevel 13)))
  ;; setup function returns initial state.
  ;; State is one big happy hashmap
  {:bgmap bgmap
   :lat 47.628 :lon -122.33
   :ox -100 :oy -100 :odrad odsize
   :dx -100 :dy -100
   :lines (routes-to-lines bgmap myroutes)
   })

(defn update-state [state]
  (-> state
   (update-in [:odrad] #(if (<= % odsize) (+ 1.5 %) 0))))

(defn mouse-clicked [state event]
  (case (:button event)
    :left (-> state (assoc :ox (:x event) :oy (:y event)))
    :right (-> state (assoc :dx (:x event) :dy (:y event)))
    ))

(defn draw-state [state]
  (let [bgmap (state :bgmap)
        lat (state :lat)
        lon (state :lon)]
    (.panTo bgmap (Location. lat lon))
    (.draw bgmap)
    (draw-o state)
    (draw-d state)
    (draw-routes state)
    ))

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


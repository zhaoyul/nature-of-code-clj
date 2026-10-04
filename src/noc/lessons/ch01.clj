(ns noc.lessons.ch01
  "第 1 章自足课堂片段的环境与移动者。"
  (:require [noc.math :as m] [noc.draw :as d]))

(def env {:width 640 :height 240 :mouse [480 180]
          :topspeed 10 :acceleration 0.2})

(defn mover
  ([] (mover env))
  ([{:keys [width height]}]
   {:position [(m/random width) (m/random height)]
    :velocity [(m/random -2 2) (m/random -2 2)]
    :acceleration [0 0] :topspeed 10}))

(defn update-motion [{:keys [position velocity acceleration topspeed] :as mover}]
  (let [velocity (m/limit (m/v+ velocity acceleration) topspeed)]
    (assoc mover :position (m/v+ position velocity) :velocity velocity)))

(defn wrap-edges [mover {:keys [width height]}]
  (let [[x y] (:position mover)]
    (assoc mover :position
           [(cond (> x width) 0 (< x 0) width :else x)
            (cond (> y height) 0 (< y 0) height :else y)])))

(defn show [mover]
  (d/circle (:position mover) 48 [127]))

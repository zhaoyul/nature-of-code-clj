(ns noc.lessons.ch02
  "第 2 章课堂代码的受力、接触和流体工具。"
  (:require [noc.math :as m] [noc.draw :as d]))

(def env {:width 640 :height 240 :mouse [400 180] :pressed? true
          :gravity 0.1 :wind 0.1 :friction 0.1 :drag 0.1 :G 1})

(defn mover
  ([] (mover [320 30] 1))
  ([position mass]
   {:position position :velocity [0 0] :acceleration [0 0]
    :mass mass :radius (* mass 8)}))

(defn show [body]
  (d/circle (:position body) (* 16 (:mass body)) [175]))

(defn contact? [body {:keys [height]}]
  (> (second (:position body)) (- height (:radius body) 1)))

(defn bounce-bottom [body {:keys [height]} restitution]
  (let [[x y] (:position body) bottom (- height (:radius body))]
    (if (> y bottom)
      (-> body (assoc :position [x bottom])
          (update-in [:velocity 1] * (- restitution)))
      body)))

(defn contains-body? [{:keys [x y w h]} body]
  (let [[px py] (:position body)]
    (and (< x px (+ x w)) (< y py (+ y h)))))

(defn drag [{:keys [c]} {:keys [velocity]}]
  (m/set-mag velocity (- (* c (m/mag-sq velocity)))))

(defn liquid [] {:x 0 :y 120 :w 640 :h 120 :c 0.1})

(defn liquid-scene [{:keys [x y w h]}]
  [[:no-stroke] [:fill 175] [:rect x y w h]])

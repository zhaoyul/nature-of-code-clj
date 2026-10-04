(ns noc.lessons.ch11
  "拍翅环境、神经网络遗传操作与局部食物传感器的教学样本。"
  (:require [noc.math :as m] [noc.draw :as d]
            [noc.learning :as learning] [noc.chapters.ch11 :as ch]))

(def env {:width 640 :height 360 :gap 130 :gravity 0.5 :population 12
          :mutation 0.05 :lifespan 120 :speed 1 :reproduction 0.001
          :clicked? false :pressed? false :mouse [320 180]})
(defn parents []
  (mapv (fn [fitness] (assoc (ch/bird (learning/brain 4 8 2)) :fitness fitness)) [8 24 68]))
(defn bird-inputs [bird next-pipe width height]
  [(/ (:y bird) height) (/ (:velocity bird) height)
   (/ (:top next-pipe) height) (/ (- (:x next-pipe) 50) width)])
(defn new-bird [] {:x 50 :y 120 :velocity 0 :gravity 0.5 :flap-force -10})
(defn flap [bird] (update bird :velocity + (:flap-force bird)))
(defn move-bird [bird height]
  (let [v (+ (:velocity bird) (:gravity bird)) y (+ (:y bird) v)]
    (if (> y height)
      (assoc bird :y height :velocity 0)
      (assoc bird :y y :velocity (* v 0.95)))))
(defn new-pipe [width height]
  (let [gap 100 top (m/random (- height gap))]
    {:x width :top top :bottom (+ top gap) :width 20 :velocity 2}))
(defn collision? [bird pipe]
  (and (< (:x pipe) (:x bird) (+ (:x pipe) (:width pipe)))
       (or (< (:y bird) (:top pipe)) (> (:y bird) (:bottom pipe)))))
(defn manual-frame [{:keys [bird pipes frame] :as state}]
  (let [bird (move-bird (if (zero? (mod frame 30)) (flap bird) bird) 240)
        pipes (filterv #(>= (+ (:x %) (:width %)) 0)
                       (mapv #(update % :x - (:velocity %)) pipes))
        pipes (cond-> pipes (zero? (mod frame 100)) (conj (new-pipe 640 240)))]
    (assoc state :bird bird :pipes pipes :frame (inc frame)
           :collision? (boolean (some #(collision? bird %) pipes)))))
(defn manual-scene [{:keys [bird pipes collision?]}]
  (d/scene (d/background)
           (mapcat (fn [{:keys [x top bottom width]}]
                     [[:fill 0] [:rect x 0 width top] [:rect x bottom width (- 240 bottom)]]) pipes)
           (d/circle [(:x bird) (:y bird)] 16)
           (when collision? (d/label "OOPS!"))))
(defn sensors [position radius total]
  (mapv (fn [i]
          {:offset (m/v* (m/from-angle (/ (* i m/tau) total)) radius) :value 0})
        (range total)))
(defn sense [position sensor food]
  (let [tip (m/v+ position (:offset sensor))
        distance (m/distance tip (:position food))]
    (assoc sensor :value (max 0 (- 1 (/ distance (:radius food)))))))
(defn reproduce [creature mutation]
  (ch/neural-bloop (:position creature) (learning/mutate-brain (:brain creature) mutation)))

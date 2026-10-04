(ns noc.chapters.extras01
  "Four companion vector exercises, including a genuine three-dimensional state."
  (:require [noc.math :as m] [noc.draw :as d]
            [noc.chapters.ch00 :as ch00] [noc.chapters.ch01 :as ch01]
            [noc.chapters.extras00 :as extras]))

(defn spec [i directory title setup step scene params]
  {:id (str "extra-1-" i) :chapter 1 :width 640 :height 240 :title title
   :directory (str "examples/01_vectors/" directory) :params params
   :source "noc/chapters/extras01.cljc"
   :setup setup :step step :scene scene})
(defn vector-walk [state {:keys [width height step-size]}]
  (let [[x y] (m/v+ (:position state) (m/v* (m/random-vector) step-size))]
    (extras/record-position state [(m/clamp x 0 (dec width)) (m/clamp y 0 (dec height))])))

(defn ball3-setup [_] {:position [0 0 0] :velocity [2 6 5] :yaw (/ m/pi 3) :pitch (/ m/pi 1.5)})
(defn ball3-step [{:keys [position velocity] :as state} {:keys [box-size pressed? mouse]}]
  (let [p (mapv + position velocity) bound (- (/ box-size 2) 10)
        v (mapv (fn [x vx] (if (or (> x bound) (< x (- bound))) (- vx) vx)) p velocity)]
    (cond-> (assoc state :position p :velocity v)
      pressed? (assoc :yaw (* 0.015 (first mouse)) :pitch (* 0.015 (second mouse))))))
(defn projection [[x y z] yaw pitch width height]
  (let [a (+ (* x (m/cos yaw)) (* z (m/sin yaw)))
        b (- (* z (m/cos yaw)) (* x (m/sin yaw)))
        c (- (* y (m/cos pitch)) (* b (m/sin pitch)))
        depth (+ (* y (m/sin pitch)) (* b (m/cos pitch)))
        scale (/ 500 (+ 500 depth))]
    [(+ (/ width 2) (* scale a)) (+ (/ height 2) (* scale c)) scale]))
(defn ball3-scene [{:keys [position yaw pitch]} {:keys [width height box-size]}]
  (let [h (/ box-size 2) corners (vec (for [x [(- h) h] y [(- h) h] z [(- h) h]] [x y z]))
        project #(projection % yaw pitch width height)
        [x y scale] (project position)]
    (d/scene (d/background) [[:no-fill] [:stroke 39 52 69]]
             (for [i (range 8) j (range (inc i) 8)
                   :when (= 1 (count (filter true? (map not= (nth corners i) (nth corners j)))))
                   :let [[a b] (project (nth corners i)) [c e] (project (nth corners j))]]
               [:line a b c e])
             (d/circle [x y] (* 20 scale) d/teal)
             (d/label (str "三维位置 " (mapv #(long %) position) "；按住鼠标拖动视角") 12 22))))

(defn train-setup [{:keys [width height]}]
  {:position [(- (/ width 2) 32) (- (/ height 2) 65)] :velocity [1 0] :acceleration 0 :last-key nil})
(defn train-step [{[x y] :position [vx _] :velocity :keys [acceleration last-key] :as state} {:keys [width key]}]
  (let [a (+ acceleration (if (= key last-key) 0 (case key :up 0.1 :down -0.1 0)))
        next-v (m/clamp (+ vx a) 0 25)
        next-x (+ x vx)]
    (assoc state :velocity [next-v 0] :position [(cond (> next-x width) -64 (< next-x -64) width :else next-x) y]
           :acceleration (if (zero? next-v) 0 a) :last-key key)))
(defn train-scene [{[x y] :position [vx _] :velocity :keys [acceleration]} {:keys [width height]}]
  (d/scene (d/background) [[:stroke 39 52 69] [:line 0 (/ height 2) width (/ height 2)] [:fill 39 52 69]]
           (mapv (fn [i] [:rect i (/ height 2) 5 2.5]) (range 0 (- width 5) 15))
           [[:image "images/train.jpg" x y 64 64]]
           (d/label (str "↑ 加速、↓ 减速；速度 " (long vx) "；加速度 " acceleration) 12 22)))

(defn attraction-step [{:keys [position velocity] :as state} {:keys [mouse width height topspeed]}]
  (let [direction (m/v- mouse position)
        a (m/set-mag direction (m/map-range (m/mag direction) 0 (max width height) 0 0.2))
        v (m/limit (m/v+ velocity a) topspeed)]
    (assoc state :position (m/v+ position v) :velocity v :acceleration a)))

(def examples
  [(spec 1 "exercise_1_1_walker_with_vectors" "练习 1.1：随机单位向量游走" ch00/walker-setup vector-walk ch00/walker-scene
         {:step-size (extras/parameter "步长" 2 0.1 10 0.1)})
   (spec 2 "exercise_1_3_solution_3_d_bouncing_ball" "练习 1.3：三维弹跳球" ball3-setup ball3-step ball3-scene
         {:box-size (extras/parameter "立方体边长" 160 60 220 1)})
   (assoc (spec 3 "exercise_1_5_solution_accelerate_and_decelerate" "练习 1.5：火车加速与减速" train-setup train-step train-scene {})
          :assets ["images/train.jpg"])
   (spec 4 "exercise_1_8_solution_attraction_magnitude" "练习 1.8：吸引加速度与距离" ch01/mover-setup attraction-step ch01/ball-scene
         {:topspeed (extras/parameter "最高速度" 5 1 15 0.5)})])

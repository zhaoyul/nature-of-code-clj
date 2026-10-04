(ns noc.chapters.ch01
  (:require [noc.math :as m] [noc.draw :as d]))

(defn scalar-setup [_] {:x 100 :y 100 :vx 2.5 :vy 2})
(defn scalar-step [{:keys [x y vx vy] :as state} {:keys [width height]}]
  (let [x (+ x vx) y (+ y vy)]
    (assoc state :x x :y y
           :vx (if (or (> x width) (< x 0)) (- vx) vx)
           :vy (if (or (> y height) (< y 0)) (- vy) vy))))
(defn ball-setup [_] {:position [100 100] :velocity [2.5 2]})
(defn ball-step [{:keys [position velocity] :as state} {:keys [width height]}]
  (let [[x y :as p] (m/v+ position velocity) [vx vy] velocity]
    (assoc state :position p :velocity
           [(if (or (> x width) (< x 0)) (- vx) vx)
            (if (or (> y height) (< y 0)) (- vy) vy)])))
(defn ball-scene [state _] (d/scene (d/background) (d/circle (:position state) 48)))

(defn vector-scene [mode _ {:keys [width height mouse factor]}]
  (let [center [(/ width 2) (/ height 2)] v (m/v- mouse center)
        result (case mode :multiply (m/v* v factor) :normalize (m/set-mag v 50) v)]
    (d/scene (d/background)
             (when (= mode :subtract)
               (d/scene (d/line [0 0] mouse [190 190 190]) (d/line [0 0] center [190 190 190])))
             (d/line center mouse [190 190 190])
             [[:stroke-weight 3]] (d/line center (m/v+ center result) d/accent)
             (when (= mode :magnitude)
               [[:no-stroke] [:fill 218 57 108] [:rect 10 10 (m/mag v) 10]])
             (d/label (str "向量长度 " (long (m/mag v))) 12 (- height 12)))))

(defn mover-setup [{:keys [width height mode]}]
  {:position (if (= mode :velocity) [(m/random width) (m/random height)] [(/ width 2) (/ height 2)])
   :velocity (if (= mode :velocity) [(m/random -2 2) (m/random -2 2)] [0 0])
   :acceleration [0 0]})
(defn mover-step [mode {:keys [position velocity] :as state} {:keys [width height mouse topspeed acceleration]}]
  (let [a (case mode
            :velocity [0 0]
            :constant [-0.001 0.01]
            :random (m/v* (m/random-vector) (m/random 2))
            :mouse (m/set-mag (m/v- mouse position) acceleration))
        v (m/limit (m/v+ velocity a) topspeed)
        p (m/v+ position v)]
    (assoc state :velocity v :acceleration a :position (if (= mode :mouse) p (m/wrap p width height)))))

(def speed-param {:topspeed {:label "最高速度" :default 5 :min 1 :max 20 :step 0.5}})
(defn vector-example [id title mode]
  {:id id :chapter 1 :title title :width 640 :height 240
   :params (if (= mode :multiply) {:factor {:label "缩放系数" :default 0.5 :min -2 :max 2 :step 0.1}} {})
   :setup (fn [_] {}) :step (fn [s _] s) :scene (partial vector-scene mode)
   :explanation "移动鼠标查看以画布中心为起点的向量；直接用两个数的向量表达坐标与方向。"})
(defn motion-example [id title mode]
  {:id id :chapter 1 :title title :width 640 :height 240
   :params (merge speed-param (when (= mode :mouse)
                               {:acceleration {:label "加速度" :default 0.2 :min 0.01 :max 1 :step 0.01}}))
   :setup #(mover-setup (assoc % :mode mode)) :step (partial mover-step mode) :scene ball-scene
   :explanation "先把加速度加到速度，再把速度加到位置；速度上限与边界规则独立于运动积分。"})
(def examples
  (vec (concat
        [{:id "1.1" :chapter 1 :title "不用向量的弹跳球" :width 640 :height 240 :params {}
          :setup scalar-setup :step scalar-step
          :scene (fn [s e] (ball-scene {:position [(:x s) (:y s)]} e))
          :explanation "四个标量分别保存 x、y 及两个速度分量；触碰边界后反转相应速度。"}
         {:id "1.2" :chapter 1 :title "使用向量的弹跳球" :width 640 :height 240 :params {}
          :setup ball-setup :step ball-step :scene ball-scene
          :explanation "用位置和速度两个二维向量表达相同运动，m/v+ 一次完成两个分量相加。"}]
        (map (fn [[id title mode]] (vector-example id title mode))
             [["1.3" "向量减法" :subtract] ["1.4" "向量乘法" :multiply]
              ["1.5" "向量的模" :magnitude] ["1.6" "向量归一化" :normalize]])
        (map (fn [[id title mode]] (motion-example id title mode))
             [["1.7" "运动基础：速度" :velocity] ["1.8" "运动基础：恒定加速度" :constant]
              ["1.9" "运动基础：随机加速度" :random] ["1.10" "向鼠标加速" :mouse]]))))

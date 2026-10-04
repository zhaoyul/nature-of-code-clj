(ns noc.chapters.extras03
  "Companion oscillation figures, keyboard controls, wave addition and a double pendulum."
  (:require [noc.math :as m] [noc.draw :as d]
            [noc.chapters.ch01 :as motion] [noc.chapters.ch03 :as ch03]
            [noc.chapters.extras00 :as extras]))

(defn spec [i directory title setup step scene params]
  {:id (str "extra-3-" i) :chapter 3 :width 640 :height 240 :title title
   :directory (str "examples/03_oscillation/" directory) :params params
   :source "noc/chapters/extras03.cljc"
   :setup setup :step step :scene scene})

(defn baton-scene [{:keys [angle]} {:keys [width height]}]
  (d/scene (d/background) [[:push] [:translate (/ width 2) (/ height 2)] [:rotate angle] [:stroke-weight 2]]
           (d/line [-50 0] [50 0]) (d/circle [-50 0] 16) (d/circle [50 0] 16) [[:pop]]))
(defn spiral-setup [_] {:radius 0 :angle 0 :points []})
(defn spiral-step [{:keys [radius angle] :as state} {:keys [radial-speed angular-speed width height]}]
  (-> state (update :points #(vec (take-last 4000 (conj % (m/v+ [(/ width 2) (/ height 2)]
                                                               (m/v* (m/from-angle angle) radius))))))
      (update :radius + radial-speed) (update :angle + angular-speed)))
(defn spiral-scene [{:keys [points]} _]
  (d/scene (d/background) [[:no-stroke] [:fill 39 52 69]]
           (mapv (fn [[x y]] [:circle x y 16]) points)))

(defn ship-setup [{:keys [width height]}]
  {:position [(/ width 2) (/ height 2)] :velocity [0 0] :acceleration [0 0] :heading 0 :thrusting? false})
(defn ship-step [{:keys [position velocity acceleration heading] :as state}
                 {:keys [width height key damping topspeed thrust]}]
  (let [v (m/limit (m/v* (m/v+ velocity acceleration) damping) topspeed)
        [x y] (m/v+ position v)
        wrap (fn [x bound] (cond (> x (+ bound 32)) -32 (< x -32) (+ bound 32) :else x))
        heading (+ heading (case key :left -0.03 :right 0.03 0))
        thrusting? (contains? #{:z :Z :up} key)]
    (assoc state :position [(wrap x width) (wrap y height)] :velocity v :heading heading :thrusting? thrusting?
           :acceleration (if thrusting? (m/v* (m/from-angle (- heading (/ m/pi 2))) thrust) [0 0]))))
(defn ship-scene [{[x y] :position :keys [heading thrusting?]} _]
  (d/scene (d/background)
           [[:push] [:translate x (+ y 16)] [:rotate heading] [:stroke-weight 2] [:stroke 39 52 69]
            (if thrusting? [:fill 245 90 40] [:fill 175]) [:rect-mode :center]
            [:rect -8 16 (/ 16 3) 8] [:rect 8 16 (/ 16 3) 8]
            [:fill 175] [:triangle -16 16 0 -16 16 16] [:pop]]
           (d/label "单击画布聚焦；← → 转向，Z 或 ↑ 推进" 12 22)))

(defn wave-step [state {:keys [angular-speed]}] (update state :angle + angular-speed))
(defn wave-scene [{:keys [angle]} {:keys [width height angle-increment diameter]}]
  (d/scene (d/background) [[:stroke 39 52 69] [:stroke-weight 2] [:fill 127 127]]
           (mapv (fn [[i x]] [:circle x (m/map-range (m/sin (+ angle (* i angle-increment))) -1 1 0 height) diameter])
                 (map-indexed vector (range 0 (inc width) 24)))))
(defn object-wave-scene [{:keys [angle]} {:keys [amplitude-scale]}]
  (d/scene (d/background) [[:stroke 39 52 69] [:fill 39 52 69 50]]
           (mapcat (fn [[x y width amplitude period]]
                     (mapv (fn [i]
                             [:circle (+ x (* i 8))
                              (+ y (* amplitude-scale amplitude (m/sin (+ angle (/ (* m/tau i 8) period))))) 48])
                           (range (long (m/floor (/ width 8))))))
                   [[50 75 100 20 600] [300 120 300 40 180]])))
(defn additive-setup [{:keys [count]}]
  {:angle 0 :waves (mapv (fn [_] {:amplitude (m/random 10 30) :period (m/random 100 300)}) (range (long count)))})
(defn additive-scene [{:keys [angle waves]} {:keys [width height]}]
  (d/scene (d/background) [[:stroke 39 52 69] [:fill 39 52 69 100]]
           (mapv (fn [x]
                   [:circle x (+ (/ height 2)
                                  (reduce + (map-indexed
                                             (fn [i {:keys [amplitude period]}]
                                               (* amplitude ((if (even? i) m/sin m/cos)
                                                             (+ angle (/ (* m/tau x) period))))) waves))) 32])
                 (range 0 (+ width 16) 8))))

(defn double-position [{:keys [a1 a2]} {:keys [r1 r2 width]}]
  (let [pivot [(/ width 2) 20]
        p1 (m/v+ pivot [(* r1 (m/sin a1)) (* r1 (m/cos a1))])
        p2 (m/v+ p1 [(* r2 (m/sin a2)) (* r2 (m/cos a2))])]
    [pivot p1 p2]))
(defn double-setup [_] {:a1 (/ m/pi 2) :a2 (/ m/pi 2) :v1 0 :v2 0 :trail []})
(defn double-step [{:keys [a1 a2 v1 v2] :as state} {:keys [r1 r2 m1 m2 gravity] :as env}]
  (let [delta (- a1 a2)
        factor (- (+ (* 2 m1) m2) (* m2 (m/cos (* 2 delta))))
        acc1 (/ (+ (* (- gravity) (+ (* 2 m1) m2) (m/sin a1))
                   (* (- m2) gravity (m/sin (- a1 (* 2 a2))))
                   (* -2 (m/sin delta) m2 (+ (* v2 v2 r2) (* v1 v1 r1 (m/cos delta)))))
                (* r1 factor))
        acc2 (/ (* 2 (m/sin delta)
                   (+ (* v1 v1 r1 (+ m1 m2)) (* gravity (+ m1 m2) (m/cos a1))
                      (* v2 v2 r2 m2 (m/cos delta))))
                (* r2 factor))
        new-v1 (+ v1 acc1) new-v2 (+ v2 acc2)
        state (assoc state :v1 new-v1 :v2 new-v2 :a1 (+ a1 new-v1) :a2 (+ a2 new-v2))
        p2 (nth (double-position state env) 2)]
    (update state :trail #(vec (take-last 4000 (conj % p2))))))
(defn double-scene [{:keys [trail] :as state} {:keys [m1 m2] :as env}]
  (let [[pivot p1 p2] (double-position state env)]
    (d/scene (d/background) [[:no-fill] [:stroke 150 166 154] [:polyline trail false]]
             [[:stroke-weight 2]] (d/line pivot p1) (d/line p1 p2)
             (d/circle p1 m1) (d/circle p2 m2) (d/circle pivot 6 d/teal))))

(def wave-params
  {:angle-increment (extras/parameter "空间角度步长" 0.2 0.01 1 0.01)
   :diameter (extras/parameter "圆的直径" 48 4 60 1)
   :angular-speed (extras/parameter "时间相位步长" 0.02 0 0.1 0.001)})
(defn static-wave [i directory title increment]
  (assoc (spec i directory title (fn [_] {:angle 0}) (fn [s _] s) wave-scene
               (-> wave-params (assoc-in [:angle-increment :default] increment)
                   (assoc-in [:angular-speed :default] 0))) :width 200 :height 200))

(def examples
  [(spec 1 "exercise_3_1_baton" "练习 3.1：旋转指挥棒" ch03/phase-setup ch03/phase-step baton-scene
         {:angular-speed (extras/parameter "角速度" 0.1 0 0.3 0.01)})
   (spec 2 "example_1_10_accelerating_towards_the_mouse" "练习 3.3：朝鼠标加速的圆球" motion/mover-setup (partial motion/mover-step :mouse) motion/ball-scene
         {:topspeed (extras/parameter "最高速度" 5 1 15 0.5) :acceleration (extras/parameter "加速度" 0.2 0.01 1 0.01)})
   (spec 3 "exercise_3_5_spiral" "练习 3.5：极坐标螺旋" spiral-setup spiral-step spiral-scene
         {:radial-speed (extras/parameter "半径增长" 0.05 0 0.5 0.01) :angular-speed (extras/parameter "角速度" 0.01 0.001 0.1 0.001)})
   (spec 4 "exercise_3_6_asteroids" "练习 3.6：小行星飞船" ship-setup ship-step ship-scene
         {:damping (extras/parameter "速度保留系数" 0.995 0.9 1 0.001) :topspeed (extras/parameter "最高速度" 6 1 15 0.5)
          :thrust (extras/parameter "推进力" 0.1 0.01 0.5 0.01)})
   (spec 5 "example_3_5_simple_harmonic_motion" "练习 3.6：简谐运动参照" ch03/phase-setup (fn [s _] s) (partial ch03/oscillator-scene :period)
         {:amplitude (extras/parameter "振幅" 200 10 280 1) :period (extras/parameter "周期" 120 30 360 1)})
   (spec 6 "example_3_9_the_wave" "练习 3.9：运动波参照" ch03/phase-setup wave-step wave-scene wave-params)
   (static-wave 7 "example_3_9_the_wave_a" "图：低空间频率静态波" 0.05)
   (static-wave 8 "example_3_9_the_wave_b" "图：中空间频率静态波" 0.2)
   (static-wave 9 "example_3_9_the_wave_c" "图：高空间频率静态波" 0.6)
   (spec 10 "exercise_3_11_oop_wave" "练习 3.11：两个波对象" ch03/phase-setup wave-step object-wave-scene
         {:angular-speed (extras/parameter "相位步长" 0.02 0 0.1 0.001) :amplitude-scale (extras/parameter "振幅倍率" 1 0 3 0.1)})
   (spec 11 "exercise_3_12_additive_wave" "练习 3.12：正弦和余弦波叠加" additive-setup wave-step additive-scene
         {:count (extras/parameter "波数（重置生效）" 5 1 10 1) :angular-speed (extras/parameter "相位步长" 0.02 0 0.1 0.001)})
   (spec 12 "exercise_3_15_double_pendulum" "练习 3.15：双摆" double-setup double-step double-scene
         {:r1 (extras/parameter "第一摆长" 100 20 110 1) :r2 (extras/parameter "第二摆长" 100 20 110 1)
          :m1 (extras/parameter "第一摆球质量" 10 1 30 1) :m2 (extras/parameter "第二摆球质量" 10 1 30 1)
          :gravity (extras/parameter "重力加速度" 1 0.1 2 0.1)})])

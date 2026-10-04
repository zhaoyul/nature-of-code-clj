(ns noc.chapters.ch03
  (:require [noc.math :as m] [noc.draw :as d]
            [noc.chapters.ch01 :as motion] [noc.chapters.ch02 :as forces]))

(defn angle-setup [_] {:angle 0 :angular-velocity 0})
(defn angle-step [state {:keys [angular-acceleration]}]
  (let [v (+ (:angular-velocity state) angular-acceleration)]
    (assoc state :angular-velocity v :angle (+ (:angle state) v))))
(defn angle-scene [{:keys [angle]} {:keys [width height]}]
  (d/scene (d/background)
           [[:push] [:translate (/ width 2) (/ height 2)] [:rotate angle]]
           (d/line [-60 0] [60 0]) (d/circle [-60 0] 16) (d/circle [60 0] 16)
           [[:pop]]))

(defn angular-movers-setup [env]
  (update (forces/attraction-setup :many env) :bodies
          #(mapv (fn [b] (assoc b :angle 0 :angular-velocity 0)) %)))
(defn angular-movers-step [state {:keys [G] :as env}]
  (let [next-state (forces/attraction-step :many state env)]
    (assoc next-state :bodies
           (mapv (fn [before after]
                   (let [a (m/vdiv (forces/attraction-force (:attractor state) before G) (:mass before))
                         av (m/clamp (+ (:angular-velocity before) (/ (first a) 10)) -0.1 0.1)]
                     (assoc after :angle (+ (:angle before) av) :angular-velocity av)))
                 (:bodies state) (:bodies next-state)))))
(defn angular-movers-scene [{:keys [bodies attractor]} _]
  (d/scene (d/background) (d/circle (:position attractor) 40 d/teal)
           (mapcat (fn [{:keys [position radius angle]}]
                     (d/scene [[:push] (into [:translate] position) [:rotate angle]]
                              (d/circle [0 0] (* radius 2) [218 57 108 130])
                              (d/line [0 0] [radius 0]) [[:pop]])) bodies)))

(defn orient-scene [{:keys [position velocity]} _]
  (d/scene (d/background)
           [[:push] (into [:translate] position) [:rotate (m/heading velocity)]
            [:stroke 39 52 69] [:fill 218 57 108] [:rect -15 -5 30 10] [:pop]]))
(defn phase-setup [_] {:angle 0})
(defn phase-step [state {:keys [angular-speed]}] (update state :angle + angular-speed))
(defn oscillator-point [mode state {:keys [width height radius amplitude period frame]}]
  (m/v+ [(/ width 2) (/ height 2)]
        (case mode
          :polar (m/v* (m/from-angle (:angle state)) radius)
          :period [(* amplitude (m/sin (/ (* m/tau frame) period))) 0]
          :phase [(* amplitude (m/sin (:angle state))) 0])))
(defn oscillator-scene [mode state {:keys [width height] :as env}]
  (let [center [(/ width 2) (/ height 2)] p (oscillator-point mode state env)]
    (d/scene (d/background) (d/line center p) (d/circle p 48))))

(defn oscillators-setup [{:keys [width height count]}]
  {:oscillators (mapv (fn [_] {:angle [0 0] :velocity [(m/random -0.05 0.05) (m/random -0.05 0.05)]
                              :amplitude [(m/random 20 (/ width 2)) (m/random 20 (/ height 2))]})
                     (range (long count)))})
(defn oscillators-step [state _]
  (update state :oscillators #(mapv (fn [o] (update o :angle m/v+ (:velocity o))) %)))
(defn oscillators-scene [{:keys [oscillators]} {:keys [width height]}]
  (let [center [(/ width 2) (/ height 2)]]
    (d/scene (d/background)
             (mapcat (fn [{[ax ay] :angle [ampx ampy] :amplitude}]
                       (let [p (m/v+ center [(* ampx (m/sin ax)) (* ampy (m/sin ay))])]
                         (d/scene (d/line center p [120 140 150]) (d/circle p 32 [218 57 108 160])))) oscillators))))

(defn wave-scene [animated? {:keys [angle]} {:keys [width height amplitude wavelength]}]
  (d/scene (d/background)
           (mapcat (fn [x]
                     (d/circle [x (+ (/ height 2) (* amplitude (m/sin (+ (if animated? angle 0) (/ (* m/tau x) wavelength)))))]
                               36 [218 57 108 120]))
                   (range 0 (inc width) 24))))

(defn spring-setup [{:keys [width rest-length]}]
  {:anchor [(/ width 2) 10] :position [(/ width 2) (+ 10 rest-length)]
   :velocity [0 0] :dragging? false})
(defn spring-step [{:keys [position velocity anchor dragging?] :as state}
                   {:keys [mouse pressed? clicked? stiffness rest-length damping mass gravity]}]
  (let [drag? (and pressed? (or dragging? (and clicked? (< (m/distance position mouse) 24))))]
    (if drag?
      (assoc state :position mouse :velocity [0 0] :dragging? true)
      (let [delta (m/v- position anchor)
            force (m/v+ [0 gravity] (m/set-mag delta (* (- stiffness) (- (m/mag delta) rest-length))))
            v (m/v* (m/v+ velocity (m/vdiv force mass)) damping)
            p (m/v+ position v)
            len (m/distance p anchor) clipped? (or (< len 30) (> len 200))]
        (assoc state :position (if clipped? (m/v+ anchor (m/set-mag (m/v- p anchor) (m/clamp len 30 200))) p)
               :velocity (if clipped? [0 0] v) :dragging? false)))))
(defn spring-scene [{:keys [anchor position]} _]
  (d/scene (d/background) (d/line anchor position) (d/circle anchor 10 d/teal)
           (d/circle position 48) (d/label "拖动圆球拉伸弹簧")))

(defn pendulum-setup [{:keys [width]}]
  {:pivot [(/ width 2) 10] :angle (/ m/pi 4) :angular-velocity 0 :dragging? false})
(defn pendulum-position [{:keys [pivot angle]} length]
  (m/v+ pivot [(* length (m/sin angle)) (* length (m/cos angle))]))
(defn pendulum-step [{:keys [pivot angle angular-velocity dragging?] :as state}
                     {:keys [mouse pressed? clicked? length gravity damping]}]
  (let [drag? (and pressed? (or dragging? (and clicked? (< (m/distance mouse (pendulum-position state length)) 24))))]
    (if drag?
      (let [[dx dy] (m/v- mouse pivot)]
        (assoc state :angle (m/atan2 dx dy) :angular-velocity 0 :dragging? true))
      (let [v (+ angular-velocity (* (- (/ gravity length)) (m/sin angle)))]
        (assoc state :angle (+ angle v) :angular-velocity (* v damping) :dragging? false)))))
(defn pendulum-scene [state {:keys [length]}]
  (let [bob (pendulum-position state length)]
    (d/scene (d/background) (d/line (:pivot state) bob) (d/circle bob 48)
             (d/circle (:pivot state) 10 d/teal) (d/label "拖动摆球改变初始角度"))))

(defn spec [id title setup step scene params]
  {:id id :chapter 3 :title title :width 640 :height 360 :params params
   :setup setup :step step :scene scene
   :explanation "角位置、角速度与角加速度遵循与线性运动相同的更新顺序；绘图由纯数据转换为 Quil 指令。"})
(def examples
  [(spec "3.1" "用 rotate 实现角运动" angle-setup angle-step angle-scene
         {:angular-acceleration {:label "角加速度" :default 0.0001 :min 0 :max 0.001 :step 0.0001}})
   (spec "3.2" "力与任意角运动" angular-movers-setup angular-movers-step angular-movers-scene
         {:G {:label "引力常数" :default 1 :min 0 :max 4 :step 0.1}
          :count {:label "数量（重置生效）" :default 20 :min 1 :max 40 :step 1}})
   (spec "3.3" "朝向运动方向" motion/mover-setup (partial motion/mover-step :mouse) orient-scene
         {:topspeed {:label "最高速度" :default 4 :min 1 :max 12 :step 0.5}
          :acceleration {:label "加速度" :default 0.5 :min 0.1 :max 1 :step 0.1}})
   (spec "3.4" "极坐标转直角坐标" phase-setup phase-step (partial oscillator-scene :polar)
         {:radius {:label "半径" :default 130 :min 20 :max 170 :step 1}
          :angular-speed {:label "角速度" :default 0.02 :min 0.001 :max 0.1 :step 0.001}})
   (spec "3.5" "简谐运动 I：周期" phase-setup (fn [s _] s) (partial oscillator-scene :period)
         {:amplitude {:label "振幅" :default 200 :min 10 :max 280 :step 1}
          :period {:label "周期（帧）" :default 120 :min 30 :max 360 :step 1}})
   (spec "3.6" "简谐运动 II：相位" phase-setup phase-step (partial oscillator-scene :phase)
         {:amplitude {:label "振幅" :default 200 :min 10 :max 280 :step 1}
          :angular-speed {:label "角速度" :default 0.05 :min 0.005 :max 0.2 :step 0.005}})
   (spec "3.7" "振荡器对象" oscillators-setup oscillators-step oscillators-scene
         {:count {:label "数量（重置生效）" :default 10 :min 1 :max 25 :step 1}})
   (spec "3.8" "静态波" phase-setup (fn [s _] s) (partial wave-scene false)
         {:amplitude {:label "振幅" :default 100 :min 10 :max 150 :step 1}
          :wavelength {:label "波长" :default 750 :min 100 :max 1000 :step 10}})
   (spec "3.9" "运动的波" phase-setup phase-step (partial wave-scene true)
         {:amplitude {:label "振幅" :default 150 :min 10 :max 150 :step 1}
          :wavelength {:label "波长" :default 750 :min 100 :max 1000 :step 10}
          :angular-speed {:label "相位步长" :default 0.02 :min 0.001 :max 0.1 :step 0.001}})
   (spec "3.10" "弹簧连接" spring-setup spring-step spring-scene
         {:stiffness {:label "弹簧常数" :default 0.2 :min 0.01 :max 1 :step 0.01}
          :rest-length {:label "原长" :default 100 :min 40 :max 180 :step 1}
          :damping {:label "阻尼" :default 0.98 :min 0.9 :max 1 :step 0.005}
          :mass {:label "质量" :default 24 :min 1 :max 50 :step 1}
          :gravity {:label "重力" :default 2 :min 0 :max 5 :step 0.1}})
   (spec "3.11" "摆动的单摆" pendulum-setup pendulum-step pendulum-scene
         {:length {:label "摆长" :default 175 :min 40 :max 280 :step 1}
          :gravity {:label "重力加速度" :default 0.4 :min 0.05 :max 2 :step 0.05}
          :damping {:label "阻尼" :default 0.995 :min 0.98 :max 1 :step 0.001}})])

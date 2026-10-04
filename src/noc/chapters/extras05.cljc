(ns noc.chapters.extras05
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.ch05 :as steering]))

(defn extra [n dir title setup step scene params explanation]
  {:id (str "extra-5-" n) :chapter 5 :directory (str "examples/05_steering/" dir)
   :title title :width 640 :height 360 :setup setup :step step :scene scene :params params
   :source "noc/chapters/extras05.cljc" :explanation explanation})
(def common-params
  {:max-speed {:label "最大速度" :default 4 :min 1 :max 10 :step 0.5}
   :max-force {:label "最大转向力" :default 0.15 :min 0.01 :max 0.5 :step 0.01}})
(defn pursuit-setup [_]
  {:pursuer (assoc (steering/vehicle 0 [100 180]) :velocity [0 0])
   :target (assoc (steering/vehicle 1 [400 180]) :velocity (m/v* (m/random-vector) 3))})
(defn pursuit-step [{:keys [pursuer target] :as state} {:keys [width height prediction max-speed max-force] :as env}]
  (let [target (merge target (m/bounce (m/v+ (:position target) (:velocity target)) (:velocity target) width height 16))
        predicted (m/v+ (:position target) (m/v* (:velocity target) prediction))]
    (assoc state :target target :predicted predicted
           :pursuer (steering/move-vehicle pursuer (steering/seek pursuer predicted max-speed max-force) env))))
(defn pursuit-scene [{:keys [pursuer target predicted]} _]
  (d/scene (d/background) (steering/vehicle-scene pursuer)
           (d/circle (:position target) 32 d/teal)
           (when predicted (d/scene (d/line (:position target) predicted [140 140 140]) (d/circle predicted 12)))
           (d/label "寻找目标未来的位置")))

(defn wander-setup [_] {:vehicle (steering/vehicle 0 [320 180]) :angle 0})
(defn wander-step [{:keys [vehicle angle] :as state} {:keys [jitter max-speed max-force] :as env}]
  (let [angle (+ angle (m/random (- jitter) jitter))
        center (m/v+ (:position vehicle) (m/set-mag (:velocity vehicle) 80))
        target (m/v+ center (m/v* (m/from-angle (+ angle (m/heading (:velocity vehicle)))) 25))]
    (assoc state :angle angle :center center :target target
           :vehicle (steering/move-vehicle vehicle (steering/seek vehicle target max-speed max-force) env))))
(defn wander-scene [{:keys [vehicle center target]} _]
  (d/scene (d/background) (steering/vehicle-scene vehicle)
           (when center (d/scene [[:no-fill] [:stroke 100 130 140] [:circle (first center) (second center) 50]]
                                (d/line (:position vehicle) center) (d/line center target) (d/circle target 6)))
           (d/label "预测圆上的随机目标产生连续漫游")))

(defn angle-scene [_ {:keys [width height mouse]}]
  (let [origin [(/ width 2) (/ height 2)] a [120 0] b (m/v- mouse origin)
        cosine (/ (m/dot a b) (max 1.0e-9 (* (m/mag a) (m/mag b))))
        angle (#?(:clj Math/acos :cljs js/Math.acos) (m/clamp cosine -1 1))]
    (d/scene (d/background) (d/line origin (m/v+ origin a) d/teal) (d/line origin mouse d/accent)
             [[:no-fill] [:stroke 39 52 69] [:arc (first origin) (second origin) 70 70 0 (m/heading b)]]
             (d/label (str "点积给出夹角 " (/ (long (* 100 angle (/ 180 m/pi))) 100.0) "°")))))

(defn crowd-setup [{:keys [width height population]}]
  {:vehicles (mapv #(steering/vehicle % [(m/random width) (m/random height)]) (range (long population)))
   :path [[30 30] [610 30] [610 330] [320 270] [30 330] [30 30]]})
(defn crowd-step [{:keys [vehicles path] :as state} {:keys [max-speed max-force separation-weight mouse clicked?] :as env}]
  (let [vehicles (if clicked? (conj vehicles (steering/vehicle (count vehicles) mouse)) vehicles)]
    (assoc state :vehicles
           (mapv (fn [v]
                   (let [path-target (steering/path-target v path)
                         force (m/v+ (steering/seek v (:target path-target) max-speed max-force)
                                     (m/v* (steering/separation v vehicles max-speed max-force 25) separation-weight))]
                     (steering/move-vehicle v force env))) vehicles))))
(defn crowd-scene [{:keys [path vehicles]} _]
  (d/scene (d/background) [[:stroke 220 224 221] [:stroke-weight 30] [:no-fill] [:polyline path false]
                          [:stroke 39 52 69] [:stroke-weight 1] [:polyline path false]]
           (mapcat steering/vehicle-scene vehicles) (d/label "路径跟随 + 分离；点击添加载具")))
(def examples
  [(extra 1 "exercise_5_2_pursuit" "练习 5.3：追逐移动目标" pursuit-setup pursuit-step pursuit-scene
          (assoc common-params :prediction {:label "预测帧数" :default 10 :min 0 :max 40 :step 1})
          "把目标的当前位置加上速度乘预测时间，再计算寻找这个未来位置的转向力。")
   (extra 2 "exercise_5_4_wander" "练习 5.4：漫游" wander-setup wander-step wander-scene
          (assoc common-params :jitter {:label "方向扰动" :default 0.3 :min 0 :max 1 :step 0.01})
          "在载具前方建立预测圆，随机改变圆上的角度，得到连续变化的目标。")
   (extra 3 "exercise_5_9_angle_between" "练习 5.9：向量夹角" (fn [_] {}) (fn [s _] s) angle-scene {}
          "由 a·b = |a||b|cosθ 求夹角；零长度向量单独处理，反余弦输入限制在 [-1,1]。")
   (extra 4 "exercise_5_13_crowd_path_following" "练习 5.13：人群路径跟随" crowd-setup crowd-step crowd-scene
          (merge common-params {:population {:label "载具数量（重置）" :default 80 :min 10 :max 150 :step 10}
                                :separation-weight {:label "分离权重" :default 1.5 :min 0 :max 3 :step 0.1}})
          "循环折线路径上的预测跟随力和局部避让力相加；所有载具读取同一个旧状态，避免遍历顺序影响。")])

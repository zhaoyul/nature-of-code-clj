(ns noc.chapters.extras00
  "Executable companion figures and solved exercises from the randomness chapter."
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.ch00 :as ch00]))

(defn parameter [label default lo hi step]
  {:label label :default default :min lo :max hi :step step})
(defn spec [i directory title setup step scene params]
  {:id (str "extra-0-" i) :chapter 0 :width 640 :height 240 :title title
   :directory (str "examples/00_randomness/" directory) :params params
   :source "noc/chapters/extras00.cljc"
   :setup setup :step step :scene scene})

(defn record-position [state p]
  (assoc state :position p :trail (vec (take-last 4000 (conj (:trail state) p)))))
(defn signed [x] (* x (m/choose [-1 1])))
(defn quadratic-sample []
  (loop [] (let [x (m/random)] (if (< (m/random) (* x x)) x (recur)))))
(defn walking-step [state env kind]
  (let [[x y :as position] (:position state)
        [mx my] (:mouse env)
        delta (case kind
                :skew [(m/random -2.75 3) (m/random -2.75 3)]
                :mouse (let [r (m/random)]
                         (cond (< r 0.25) [(if (< x mx) 1 -1) 0]
                               (< r 0.5) [0 (if (< y my) 1 -1)]
                               :else (m/choose [[1 0] [-1 0] [0 1] [0 -1]])))
                :gaussian [(m/gaussian 0 (:sd env)) (m/gaussian 0 (:sd env))]
                :quadratic [(signed (* (:step-size env) (quadratic-sample)))
                            (signed (* (:step-size env) (quadratic-sample)))]
                :noise [(m/map-range (m/noise (:t state)) 0 1 -1 1)
                        (m/map-range (m/noise (+ (:t state) 10000)) 0 1 -1 1)])]
    (-> state (record-position (m/v+ position delta)) (update :t + 0.01))))

(defn bell-scene [_ {:keys [width height sd]}]
  (d/scene (d/background) [[:stroke-weight 2] [:no-fill] [:stroke 39 52 69]
                          [:polyline (mapv (fn [i]
                                             (let [x (m/map-range i 0 width -3 3)
                                                   density (/ (m/exp (- (/ (* x x) (* sd sd))))
                                                              (* sd (m/sqrt (* 2 m/pi))))]
                                               [i (m/map-range density 0 1 (- height 2) 2)]))
                                           (range (inc width))) false]]))

(defn hsv [h s v]
  (let [h (/ (mod h 360) 60) s (m/clamp (/ s 100) 0 1) v (m/clamp (/ v 100) 0 1)
        c (* v s) x (* c (- 1 (m/abs (- (mod h 2) 1)))) a (- v c)
        rgb (case (long (m/floor h)) 0 [c x 0] 1 [x c 0] 2 [0 c x] 3 [0 x c] 4 [x 0 c] [c 0 x])]
    (mapv #(long (* 255 (+ a %))) rgb)))
(defn splatter-step [state {:keys [width height spread size size-spread hue hue-spread alpha]}]
  (let [spot {:position [(m/gaussian (/ width 2) (* (/ height 2) spread))
                         (m/gaussian (/ height 2) (* (/ height 2) spread))]
              :size (max 0.1 (m/gaussian (/ size 2) (* (/ height 2) size-spread)))
              :color (conj (hsv (m/gaussian hue hue-spread) (m/gaussian 80 20) (m/gaussian 80 20))
                           (* 255 alpha))}]
    (update state :spots #(vec (take-last 2000 (conj % spot))))))
(defn splatter-scene [{:keys [spots]} _]
  (d/scene (d/background) [[:no-stroke]]
           (mapcat (fn [{[x y] :position :keys [size color]}]
                     [(into [:fill] color) [:circle x y size]]) spots)))

;; Three-dimensional gradient Perlin noise, evaluated identically by Clojure
;; and ClojureScript. A quintic fade preserves derivatives at cell boundaries.
(defn fade [t] (* t t t (+ (* t (- (* t 6) 15)) 10)))
(def gradients [[1 1 0] [-1 1 0] [1 -1 0] [-1 -1 0]
                [1 0 1] [-1 0 1] [1 0 -1] [-1 0 -1]
                [0 1 1] [0 -1 1] [0 1 -1] [0 -1 -1]])
(defn noise3 [x y z]
  (let [ix (long (m/floor x)) iy (long (m/floor y)) iz (long (m/floor z))
        tx (- x ix) ty (- y iy) tz (- z iz)
        corner (fn [a b c]
                 (let [h (mod (mod (* 48271 (+ (* (+ ix a) 1619) (* (+ iy b) 31337)
                                                  (* (+ iz c) 6971) 1013)) 2147483647) 12)
                       [gx gy gz] (nth gradients h)]
                   (+ (* gx (- tx a)) (* gy (- ty b)) (* gz (- tz c)))))
        u (fade tx) v (fade ty) w (fade tz)
        plane (fn [c] (m/lerp (m/lerp (corner 0 0 c) (corner 1 0 c) u)
                              (m/lerp (corner 0 1 c) (corner 1 1 c) u) v))]
    (m/clamp (+ 0.5 (* 0.5 (m/lerp (plane 0) (plane 1) w))) 0 1)))
(defn fractal-noise [x y z octaves falloff]
  (loop [i 0 frequency 1.0 weight 1.0 value 0.0 total 0.0]
    (if (>= i (long octaves)) (/ value (max total 1.0e-12))
        (recur (inc i) (* frequency 2) (* weight falloff)
               (+ value (* weight (noise3 (* x frequency) (* y frequency) (* z frequency))))
               (+ total weight)))))
(defn noise-step [state {:keys [noise-speed clicked?]}]
  (cond-> (update state :t + noise-speed)
    clicked? (assoc :offset (m/random 1000))))
(defn noise-grid-scene [{:keys [t offset]} {:keys [width height pixel-step xoff yoff octaves falloff colored]}]
  (d/scene (d/background) [[:no-stroke]]
           (mapcat (fn [[x y]]
                     (let [value (fractal-noise (+ offset (* x xoff)) (* y yoff) t octaves falloff)
                           color (if (pos? colored) (hsv (* 360 value) 100 (* 100 value))
                                     (vec (repeat 3 (* 255 value))))]
                       [(into [:fill] color) [:rect x y pixel-step pixel-step]]))
                   (for [y (range 0 height (max 1 (long pixel-step)))
                         x (range 0 width (max 1 (long pixel-step)))] [x y]))))
(def noise-params
  {:pixel-step (parameter "采样像素（1 为逐像素）" 4 1 8 1)
   :xoff (parameter "横向噪声步长" 0.01 0.01 0.1 0.01)
   :yoff (parameter "纵向噪声步长" 0.01 0.01 0.1 0.01)
   :octaves (parameter "倍频层数" 4 1 10 1)
   :falloff (parameter "倍频衰减" 0.5 0 1 0.05)
   :colored (parameter "彩色噪声" 1 0 1 1)
   :noise-speed (parameter "时间步长" 0 0 0.05 0.001)})
(defn graph-setup [{:keys [width height]}]
  {:t 0 :values (mapv (fn [_] (m/random height)) (range width))})
(defn graph-scene [{:keys [t values]} {:keys [width height random-graph]}]
  (d/scene (d/background) [[:no-fill] [:stroke-weight 2] [:stroke 39 52 69]
                          [:polyline (mapv (fn [x]
                                             [x (if (pos? random-graph)
                                                  (nth values (mod (+ x (long (* 100 t))) (count values)))
                                                  (* height (m/noise (+ t (* x 0.01)))))])
                                           (range width)) false]]))

(defn terrain-setup [_] {:t 0 :theta 0})
(defn terrain-step [state {:keys [noise-speed rotation-speed pressed? mouse]}]
  (-> state (update :t + noise-speed)
      (update :theta #(if pressed? (* 0.01 (first mouse)) (+ % rotation-speed)))))
(defn terrain-scene [{:keys [t theta]} {:keys [width height elevation]}]
  (let [project (fn [x y]
                  (let [z (* elevation (- (* 2 (noise3 (* x 0.1) (* y 0.1) t)) 1))
                        [a b] (m/rotate [(- (* x 20) 400) (- (* y 20) 200)] theta)
                        scale (/ width 1100.0)]
                    [(+ (/ width 2) (* scale a))
                     (+ (* height 0.55) (* scale (- (* b 0.5) (* z 0.866))))]))]
    (d/scene (d/background) [[:stroke 39 52 69] [:stroke-weight 0.6]]
             (mapcat (fn [[x y]]
                       (let [shade (m/map-range (noise3 (* x 0.1) (* y 0.1) t) 0 1 40 230)]
                         [[:fill shade] [:polygon [(project x y) (project (inc x) y)
                                                  (project (inc x) (inc y)) (project x (inc y))]]]))
                     (for [y (range 19) x (range 39)] [x y]))
             (d/label "三维高度场的投影；按住鼠标拖动以转动地形" 12 22))))

(defn tree-node [length]
  {:length length :children (when (> (* length 0.7) 4)
                             (mapv (fn [_] (tree-node (* length 0.7)))
                                   (range (inc (m/random-int 4)))))})
(defn tree-setup [_] {:t 0 :root (tree-node 60)})
(defn tree-step [state {:keys [clicked?]}]
  (cond-> (update state :t + 0.005) clicked? (assoc :root (tree-node 60) :t (m/random 1000))))
(defn tree-lines [node position angle xoff t]
  (let [length (:length node) end (m/v+ position (m/v* (m/from-angle angle) length))
        children (:children node) n (count children)]
    (concat [[:stroke-weight (m/map-range length 2 100 1 5)]
             (into [:line] (concat position end))]
            (mapcat (fn [[i child]]
                      (let [delta (m/map-range (m/noise (+ xoff i) t) 0 1 (- (/ m/pi 2)) (/ m/pi 2))]
                        (tree-lines child end (+ angle (* (if (even? n) -1 1) delta)) (+ xoff 0.1) t)))
                    (map-indexed vector children)))))
(defn tree-scene [{:keys [root t]} {:keys [width height]}]
  (d/scene (d/background) [[:stroke 39 52 69]]
           (tree-lines root [(/ width 2) height] (- (/ m/pi 2)) 0 t)))

(defn flow-setup [{:keys [width height count]}]
  {:offset (m/random 1000)
   :vehicles (mapv (fn [_] {:position [(m/random width) (m/random height)] :velocity [0 0]
                            :max-speed (m/random 2 5) :max-force (m/random 0.1 0.5)}) (range (long count)))})
(defn field-vector [offset [x y]]
  (m/from-angle (* m/tau (m/noise (+ offset (* (m/floor (/ x 20)) 0.1))
                                 (* (m/floor (/ y 20)) 0.1)))))
(defn flow-step [state {:keys [width height clicked?]}]
  (let [offset (if clicked? (m/random 1000) (:offset state))]
    (assoc state :offset offset
           :vehicles (mapv (fn [{:keys [position velocity max-speed max-force] :as vehicle}]
                             (let [force (m/limit (m/v- (m/v* (field-vector offset position) max-speed) velocity) max-force)
                                   v (m/limit (m/v+ velocity force) max-speed)]
                               (assoc vehicle :position (m/wrap (m/v+ position v) width height) :velocity v)))
                           (:vehicles state)))))
(defn flow-scene [{:keys [offset vehicles]} {:keys [width height debug]}]
  (d/scene (d/background)
           (when (pos? debug)
             (mapcat (fn [p] (d/arrow p (m/v* (field-vector offset p) 18) [175 183 174]))
                     (for [y (range 0 height 20) x (range 0 width 20)] [x y])))
           (mapcat (fn [{:keys [position velocity]}]
                     [[:push] (into [:translate] position) [:rotate (+ (m/heading velocity) (/ m/pi 2))]
                      [:fill 90 130 110] [:stroke 39 52 69] [:triangle 0 -8 -4 8 4 8] [:pop]]) vehicles)))

(def examples
  [(spec 1 "exercise_0_1_solution_skewed_random_walker" "练习 0.1：偏斜随机游走" ch00/walker-setup #(walking-step %1 %2 :skew) ch00/walker-scene {})
   (spec 2 "exercise_0_3_solution_random_walk_towards_mouse" "练习 0.3：朝鼠标随机游走" ch00/walker-setup #(walking-step %1 %2 :mouse) ch00/walker-scene {})
   (spec 3 "figure_0_2_bell_curve_low" "图 0.2：低标准差钟形曲线" (fn [_] {}) (fn [s _] s) bell-scene {:sd (parameter "标准差" 0.41 0.1 2 0.01)})
   (spec 4 "figure_0_2_bell_curve_high" "图 0.2：高标准差钟形曲线" (fn [_] {}) (fn [s _] s) bell-scene {:sd (parameter "标准差" 1.5 0.1 2 0.01)})
   (spec 5 "exercise_0_4_solution_paint_splatter" "练习 0.4：高斯泼墨" (fn [_] {:spots []}) splatter-step splatter-scene
         {:spread (parameter "位置分散程度" 0.25 0 0.75 0.01) :size (parameter "尺寸" 20 5 50 1)
          :size-spread (parameter "尺寸标准差" 0.01 0 0.1 0.005) :hue (parameter "色相" 250 0 360 1)
          :hue-spread (parameter "色相标准差" 15 0 100 1) :alpha (parameter "不透明度" 0.75 0 1 0.05)})
   (spec 6 "exercise_0_5_solution_gaussian_random_walker" "练习 0.5：高斯步长游走" ch00/walker-setup #(walking-step %1 %2 :gaussian) ch00/walker-scene {:sd (parameter "步长标准差" 3 0.1 10 0.1)})
   (spec 7 "exercise_0_6_solution_quadratic_random_walker" "练习 0.6：平方接受拒绝游走" ch00/walker-setup #(walking-step %1 %2 :quadratic) ch00/walker-scene {:step-size (parameter "最大步长" 5 1 15 1)})
   (assoc (spec 8 "figure_i_4_noise" "图：连续噪声与时间" graph-setup (fn [s _] (update s :t + 0.01)) graph-scene {:random-graph (parameter "独立随机值" 0 0 1 1)}) :width 360)
   (assoc (spec 9 "figure_i_5_random" "图：独立随机值与时间" graph-setup (fn [s _] (update s :t + 0.01)) graph-scene {:random-graph (parameter "独立随机值" 1 0 1 1)}) :width 360)
   (spec 10 "exercise_0_7_solution_perlin_noise_walker" "练习 0.7：连续噪声步长" ch00/walker-setup #(walking-step %1 %2 :noise) ch00/walker-scene {})
   (spec 11 "figure_i_noise_2_d" "图：二维噪声像素场" (fn [_] {:t 0 :offset 0}) noise-step noise-grid-scene
         (assoc-in noise-params [:colored :default] 0))
   (assoc (spec 12 "exercise_0_8_solution_2_d_noise_parameterized" "练习 0.8：参数化二维噪声" (fn [_] {:t 0 :offset 0}) noise-step noise-grid-scene noise-params) :width 320 :height 120)
   (assoc (spec 13 "exercise_0_9_solution_2_d_noise_animated" "练习 0.9：三维噪声动画" (fn [_] {:t 0 :offset 0}) noise-step noise-grid-scene
                (assoc-in noise-params [:noise-speed :default] 0.01)) :width 320 :height 120)
   (spec 14 "exercise_i_10_noise_terrain" "练习 0.10：噪声地形" terrain-setup terrain-step terrain-scene
         {:noise-speed (parameter "噪声时间步长" 0.01 0 0.05 0.001) :rotation-speed (parameter "旋转速度" 0.0025 0 0.02 0.0005)
          :elevation (parameter "地形高度" 120 0 200 1)})
   (assoc (spec 15 "figure_i_11_tree_stochastic_noise" "图：随机树与噪声摆动" tree-setup tree-step tree-scene {}) :width 360)
   (assoc (spec 16 "figure_i_12_flow_field_with_perlin_noise" "图：噪声流场" flow-setup flow-step flow-scene
                {:count (parameter "车辆数（重置生效）" 120 1 180 1) :debug (parameter "显示流场" 1 0 1 1)}) :width 360)])

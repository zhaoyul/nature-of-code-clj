(ns noc.chapters.extras04
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.ch04 :as particles]))

(defn base [id] (some #(when (= id (:id %)) %) particles/examples))
(defn extra [n directory title spec overrides]
  (merge spec {:id (str "extra-4-" n) :chapter 4 :title title
               :directory (str "examples/04_particles/" directory)
               :source "noc/chapters/extras04.cljc"} overrides))

(defn ship-setup [{:keys [width height]}]
  {:position [(/ width 2) (/ height 2)] :velocity [0 0] :heading 0 :particles []})
(defn ship-step [{:keys [position velocity heading] :as state} {:keys [width height key thrust]}]
  (let [heading (+ heading (case key :left -0.03 :right 0.03 0))
        thrust? (= key :up) direction (m/from-angle (- heading (/ m/pi 2)))
        velocity (m/limit (m/v* (m/v+ velocity (if thrust? (m/v* direction thrust) [0 0])) 0.995) 6)
        newborn (when thrust?
                  [{:position position :velocity (m/v+ (m/random-vector) (m/v* direction -5)) :lifespan 255 :angle 0}])]
    (assoc state :position (m/wrap (m/v+ position velocity) width height) :velocity velocity
           :heading heading :thrusting? thrust?
           :particles (vec (filter #(pos? (:lifespan %))
                                   (map #(particles/update-particle % [0 0]) (concat (:particles state) newborn)))))))
(defn ship-scene [{:keys [position heading particles thrusting?]} _]
  (d/scene (d/background) (mapcat #(d/circle (:position %) 8 [218 57 108 (:lifespan %)]) particles)
           [[:push] (into [:translate] position) [:rotate heading] [:stroke 39 52 69]
            (into [:fill] (if thrusting? d/accent d/teal)) [:triangle -16 16 0 -16 16 16] [:pop]]
           (d/label "点击画布获得键盘焦点；← → 转向，↑ 推进并发射尾焰")))

(defn shatter-setup [_]
  {:particles (vec (for [y (range 10) x (range 10)]
                     {:position [(+ 270 (* 10 x)) (+ 70 (* 10 y))] :velocity [0 0] :lifespan 255 :size 10}))})
(defn shatter-step [state {:keys [clicked? impulse]}]
  (update state :particles
          #(mapv (fn [p]
                   (let [v (m/v+ (:velocity p) (if clicked? (m/v* (m/random-vector) impulse) [0 0]))]
                     (assoc p :position (m/v+ (:position p) v) :velocity (m/v* v 0.95)))) %)))
(defn shatter-scene [{:keys [particles]} _]
  (d/scene (d/background) [[:no-stroke] [:fill 218 57 108]]
           (map (fn [{[x y] :position size :size}] [:rect (- x (/ size 2)) (- y (/ size 2)) size size]) particles)
           (d/label "点击使组成方块的 100 个小粒子碎裂")))

(defn smoke-circle-scene [state {:keys [width height mouse wind]}]
  (d/scene [[:background 0] [:no-stroke]]
           (mapcat #(d/circle (:position %) 24 [255 255 255 (:lifespan %)]) (:particles state))
           (d/arrow [(/ width 2) 40] [(* 500 wind (m/map-range (first mouse) 0 width -1 1)) 0] [255 255 255])
           (d/label "圆形烟雾与图像纹理的对照" 10 (- height 10))))

(def texture-paths ["images/emitter.png" "images/array-particle.png" "images/reflection.png"])
(defn texture-array-step [state {:keys [mouse emission]}]
  (let [newborn (repeatedly (long emission)
                           #(hash-map :position mouse :velocity (m/random-vector) :lifespan 255 :angle 0 :texture (m/choose texture-paths)))]
    (update state :particles
            (fn [ps]
              (vec (filter #(pos? (:lifespan %))
                           (map (fn [p] (-> (particles/update-particle p [0 -0.2]) (update :lifespan - 3)))
                                (concat ps newborn))))))))
(defn texture-array-scene [{:keys [particles]} _]
  (d/scene [[:blend-mode :blend] [:background 0] [:blend-mode :add]]
           (mapcat (fn [{[x y] :position lifespan :lifespan texture :texture}]
                     [[:tint lifespan] [:image texture (- x 16) (- y 16) 32 32] [:no-tint]]) particles)
           [[:blend-mode :blend]]))

(defn emitters-setup [origins env] (assoc (particles/setup :systems env) :emitters origins))
(def examples
  [(extra 1 "exercise_4_4_asteroids" "练习 4.4：飞船的粒子尾焰" (base "4.3")
          {:setup ship-setup :step ship-step :scene ship-scene
           :params {:thrust {:label "推进力" :default 0.1 :min 0.02 :max 0.5 :step 0.01}}
           :explanation "飞船朝向决定推力方向；反方向发射带寿命的尾焰粒子，速度阻尼和边界回绕与飞船共享。"})
   (extra 2 "4_4_multiple_emitters_0" "多个发射器：从空系统开始" (base "4.4")
          {:setup (partial emitters-setup []) :explanation "开始时没有发射器，点击画布添加独立粒子发射位置。"})
   (extra 3 "4_4_emitters_1" "多个发射器：一个初始发射器" (base "4.4")
          {:setup (partial emitters-setup [[100 60]]) :explanation "从一个发射器开始，点击画布增加新的系统。"})
   (extra 4 "4_4_emitters_2" "多个发射器：四个初始发射器" (base "4.4")
          {:setup (partial emitters-setup [[100 60] [200 30] [350 90] [540 50]])
           :explanation "四个不同起点共享粒子更新函数，展示系统的系统。"})
   (extra 5 "exercise_4_6_particle_shatter" "练习 4.6：粒子方块碎裂" (base "4.2")
          {:setup shatter-setup :step shatter-step :scene shatter-scene
           :params {:impulse {:label "爆裂冲量" :default 10 :min 1 :max 25 :step 1}}
           :explanation "100 个静止方块拼成一个大方块；点击时独立施加随机冲量，再以 0.95 的速度阻尼散开。"})
   (extra 6 "figure_4_8_circles" "图 4.8：用圆形绘制烟雾" (base "4.8")
          {:scene smoke-circle-scene :assets [] :explanation "烟雾运动保持高斯初速度与风力，改用半透明圆形显示。"})
   (extra 7 "figure_4_8_image" "图 4.8：用图像纹理绘制烟雾" (base "4.8")
          {:explanation "与圆形烟雾使用相同粒子运动，用原书透明纹理渲染每个粒子。"})
   (extra 8 "exercise_4_12_particle_textures_array" "练习 4.12：多个粒子纹理" (base "4.9")
          {:setup (fn [_] {:particles []}) :step texture-array-step :scene texture-array-scene
           :assets texture-paths :params {:emission {:label "每帧粒子数" :default 1 :min 1 :max 8 :step 1}}
           :explanation "在鼠标位置发射粒子，从原示例的三张纹理中随机选择，向上加速并进行加法混合。"})])

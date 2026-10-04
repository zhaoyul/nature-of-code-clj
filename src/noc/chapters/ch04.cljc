(ns noc.chapters.ch04
  "Particles, emitters, forces, texture and additive compositing."
  (:require [noc.math :as m] [noc.draw :as d]))

(defn particle [origin smoke? polymorphic?]
  {:position origin
   :velocity (if smoke? [(m/gaussian 0 0.3) (m/gaussian -1 0.3)] [(m/random -1 1) (m/random -2 0)])
   :lifespan (if smoke? 100 255) :angle 0
   :kind (if polymorphic? (m/choose [:circle :square]) :circle)})

(defn repel [position repeller strength]
  (let [direction (m/v- position repeller)
        distance (m/clamp (m/mag direction) 5 100)]
    (m/v* (m/normalize direction) (/ strength (* distance distance)))))

(defn update-particle [p force]
  (let [velocity (m/v+ (:velocity p) force)]
    (-> p (assoc :velocity velocity :position (m/v+ (:position p) velocity))
        (update :lifespan - 2) (update :angle + (* 0.03 (first velocity))))))

(defn setup [mode {:keys [width height]}]
  (let [origin [(/ width 2) (if (#{:texture :additive} mode) (* height 0.8) 55)]]
    {:mode mode :emitters (if (= mode :systems) [[(* width 0.3) 60] [(* width 0.7) 60]] [origin])
     :particles (if (= mode :single) [(particle origin false false)] [])}))

(defn step [{:keys [mode particles emitters] :as state} {:keys [width height mouse clicked? emission gravity wind repulsion]}]
  (let [smoke? (#{:texture :additive} mode)
        emitters (if (and (= mode :systems) clicked?) (conj emitters mouse) emitters)
        spawn? (not= mode :single)
        newborn (when spawn? (for [origin emitters _ (range (long emission))]
                              (particle origin smoke? (= mode :polymorphism))))
        wind-force (if (#{:forces :texture :additive} mode)
                     [(* wind (m/map-range (first mouse) 0 width -1 1)) 0] [0 0])
        gravity-force [0 (if smoke? 0 gravity)]
        next-particles (->> (concat particles newborn)
                            (map #(update-particle %
                                                   (m/v+ gravity-force wind-force
                                                         (if (= mode :repeller)
                                                           (repel (:position %) [(/ width 2) (* height 0.58)] repulsion)
                                                           [0 0]))))
                            (filter #(pos? (:lifespan %))) vec)]
    (assoc state :particles next-particles :emitters emitters)))

(defn particle-scene [{:keys [position lifespan kind angle]} smoke?]
  (let [[x y] position]
    (if smoke?
      [[:tint 255 lifespan] [:image "images/texture.png" (- x 16) (- y 16) 32 32] [:no-tint]]
      (concat [[:stroke 39 52 69 lifespan] [:fill 218 57 108 lifespan]]
              (if (= kind :square)
                [[:push] [:translate x y] [:rotate angle] [:rect -7 -7 14 14] [:pop]]
                [[:circle x y 12]])))))

(defn scene [{:keys [mode particles emitters]} {:keys [width height]}]
  (let [smoke? (#{:texture :additive} mode)]
    (vec (concat (if smoke? [[:background 0]] (d/background))
                 (if (= mode :additive) [[:blend-mode :add]] [[:blend-mode :blend]])
                 (mapcat #(particle-scene % smoke?) particles)
                 [[:blend-mode :blend]]
                 (when (= mode :repeller) (d/circle [(/ width 2) (* height 0.58)] 36 d/teal))
                 (when (= mode :systems) (mapcat #(d/circle % 5 d/ink) emitters))
                 (when-not smoke? (d/label (str "粒子 " (count particles) " / 发射器 " (count emitters))))))))

(def titles
  [["4.1" "单个粒子" "A Single Particle" :single]
   ["4.2" "粒子数组" "An Array of Particles" :array]
   ["4.3" "单个粒子发射器" "A Particle Emitter" :emitter]
   ["4.4" "系统的系统" "A System of Systems" :systems]
   ["4.5" "继承与多态的粒子系统" "Inheritance and Polymorphism" :polymorphism]
   ["4.6" "带力的粒子系统" "A Particle System with Forces" :forces]
   ["4.7" "带排斥器的粒子系统" "A Particle System with a Repeller" :repeller]
   ["4.8" "图像纹理粒子系统" "An Image Texture Particle System" :texture]
   ["4.9" "加法混合" "Additive Blending" :additive]])

(def examples
  (mapv (fn [[id title original-title mode]]
          {:id id :title title :original-title original-title :chapter 4 :width 640 :height 360
           :assets (if (#{:texture :additive} mode) ["images/texture.png"] [])
           :params {:emission {:label "每帧粒子数" :default 2 :min 1 :max 8 :step 1}
                    :gravity {:label "重力" :default 0.05 :min 0 :max 0.3 :step 0.01}
                    :wind {:label "风力" :default 0.1 :min 0 :max 0.5 :step 0.01}
                    :repulsion {:label "排斥强度" :default 180 :min 0 :max 500 :step 10}}
           :setup (partial setup mode) :step step :scene scene
           :source "noc/chapters/ch04.cljc"
           :explanation (case mode
                          :single "位置和速度用不可变向量表示; 寿命每帧减少, 粒子死亡后移出系统."
                          :array "发射与移除都返回新的粒子向量. 存活粒子独立积累重力和运动."
                          :emitter "发射器保存出生位置, 统一生成和更新粒子, 把系统行为从单粒子行为分开."
                          :systems "点击画布添加发射器. 每个发射器独立出生粒子, 所有系统共享更新规则."
                          :polymorphism "粒子用 :kind 区分圆形和旋转方形, 绘制函数按数据分派, 对应原书的继承和多态."
                          :forces "移动鼠标改变水平风向和强度. 外力相加后更新速度, 每帧重新计算力."
                          :repeller "排斥力沿排斥器指向粒子的单位向量, 强度与距离平方成反比, 限制距离避免奇点."
                          :texture "使用原示例的32像素透明纹理. 粒子初速遵循高斯分布, 向上运动并按寿命减小透明度."
                          :additive "与纹理例使用相同运动, Quil 的加法混合把重叠粒子的颜色相加, 形成更亮的烟雾核心.")}) titles))

;; # 第 11 章: 神经进化

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch11
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch11 :as ch]
            [noc.lessons.ch11 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 0))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：用本工程的前馈网络生成真实权重；两个输出对应拍翅与保持，权重将在神经进化中选择和突变。")

^{:nextjournal.clerk/no-cache true}
(do
(let [brain (learning/brain 5 8 2)
      labels ["flap" "no flap"]
      outputs (learning/predict brain [0.5 0.0 0.25 0.4 0.3])]
  {:inputs (:inputs brain) :outputs (zipmap labels outputs)
   :decision (nth labels (learning/best-index outputs))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 1))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 小鸟的位置, x 保持不变.
  ;; 小鸟只沿 y 轴运动, 因而速度和力都是标量.
  ;; 重力改变速度, 速度改变位置.
  ;; 阻尼让运动稍微稳定.
  ;; 处理地面.
(letfn [(bird [] {:x 50 :y 120 :velocity 0 :gravity 0.5 :flap-force -10})
        (flap [b] (update b :velocity + (:flap-force b)))
        (advance [b height]
          (let [velocity (+ (:velocity b) (:gravity b)) y (+ (:y b) velocity)]
            (if (> y height) (assoc b :y height :velocity 0)
                (assoc b :y y :velocity (* velocity 0.95)))))]
  (let [b (advance (flap (bird)) 240)]
    (notebook/scene (d/circle [(:x b) (:y b)] 16 [175 175 175]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 2))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 上下管道之间的开口大小.
  ;; 上方管道底边的随机高度.
  ;; 下方管道顶边由 top 和 spacing 得到.
  ;; 管道从画布右边缘出现.
(letfn [(pipe [width height]
          (let [spacing 100 top (m/random (- height spacing))]
            {:top top :bottom (+ top spacing) :x width :width 20 :velocity 2}))
        (advance [p] (update p :x - (:velocity p)))
        (offscreen? [p] (neg? (+ (:x p) (:width p))))]
  (let [p (advance (pipe 640 240))
        {:keys [x top bottom width]} p]
    {:pipe p :offscreen? (offscreen? p)
     :drawing (notebook/scene [[:fill 0] [:rect x 0 width top]
                               [:rect x bottom width (- 240 bottom)]])}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 3))

^{:nextjournal.clerk/no-cache true}
(do
(let [bird {:x 50 :y 20} pipe {:x 40 :width 20 :top 80 :bottom 180}
      vertical-collision (or (< (:y bird) (:top pipe)) (> (:y bird) (:bottom pipe)))
      horizontal-collision (< (:x pipe) (:x bird) (+ (:x pipe) (:width pipe)))]
  (and vertical-collision horizontal-collision))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 4))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 5))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每 100 帧加入一根新管道.
(let [initial {:bird (lesson/new-bird) :pipes [(lesson/new-pipe 640 240)] :frame 0}
      state (nth (iterate lesson/manual-frame initial) 160)]
  (notebook/scene (lesson/manual-scene state)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 6))

^{:nextjournal.clerk/no-cache true}
(do
(assoc (ch/bird nil) :brain (learning/brain 4 8 2))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 7))

^{:nextjournal.clerk/no-cache true}
(do
(let [bird (ch/bird (learning/brain 4 8 2))
      pipes [{:x 20 :width 28 :top 70 :bottom 200}
             {:x 300 :width 28 :top 100 :bottom 230}]
      next-pipe (first (filter #(> (+ (:x %) (:width %)) 50) pipes))]
  (when next-pipe
    (let [inputs (lesson/bird-inputs bird next-pipe 640 360)
          [flap keep] (learning/predict (:brain bird) inputs)
          flap? (> flap keep)]
      {:inputs inputs :outputs {:flap flap :keep keep}
       :bird (ch/bird-motion bird flap? 0.5)})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 8))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 对这些极小网络而言, CPU 反而更高效.
(let [population-size 200
      env (assoc lesson/env :population population-size)
      state (ch/evolved-birds-setup env)
      birds (mapv #(ch/evolved-bird-step % (:pipes state) (:gravity env)) (:birds state))]
  (notebook/scene (ch/birds-scene (assoc state :birds birds) false)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 9))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 其余位置和速度更新代码.
(let [bird {:fitness 0 :alive? true :y 120 :velocity 0}
      updated (-> (ch/bird-motion bird false 0.5) (update :fitness inc))]
  updated)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 10))

^{:nextjournal.clerk/no-cache true}
(do
(let [pipes [{:x 40 :width 28 :top 80 :bottom 200}]
      birds [(assoc (ch/bird (learning/brain 4 8 2)) :y 20)
             (assoc (ch/bird (learning/brain 4 8 2)) :y 140)]
      next-birds (mapv (fn [bird]
                         (if (:alive? bird) (ch/evolved-bird-step bird pipes 0.5) bird)) birds)]
  (mapv #(select-keys % [:y :velocity :fitness :alive?]) next-birds))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 11))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(all-birds-dead? [birds] (not-any? :alive? birds))]
  {:one-alive (all-birds-dead? [{:alive? false} {:alive? true}])
   :all-dead (all-birds-dead? [{:alive? false} {:alive? false}])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 12))

^{:nextjournal.clerk/no-cache true}
(do
(let [birds (lesson/parents)
      weighted-selection (fn [] (:brain (learning/weighted-choice birds :fitness)))]
  (learning/brain-genes (weighted-selection)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 13))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：全体适应度为零时改用均匀分布，避免除零。")

^{:nextjournal.clerk/no-cache true}
(do
(let [birds (lesson/parents) sum (reduce + (map :fitness birds))
      population-size (count birds)]
  (mapv (fn [bird]
          (assoc bird :fitness (if (pos? sum) (/ (:fitness bird) sum) (/ 1 population-size))))
        birds))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [birds (lesson/parents)
      parent-a (:brain (learning/weighted-choice birds :fitness))
      parent-b (:brain (learning/weighted-choice birds :fitness))
      child (learning/crossover-brain parent-a parent-b)]
  {:parent-a (learning/brain-genes parent-a) :parent-b (learning/brain-genes parent-b)
   :child (learning/brain-genes child)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 15))

^{:nextjournal.clerk/no-cache true}
(do
(let [child (learning/brain 4 8 2)
      mutated (learning/mutate-brain child 0.01)]
  {:before (learning/brain-genes child) :after (learning/brain-genes mutated)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 16))

^{:nextjournal.clerk/no-cache true}
(do
(let [birds (lesson/parents)
      population-size (count birds)
      next-birds (mapv (fn [_]
                         (let [parent-a (:brain (learning/weighted-choice birds :fitness))
                               parent-b (:brain (learning/weighted-choice birds :fitness))
                               child (-> (learning/crossover-brain parent-a parent-b)
                                         (learning/mutate-brain 0.01))]
                           (ch/bird child)))
                       (range population-size))]
  (mapv #(select-keys % [:y :fitness :alive? :brain]) next-birds))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 17))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(bird [brain] (ch/bird (or brain (learning/brain 4 8 2))))]
  {:fresh (bird nil) :inherited (bird (learning/brain 4 8 2))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 18))

;; ## Clojure 实验 11.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "11.1")

(notebook/sketch "11.1")

(select-keys (:state (engine/run "11.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 19))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 其余游戏更新和绘制代码.
  ;; 只保留最新的一根管道, 防止新一代出生时立即碰撞.
(let [birds (mapv #(assoc % :alive? false) (lesson/parents))
      pipes [{:x 20 :width 28 :top 70 :bottom 200}
             {:x 640 :width 28 :top 100 :bottom 230}]
      all-dead? (not-any? :alive? birds)
      next-state (if all-dead?
                   {:birds (mapv (fn [_] (ch/bird (ch/child-brain birds :fitness 0.01))) birds)
                    :pipes (vec (take-last 1 pipes)) :generation 1 :age 0 :best-fitness 68}
                   {:birds birds :pipes pipes :generation 0 :age 100 :best-fitness 68})]
  (notebook/scene (ch/birds-scene next-state false)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 20))

^{:nextjournal.clerk/no-cache true}
(do
(let [lifespan 200 max-force 0.1]
  (mapv (fn [_] (m/v* (m/random-vector) (m/random max-force))) (range lifespan)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [brain (learning/brain 2 8 2)]
  {:brain brain :prediction (learning/predict brain [0.5 0.75])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 22))

^{:nextjournal.clerk/no-cache true}
(do
(let [brain (learning/brain 2 8 2) inputs [0.5 0.75] max-force 0.1
      [angle-output magnitude-output] (learning/predict brain inputs)
      angle (* angle-output m/tau) magnitude (* magnitude-output max-force)
      force (m/set-mag (m/from-angle angle) magnitude)]
  {:angle angle :magnitude magnitude :force force
   :acceleration (m/v+ [0 0] force)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 23))

^{:nextjournal.clerk/no-cache true}
(do
(let [[x y] [320 220] width 640 height 240]
  [(/ x width) (/ y height)])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 24))

;; ## Clojure 实验 11.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "11.2")

(notebook/sketch "11.2")

(select-keys (:state (engine/run "11.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 25))

^{:nextjournal.clerk/no-cache true}
(do
(let [population (mapv (fn [fitness]
                         (assoc (ch/neural-rocket (learning/brain 2 8 2)) :fitness fitness)) [10 30 60])
      next-population (mapv (fn [_]
                             (let [parent-a (:brain (learning/weighted-choice population :fitness))
                                   parent-b (:brain (learning/weighted-choice population :fitness))
                                   child (-> (learning/crossover-brain parent-a parent-b)
                                             (learning/mutate-brain 0.05))]
                               (assoc (ch/neural-rocket child) :position [320 220]))) population)]
  {:population next-population :generation 1})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 26))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(glow [] {:xoff 0 :yoff 1000 :position [0 0] :radius 24})
        (advance [g]
          (-> g (assoc :position [(* 640 (m/noise (:xoff g))) (* 240 (m/noise (:yoff g)))])
              (update :xoff + 0.01) (update :yoff + 0.01)))]
  (let [g (nth (iterate advance (glow)) 120)]
    (notebook/scene (d/circle (:position g) (* 2 (:radius g)) [200 200 200]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 27))

^{:nextjournal.clerk/no-cache true}
(do
(let [creature {:position [200 120] :velocity [1 0] :brain (learning/brain 5 8 2)}
      target [320 120] max-speed 4
      delta (m/v- target (:position creature)) [dx dy] (m/normalize delta)
      [vx vy] (m/vdiv (:velocity creature) max-speed)
      inputs [dx dy (/ (m/mag delta) 640) vx vy]
      [angle magnitude] (learning/predict (:brain creature) inputs)
      force (m/set-mag (m/from-angle (* angle m/tau)) magnitude)]
  {:inputs inputs :force force :acceleration (m/v+ [0 0] force)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 28))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 常规的位置, 速度和加速度更新.
(let [creature {:position [300 120] :velocity [1 0] :acceleration [0.1 0]
               :radius 4 :fitness 0}
      target {:position [320 120] :radius 24}
      velocity (m/v+ (:velocity creature) (:acceleration creature))
      position (m/v+ (:position creature) velocity)
      distance (m/distance position (:position target))]
  (cond-> (assoc creature :position position :velocity velocity :acceleration [0 0])
    (< distance (+ (:radius creature) (:radius target))) (update :fitness inc)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 29))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 绘制只执行一次.
  ;; 模拟根据 slider 的值执行多次.
(let [simulation-steps 10
      env (assoc lesson/env :population 12 :lifespan 250)
      state (ch/steering-setup env)
      state (nth (iterate #(ch/steering-step-once % env) state) simulation-steps)]
  (notebook/scene (ch/steering-scene state env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 30))

;; ## Clojure 实验 11.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "11.3")

(notebook/sketch "11.3")

(select-keys (:state (engine/run "11.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 31))

^{:nextjournal.clerk/no-cache true}
(do
  ;; seek() 使用前述五个输入预测转向力.
  ;; update() 在接触 glow 时增加适应度.
(letfn [(creature [x y brain]
          {:position [x y] :velocity [0 0] :acceleration [0 0] :radius 4
           :max-speed 4 :fitness 0 :brain (or brain (learning/brain 5 8 2))})]
  (let [c (creature 200 120 nil)
        updated (ch/steering-update c [320 120])]
    (select-keys updated [:position :velocity :fitness :brain])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 32))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [320 120] radius 16 total-sensors 8
      sensors (mapv (fn [i]
                      (let [angle (m/map-range i 0 total-sensors 0 m/tau)]
                        (m/set-mag (m/from-angle angle) (* radius 1.5))))
                    (range total-sensors))]
  (notebook/scene
   (d/scene (mapcat #(d/line position (m/v+ position %)) sensors)
            (d/circle position (* radius 2)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 33))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(food [] {:position [(m/random 640) (m/random 240)] :radius 50})]
  (let [f (food)]
    (notebook/scene (d/circle (:position f) (* 2 (:radius f)) [0 0 0 100]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 34))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 传感器端点等于生物位置加上传感器向量.
  ;; 越接近食物中心, 传感器值越接近 1.
(let [position [300 120] sensor {:offset [32 0] :value 0}
      food {:position [350 120] :radius 50}
      tip (m/v+ position (:offset sensor))
      distance (m/distance tip (:position food))
      value (if (< distance (:radius food))
              (m/map-range distance 0 (:radius food) 1 0) 0)]
  (assoc sensor :value value))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 35))

;; ## Clojure 实验 11.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "11.4")

(notebook/sketch "11.4")

(select-keys (:state (engine/run "11.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 36))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 暂时用鼠标控制 bloop.
(let [position [300 120] foods [{:position [350 120] :radius 50}]
      sensors (lesson/sensors position 32 15)
      values (mapv :value (mapv #(lesson/sense position % (first foods)) sensors))]
  (notebook/scene
   (d/scene (d/background) (ch/food-drawing foods)
            (ch/sensors-drawing position 32 values 16 d/ink))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 37))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [300 120] foods [{:position [350 120] :radius 50}]
      inputs (ch/sensor-values position 18 foods)
      brain (learning/brain (count inputs) 10 2)
      [angle-output magnitude] (learning/predict brain inputs)
      force (m/set-mag (m/from-angle (* angle-output m/tau)) magnitude)]
  {:sensor-inputs inputs :force force :acceleration (m/v+ [0 0] force)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 38))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 其余属性.
  ;; 常规位置, 速度和加速度更新.
(let [creature {:position [300 120] :velocity [1 0] :health 100}]
  (-> creature (update :position m/v+ (:velocity creature)) (update :health - 0.25)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 39))

^{:nextjournal.clerk/no-cache true}
(do
(let [bloops [(assoc (ch/neural-bloop [320 120] (learning/brain 15 10 2)) :health -1)
             (ch/neural-bloop [300 120] (learning/brain 15 10 2))]
      survivors (filterv #(not (neg? (:health %))) bloops)
      children (keep (fn [parent]
                       (when (< (m/random) 0.001) (lesson/reproduce parent 0.1))) survivors)]
  (into survivors children))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 40))

^{:nextjournal.clerk/no-cache true}
(do
(let [parent (ch/neural-bloop [300 120] (learning/brain 15 10 2))
      brain (learning/mutate-brain (:brain parent) 0.1)
      child (ch/neural-bloop (:position parent) brain)]
  {:position (:position child) :health (:health child)
   :parent-genes (learning/brain-genes (:brain parent))
   :child-genes (learning/brain-genes (:brain child))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 41))

^{:nextjournal.clerk/no-cache true}
(do
(let [creature {:position [300 120] :radius 12 :health 80}
      food {:position [320 120] :radius 50}
      distance (m/distance (:position creature) (:position food))]
  (cond-> creature (< distance (+ (:radius creature) (:radius food))) (update :health + 0.5)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 42))

;; ## Clojure 实验 11.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "11.5")

(notebook/sketch "11.5")

(select-keys (:state (engine/run "11.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 43))

^{:nextjournal.clerk/no-cache true}
(do
(let [env (assoc lesson/env :population 20 :mutation 0.1 :reproduction 0.001)
      state (ch/neural-eco-setup env)
      state (nth (iterate #(ch/neural-eco-step-once % env) state) 120)]
  (notebook/scene (ch/neural-eco-scene state env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch11" 44))

;; ## Clojure 实验 11.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "11.6")

(notebook/sketch "11.6")

(select-keys (:state (engine/run "11.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "11.1")

;; ## 本章随附练习与补充演示

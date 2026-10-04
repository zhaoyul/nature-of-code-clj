;; # 第 2 章: 力

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch02
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch02 :as ch]
            [noc.lessons.ch02 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 0))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-mover []
          {:position [0 0] :velocity [0 0] :acceleration [0 0]})]
  (make-mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 1))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover (lesson/mover) wind [0.1 0] gravity [0 0.1]]
  (-> mover (ch/apply-force wind) (ch/apply-force gravity)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 2))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：保留原例错误模型以观察问题：后一个力覆盖前一个力，后续片段改为合力累加。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(overwrite-force [mover force] (assoc mover :acceleration force))]
  (let [mover (lesson/mover)]
    (-> mover (overwrite-force [0.1 0]) (overwrite-force [0 0.1]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 3))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover (lesson/mover) wind [0.1 0] gravity [0 0.1]]
  (-> mover (ch/apply-force wind) (ch/apply-force gravity) ch/integrate))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [velocity [1 2] acceleration [0.1 0.2]]
  (m/v+ velocity acceleration))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 5))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：此处先假设质量为 1，直接累加力；后续再除以质量。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(apply-force [mover force]
          (update mover :acceleration m/v+ force))]
  (-> (lesson/mover) (apply-force [0.1 0]) (apply-force [0 0.1])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 6))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover (lesson/mover) mouse-pressed? (:pressed? lesson/env)]
  (if mouse-pressed? (ch/apply-force mover [0.5 0]) mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 7))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(update-mover [{:keys [position velocity acceleration] :as mover}]
          (let [velocity (m/v+ velocity acceleration)]
            (assoc mover :velocity velocity :position (m/v+ position velocity)
                   :acceleration [0 0])))]
  (update-mover {:position [0 0] :velocity [1 0] :acceleration [0 0.1]}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 8))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-mover []
          {:position [0 0] :velocity [0 0] :acceleration [0 0] :mass 10})]
  (make-mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 9))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  {:position [(m/random (:width lesson/env)) (m/random (:height lesson/env))]
   :velocity [0 0] :acceleration [0 0] :mass 10})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 10))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：不可变向量的除法返回新值，因此不会像原例的 force.div 那样破坏共享的力。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(apply-force [mover force]
          (let [acceleration (m/vdiv force (:mass mover))]
            (update mover :acceleration m/v+ acceleration)))]
  (let [force [1 0] mover (lesson/mover [100 30] 10)]
    {:force-before force :mover (apply-force mover force) :force-after force}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 11))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：同一个不可变 wind 可安全传给两个物体，两者得到相同加速度。")

^{:nextjournal.clerk/no-cache true}
(do
(let [wind [1 0]
      mover-a (lesson/mover [100 30] 10)
      mover-b (lesson/mover [400 30] 10)]
  {:a (ch/apply-force mover-a wind)
   :b (ch/apply-force mover-b wind)
   :wind wind})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 12))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：vec 表达原例复制向量的意图；不可变数据本身已经避免别名修改。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(apply-force [mover force]
          (let [f (vec force)
                f (m/vdiv f (:mass mover))]
            (update mover :acceleration m/v+ f)))]
  (apply-force (lesson/mover [100 30] 10) [1 0]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 13))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(apply-force [mover force]
          (let [f (m/vdiv force (:mass mover))]
            (update mover :acceleration m/v+ f)))]
  (apply-force (lesson/mover [100 30] 10) [1 0]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [wind [0.01 0] mover (lesson/mover)]
  (ch/apply-force mover wind))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 15))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 16))

^{:nextjournal.clerk/no-cache true}
(do
(let [gravity [0 0.1]
      mover (ch/apply-force (lesson/mover) gravity)]
  (if (:pressed? lesson/env) (ch/apply-force mover [0.1 0]) mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 17))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-mover []
          {:mass 1 :position [320 30] :velocity [0 0] :acceleration [0 0]})
        (apply-force [mover force]
          (update mover :acceleration m/v+ (m/vdiv force (:mass mover))))
        (update-mover [{:keys [velocity acceleration] :as mover}]
          (let [velocity (m/v+ velocity acceleration)]
            (-> mover (assoc :velocity velocity :acceleration [0 0])
                (update :position m/v+ velocity))))
        (check-edges [{:keys [position velocity] :as mover}]
          (let [[x y] position [vx vy] velocity
                hit-x? (or (> x 640) (< x 0)) hit-y? (> y 240)]
            (assoc mover :position [(m/clamp x 0 640) (min y 240)]
                   :velocity [(if hit-x? (- vx) vx) (if hit-y? (- vy) vy)])))
        (show [mover] (d/circle (:position mover) (* (:mass mover) 16) [175]))]
  (let [mover (-> (make-mover) (apply-force [0 0.1]) update-mover check-edges)]
    {:mover mover :scene (notebook/scene (show mover))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover-a (lesson/mover) mover-b (lesson/mover)]
  {:a mover-a :b mover-b})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 19))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 其他方法保持不变.
(letfn [(make-mover [x y mass]
          {:mass mass :position [x y] :velocity [0 0] :acceleration [0 0]})]
  (make-mover 100 30 10))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 20))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover-a (lesson/mover [100 30] 10)
      mover-b (lesson/mover [400 30] 2)]
  {:a mover-a :b mover-b})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 21))

;; ## Clojure 实验 2.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.1")

(notebook/sketch "2.1")

(select-keys (:state (engine/run "2.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 22))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 虚构一个重力并施加给两个对象.
  ;; 按下鼠标时施加风力.
(let [gravity [0 0.1] wind [0.1 0]
      movers [(lesson/mover [100 30] 10) (lesson/mover [400 30] 2)]
      movers (mapv (fn [mover]
                     (let [mover (ch/apply-force mover gravity)
                           mover (if (:pressed? lesson/env)
                                   (ch/apply-force mover wind) mover)]
                       (-> mover (ch/edges lesson/env) ch/integrate))) movers)]
  {:movers movers :scene (notebook/scene
                           (d/scene [[:background 255]] (mapcat lesson/show movers)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 23))

;; ## Clojure 实验 2.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.2")

(notebook/sketch "2.2")

(select-keys (:state (engine/run "2.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 24))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：重力乘质量后再在 apply-force 中除以质量，两个物体的下落加速度相同。")

^{:nextjournal.clerk/no-cache true}
(do
(let [gravity [0 0.1]
      mover-a (lesson/mover [100 30] 10) mover-b (lesson/mover [400 30] 2)
      gravity-a (m/v* gravity (:mass mover-a))
      gravity-b (m/v* gravity (:mass mover-b))]
  {:a (ch/apply-force mover-a gravity-a)
   :b (ch/apply-force mover-b gravity-b)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 25))

^{:nextjournal.clerk/no-cache true}
(do
(let [velocity [3 4]
      friction (-> velocity m/normalize (m/v* -1))]
  friction)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 26))

^{:nextjournal.clerk/no-cache true}
(do
(let [c 0.01] {:friction-coefficient c})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 27))

^{:nextjournal.clerk/no-cache true}
(do
(let [normal 1] {:normal-force normal})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 28))

^{:nextjournal.clerk/no-cache true}
(do
(let [c 0.1 normal 1 friction-mag (* c normal)
      velocity [3 4]
      friction (-> velocity (m/v* -1) m/normalize (m/v* friction-mag))]
  friction)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 29))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(contact-edge? [{:keys [position radius]} height]
          (> (second position) (- height radius 1)))]
  {:touching? (contact-edge? {:position [100 232] :radius 8} 240)
   :airborne? (contact-edge? {:position [100 100] :radius 8} 240)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 30))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(bounce-edges [{:keys [position velocity radius] :as mover} height]
          (let [[x y] position [vx vy] velocity bounce -0.9]
            (if (> y (- height radius))
              (assoc mover :position [x (- height radius)] :velocity [vx (* vy bounce)])
              mover)))]
  (bounce-edges {:position [100 239] :velocity [3 4] :radius 8} 240))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 31))

;; ## Clojure 实验 2.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.3")

(notebook/sketch "2.3")

(select-keys (:state (engine/run "2.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 32))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover (assoc (lesson/mover [320 233] 1) :velocity [3 2])
      mover (ch/apply-force mover [0 1])
      mover (if (:pressed? lesson/env) (ch/apply-force mover [0.5 0]) mover)
      mover (if (lesson/contact? mover lesson/env)
              (ch/apply-force mover (m/set-mag (m/v* (:velocity mover) -1) 0.1))
              mover)
      mover (-> mover (lesson/bounce-bottom lesson/env 0.9) ch/integrate)]
  {:mover mover :scene (notebook/scene
                          (d/scene [[:background 255]] (lesson/show mover)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 33))

^{:nextjournal.clerk/no-cache true}
(do
(let [c 0.1 velocity [3 4] speed (m/mag velocity)
      drag-magnitude (* c speed speed)
      drag (m/set-mag (m/v* velocity -1) drag-magnitude)]
  {:speed speed :drag drag})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 34))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-liquid [x y w h c] {:x x :y y :w w :h h :c c})
        (show-liquid [{:keys [x y w h]}]
          [[:no-stroke] [:fill 175] [:rect x y w h]])]
  (let [liquid (make-liquid 0 120 640 120 0.1)]
    {:liquid liquid :scene (notebook/scene (show-liquid liquid))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 35))

^{:nextjournal.clerk/no-cache true}
(do
(let [{:keys [width height]} lesson/env
      liquid {:x 0 :y (/ height 2) :w width :h (/ height 2) :c 0.1}]
  liquid)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 36))

^{:nextjournal.clerk/no-cache true}
(do
(let [liquid (lesson/liquid)
      mover (assoc (lesson/mover [320 180] 1) :velocity [3 4])]
  (if (lesson/contains-body? liquid mover)
    (ch/apply-force mover (lesson/drag liquid mover))
    mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 37))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(contains-mover? [{:keys [x y w h]} mover]
          (let [[px py] (:position mover)]
            (and (> px x) (< px (+ x w)) (> py y) (< py (+ y h)))))]
  (let [liquid (lesson/liquid)]
    {:inside (contains-mover? liquid (lesson/mover [320 180] 1))
     :outside (contains-mover? liquid (lesson/mover [320 30] 1))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 38))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(calculate-drag [liquid mover]
          (let [velocity (:velocity mover) speed (m/mag velocity)
                drag-magnitude (* (:c liquid) speed speed)]
            (m/set-mag (m/v* velocity -1) drag-magnitude)))]
  (calculate-drag (lesson/liquid)
                  (assoc (lesson/mover [320 180] 1) :velocity [3 4])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 39))

;; ## Clojure 实验 2.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.4")

(notebook/sketch "2.4")

(select-keys (:state (engine/run "2.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 40))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：有限执行 60 帧；reset-movers 对应鼠标事件中重新初始化对象。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 重新初始化草图中的移动对象.
(letfn [(reset-movers []
          (mapv (fn [i] (lesson/mover [(+ 40 (* i 70)) 0] (m/random 0.1 5)))
                (range 9)))
        (step-movers [movers liquid]
          (mapv (fn [mover]
                  (let [mover (if (lesson/contains-body? liquid mover)
                                (ch/apply-force mover (lesson/drag liquid mover)) mover)
                        gravity [0 (* 0.1 (:mass mover))]]
                    (-> mover (ch/apply-force gravity) ch/integrate
                        (ch/edges lesson/env)))) movers))]
  (binding [m/*rng* (m/seeded 42)]
    (let [liquid (lesson/liquid)
          initial (reset-movers)
          movers (nth (iterate #(step-movers % liquid) initial) 60)
          reset-after-click (reset-movers)]
      {:movers movers :reset-after-click reset-after-click
       :scene (notebook/scene
                (d/scene [[:background 255]] (lesson/liquid-scene liquid)
                         (mapcat lesson/show movers)))})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 41))

^{:nextjournal.clerk/no-cache true}
(do
(let [mouse (:mouse lesson/env) position [320 120]]
  (m/v- mouse position))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 42))

^{:nextjournal.clerk/no-cache true}
(do
(let [position1 [320 120] position2 [100 100]
      direction (m/v- position1 position2)]
  (m/normalize direction))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 43))

^{:nextjournal.clerk/no-cache true}
(do
(let [G 1 mass1 20 mass2 2 distance 25
      direction (m/normalize [20 70])
      magnitude (/ (* G mass1 mass2) (* distance distance))]
  (m/v* direction magnitude))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 44))

^{:nextjournal.clerk/no-cache true}
(do
(let [G 1 mass1 20 mass2 2
      position1 [300 50] position2 [320 120]
      force (m/v- position2 position1) distance (m/mag force)
      magnitude (/ (* G mass1 mass2) (* distance distance))]
  (m/set-mag force magnitude))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 45))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-attractor [{:keys [width height]}]
          {:position [(/ width 2) (/ height 2)] :mass 20})
        (show-attractor [attractor]
          (d/circle (:position attractor) (* 2 (:mass attractor)) [175 200]))]
  (let [attractor (make-attractor lesson/env)]
    {:attractor attractor :scene (notebook/scene (show-attractor attractor))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 46))

^{:nextjournal.clerk/no-cache true}
(do
(let [canvas {:width 640 :height 360}
      attractor {:position [320 180] :mass 20}
      mover (ch/integrate (lesson/mover [300 100] 5))]
  {:canvas canvas :mover mover :attractor attractor
   :scene (notebook/scene
            (d/scene [[:background 255]]
                     (d/circle (:position attractor) 40 [175 200])
                     (lesson/show mover)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 47))

^{:nextjournal.clerk/no-cache true}
(do
(let [attractor {:position [320 180] :mass 20}
      mover (lesson/mover [300 100] 5)
      force (ch/attraction-force attractor mover 1)
      mover (-> mover (ch/apply-force force) ch/integrate)]
  {:force force :mover mover
   :scene (notebook/scene
            (d/scene [[:background 255]]
                     (d/circle (:position attractor) 40 [175 200])
                     (lesson/show mover)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 48))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(attract [attractor mover]
          (let [force (m/v- (:position attractor) (:position mover))
                distance (m/mag force)
                strength (/ (* (:mass attractor) (:mass mover)) (* distance distance))]
            (m/set-mag force strength)))]
  (attract {:position [320 120] :mass 20} (lesson/mover [300 50] 2)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 49))

^{:nextjournal.clerk/no-cache true}
(do
(let [distances [0 3 10 40 100]]
  (mapv #(m/clamp % 5 25) distances))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 50))

;; ## Clojure 实验 2.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.5")

(notebook/sketch "2.5")

(select-keys (:state (engine/run "2.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 51))

^{:nextjournal.clerk/no-cache true}
(do
(let [G 1.0 attractor {:position [320 120] :mass 20}]
  (letfn [(attract [mover]
            (let [force (m/v- (:position attractor) (:position mover))
                  distance (m/clamp (m/mag force) 5 25)
                  strength (/ (* G (:mass attractor) (:mass mover))
                              (* distance distance))]
              (m/set-mag force strength)))
          (step [mover] (ch/integrate (ch/apply-force mover (attract mover))))]
    (let [mover (nth (iterate step (lesson/mover [300 50] 2)) 30)]
      {:mover mover :attractor attractor
       :scene (notebook/scene
                (d/scene [[:background 255]]
                         (d/circle (:position attractor) 40 [175 200])
                         (lesson/show mover)))})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 52))

^{:nextjournal.clerk/no-cache true}
(do
(let [body (lesson/mover [320 120] 20)
      diameter (* 2 (m/sqrt (:mass body)))]
  (notebook/scene (d/circle (:position body) diameter [175])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 53))

;; ## Clojure 实验 2.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.6")

(notebook/sketch "2.6")

(select-keys (:state (engine/run "2.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 54))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [attractor {:position [320 180] :mass 20}
        movers (mapv (fn [_] (lesson/mover [(m/random 640) (m/random 360)]
                                           (m/random 0.5 3))) (range 10))
        movers (mapv (fn [mover]
                       (-> mover (ch/apply-force (ch/attraction-force attractor mover 1))
                           ch/integrate)) movers)]
    {:movers movers
     :scene (notebook/scene
              (d/scene [[:background 255]]
                       (d/circle (:position attractor) 40 [175 200])
                       (mapcat lesson/show movers)))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 55))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：此版本返回施加引力后的目标物体，对应原方法内部调用 body.applyForce。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 构造函数、applyForce()、update()、show() 等与前面类似.
(letfn [(attract-body [source target G]
          (let [force (m/v- (:position source) (:position target))
                distance (m/clamp (m/mag force) 5 25)
                strength (/ (* G (:mass source) (:mass target)) (* distance distance))]
            (ch/apply-force target (m/set-mag force strength))))]
  (attract-body (lesson/mover [320 40] 10) (lesson/mover [320 200] 10) 1))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 56))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：先从同一帧状态计算两边的引力，再同时积分，保持作用力与反作用力成对。")

^{:nextjournal.clerk/no-cache true}
(do
(let [body-a (lesson/mover [320 40] 10)
      body-b (lesson/mover [320 200] 10)
      force-on-a (ch/attraction-force body-b body-a 1)
      force-on-b (ch/attraction-force body-a body-b 1)
      body-a (-> body-a (ch/apply-force force-on-a) ch/integrate)
      body-b (-> body-b (ch/apply-force force-on-b) ch/integrate)]
  {:a body-a :b body-b
   :scene (notebook/scene
            (d/scene [[:background 255]] (lesson/show body-a) (lesson/show body-b)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 57))

;; ## Clojure 实验 2.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.7")

(notebook/sketch "2.7")

(select-keys (:state (engine/run "2.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 58))

^{:nextjournal.clerk/no-cache true}
(do
(let [body-a (assoc (lesson/mover [320 40] 10) :velocity [1 0])
      body-b (assoc (lesson/mover [320 200] 10) :velocity [-1 0])]
  {:a body-a :b body-b})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 59))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [bodies (mapv (fn [_] (lesson/mover [(m/random 640) (m/random 240)] 1))
                     (range 10))
        bodies (mapv ch/integrate bodies)]
    {:bodies bodies :scene (notebook/scene
                             (d/scene [[:background 255]] (mapcat lesson/show bodies)))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 60))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：此处保留全部配对的循环。受约束距离和零向量归一化让自引力为零，下一段显式排除自身；所有物体按同一帧状态计算。")

^{:nextjournal.clerk/no-cache true}
(do
(let [bodies [(lesson/mover [100 100] 1)
              (lesson/mover [320 80] 2)
              (lesson/mover [480 180] 3)]
      next-bodies
      (mapv (fn [target]
              (let [net-force (reduce m/v+ [0 0]
                                      (map #(ch/attraction-force % target 1) bodies))]
                (-> target (ch/apply-force net-force) ch/integrate))) bodies)]
  {:bodies next-bodies :scene (notebook/scene
                               (d/scene [[:background 255]]
                                        (mapcat lesson/show next-bodies)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 61))

;; ## Clojure 实验 2.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.8")

(notebook/sketch "2.8")

(select-keys (:state (engine/run "2.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 62))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：按索引排除自身，累加其他物体的引力；有限执行 12 帧，避免遍历顺序改变同一帧的输入状态。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(step-bodies [bodies]
          (mapv (fn [i target]
                  (let [net-force
                        (reduce m/v+ [0 0]
                                (keep-indexed (fn [j source]
                                                (when (not= i j)
                                                  (ch/attraction-force source target 1)))
                                              bodies))]
                    (-> target (ch/apply-force net-force) ch/integrate)))
                (range) bodies))]
  (binding [m/*rng* (m/seeded 42)]
    (let [initial (mapv (fn [_]
                          (lesson/mover [(m/random 640) (m/random 240)] (m/random 0.1 2)))
                        (range 10))
          bodies (nth (iterate step-bodies initial) 12)]
      {:bodies bodies
       :scene (notebook/scene
                (d/scene [[:background 255]] (mapcat lesson/show bodies)))})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 63))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：该版本只计算并返回引力，不在 attract 内施加力，方便将所有力求和后再积分。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(attract [source target G]
          (let [force (m/v- (:position source) (:position target))
                distance (m/clamp (m/mag force) 5 25)
                strength (/ (* G (:mass source) (:mass target)) (* distance distance))]
            (m/set-mag force strength)))]
  (attract (lesson/mover [320 40] 10) (lesson/mover [320 200] 2) 1))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch02" 64))

;; ## Clojure 实验 2.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "2.9")

(notebook/sketch "2.9")

(select-keys (:state (engine/run "2.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "2.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 2.1 · examples/02_forces/nature_of_code_exercise_2_1_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/02_forces/nature_of_code_exercise_2_1_solution"))

(notebook/sketch (notebook/companion-id "examples/02_forces/nature_of_code_exercise_2_1_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/02_forces/nature_of_code_exercise_2_1_solution") 12)))

;; ### Exercise 2.16 · examples/02_forces/exercise_2_16

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/02_forces/exercise_2_16"))

(notebook/sketch (notebook/companion-id "examples/02_forces/exercise_2_16"))

(:_frame (:state (engine/run (notebook/companion-id "examples/02_forces/exercise_2_16") 12)))

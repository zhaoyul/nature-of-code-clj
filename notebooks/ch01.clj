;; # 第 1 章: 向量

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch01
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch01 :as ch]
            [noc.lessons.ch01 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 0))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 1))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 根据速度移动小球.
  ;; 检查是否碰到边界并反弹.
  ;; 在位置 (x, y) 绘制小球.
(letfn [(draw-step [{:keys [x y xspeed yspeed]} {:keys [width height]}]
          (let [x (+ x xspeed) y (+ y yspeed)]
            {:x x :y y
             :xspeed (if (or (> x width) (< x 0)) (- xspeed) xspeed)
             :yspeed (if (or (> y height) (< y 0)) (- yspeed) yspeed)}))]
  (let [initial {:x 100 :y 100 :xspeed 2.5 :yspeed 2}
        ball (nth (iterate #(draw-step % lesson/env) initial) 12)]
    {:ball ball :scene (notebook/scene
                        [[:background 255] [:stroke 0] [:fill 127]
                         [:circle (:x ball) (:y ball) 48]])}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 2))

^{:nextjournal.clerk/no-cache true}
(do
(let [x nil y nil xspeed nil yspeed nil]
  {:x x :y y :xspeed xspeed :yspeed yspeed})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 3))

^{:nextjournal.clerk/no-cache true}
(do
(let [position nil speed nil]
  {:position position :speed speed})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 4))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：二维向量用含两个数的不可变向量保存，不需要自定义类。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(vector2 [x y] [x y])]
  (vector2 100 100))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 100 y 100 xspeed 1 yspeed 3.3]
  {:position [x y] :velocity [xspeed yspeed]})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 6))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [100 100] velocity [1 3.3]]
  {:position position :velocity velocity})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 7))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 100 y 100 xspeed 1 yspeed 3.3
      x (+ x xspeed) y (+ y yspeed)]
  [x y])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 8))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Clojure 的 + 处理标量；二维向量相加使用 m/v+。")

^{:nextjournal.clerk/no-cache true}
(do
(let [position [100 100] velocity [1 3.3]]
  (m/v+ position velocity))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 9))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：函数返回新向量，原向量保持不变。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(vector-add [[x y] [vx vy]]
          [(+ x vx) (+ y vy)])]
  (vector-add [100 100] [1 3.3]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 10))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原片段中的 position + velocity 是错误示范。对应的正确 Clojure 写法为 m/v+，然后保存返回值。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这行不能工作.
  ;; 把 velocity 向量加到 position 向量上.
(let [position [100 100] velocity [1 3.3]
      next-position (m/v+ position velocity)]
  {:before position :after next-position})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 11))

;; ## Clojure 实验 1.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.1")

(notebook/sketch "1.1")

(select-keys (:state (engine/run "1.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 12))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Clojure 数值向量可在任意位置创建；Quil 草图仍在 setup 中设置画布。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; createVector() 必须在 setup() 内调用.
(letfn [(update-ball [{:keys [position velocity]} {:keys [width height]}]
          (let [[x y :as position] (m/v+ position velocity)
                [vx vy] velocity]
            {:position position
             :velocity [(if (or (> x width) (< x 0)) (- vx) vx)
                        (if (or (> y height) (< y 0)) (- vy) vy)]}))]
  (let [ball (nth (iterate #(update-ball % lesson/env)
                          {:position [100 100] :velocity [2.5 2]}) 12)]
    {:ball ball
     :scene (notebook/scene
              (d/scene [[:background 255]] (d/circle (:position ball) 48 [127])))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 13))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：解构取出两个分量；本工程 d/circle 也接受整个位置向量。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 不能工作.
  ;; 使用点语法读取向量分量.
(let [position [100 100]
      [x y] position]
  (notebook/scene [[:circle x y 48]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [650 100] velocity [2.5 2]
      [x _] position
      velocity (if (or (> x (:width lesson/env)) (< x 0))
                 (update velocity 0 -) velocity)]
  velocity)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 15))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(vector-subtract [[x y] [vx vy]]
          [(- x vx) (- y vy)])]
  (vector-subtract [480 180] [320 120]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 16))

;; ## Clojure 实验 1.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.2")

(notebook/sketch "1.2")

(select-keys (:state (engine/run "1.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 17))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：用相加后的世界坐标绘制，等价于把原点平移到画布中心。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 一个向量表示鼠标位置, 另一个表示画布中心.
  ;; 绘制原始两个位置向量.
  ;; 向量减法.
  ;; 移动原点, 再绘制减法结果.
(let [mouse (:mouse lesson/env)
      center [(/ (:width lesson/env) 2) (/ (:height lesson/env) 2)]
      difference (m/v- mouse center)]
  (notebook/scene
    (d/scene [[:background 255] [:stroke-weight 4]]
             (d/line [0 0] mouse [200]) (d/line [0 0] center [200])
             (d/line center (m/v+ center difference) [0]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 18))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(vector-multiply [[x y] n] [(* x n) (* y n)])]
  (vector-multiply [-3 7] 3))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 19))

^{:nextjournal.clerk/no-cache true}
(do
  ;; u 现在等于 (-9, 21), 大小是原来的 3 倍.
(let [u [-3 7]] (m/v* u 3))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 20))

;; ## Clojure 实验 1.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.3")

(notebook/sketch "1.3")

(select-keys (:state (engine/run "1.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 21))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 向量缩小为原来的一半.
(let [center [320 120] mouse (:mouse lesson/env)
      difference (m/v- mouse center)
      half (m/v* difference 0.5)]
  (notebook/scene
    (d/scene [[:background 255] [:stroke-weight 2]]
             (d/line center (m/v+ center difference) [200])
             [[:stroke-weight 4]]
             (d/line center (m/v+ center half) [0]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 22))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(vector-divide [[x y] n] [(/ x n) (/ y n)])]
  (vector-divide [8 -4] 2))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 23))

^{:nextjournal.clerk/no-cache true}
(do
  ;; u 现在等于 (4, -2), 长度减半.
(let [u [8 -4]] (m/vdiv u 2))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 24))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(magnitude [[x y]] (m/sqrt (+ (* x x) (* y y))))]
  (magnitude [3 4]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 25))

;; ## Clojure 实验 1.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.4")

(notebook/sketch "1.4")

(select-keys (:state (engine/run "1.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 26))

^{:nextjournal.clerk/no-cache true}
(do
  ;; mag() 返回向量长度.
(let [center [320 120] mouse (:mouse lesson/env)
      direction (m/v- mouse center)
      magnitude (m/mag direction)]
  {:length magnitude
   :scene (notebook/scene
            (d/scene [[:background 255] [:fill 0] [:rect 0 0 magnitude 10]]
                     (d/line center (m/v+ center direction) [0])))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 27))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：这一版先使用非零向量；下一段补充长度为零的保护。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(normalize-nonzero [v]
          (let [magnitude (m/mag v)] (m/vdiv v magnitude)))]
  (normalize-nonzero [3 4]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 28))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(normalize-vector [v]
          (let [magnitude (m/mag v)]
            (if (pos? magnitude) (m/vdiv v magnitude) v)))]
  {:nonzero (normalize-vector [3 4]) :zero (normalize-vector [0 0])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 29))

;; ## Clojure 实验 1.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.5")

(notebook/sketch "1.5")

(select-keys (:state (engine/run "1.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 30))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 绘制原始向量.
  ;; 归一化后再缩放到长度 50.
(let [center [320 120] direction (m/v- (:mouse lesson/env) center)
      normalized (m/v* (m/normalize direction) 50)]
  (notebook/scene
    (d/scene [[:background 255]]
             (d/line center (m/v+ center direction) [200])
             [[:stroke-weight 8]]
             (d/line center (m/v+ center normalized) [0]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 31))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [100 100] velocity [2.5 2]]
  (m/v+ position velocity))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 32))

^{:nextjournal.clerk/no-cache true}
(do
(let [[x y] [100 100]]
  (notebook/scene [[:circle x y 48]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 33))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [width (:width lesson/env) height (:height lesson/env)]
    {:position [(m/random width) (m/random height)]
     :velocity [(m/random -2 2) (m/random -2 2)]}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 34))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 移动者根据速度改变位置.
(letfn [(update-mover [mover]
          (update mover :position m/v+ (:velocity mover)))
        (show-mover [{:keys [position]}]
          (d/circle position 48 [175]))]
  (let [mover (update-mover {:position [100 100] :velocity [2.5 2]})]
    {:mover mover :scene (notebook/scene (show-mover mover))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 35))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(check-edges [{:keys [position] :as mover} {:keys [width height]}]
          (let [[x y] position]
            (assoc mover :position
                   [(cond (> x width) 0 (< x 0) width :else x)
                    (cond (> y height) 0 (< y 0) height :else y)])))]
  (check-edges {:position [645 -3] :velocity [2 1]} lesson/env))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 36))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover nil] {:mover mover})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 37))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)] (lesson/mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 38))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover {:position [639 238] :velocity [3 4] :acceleration [0 0] :topspeed 10}
      mover (-> mover (lesson/update-motion) (lesson/wrap-edges lesson/env))]
  {:mover mover :scene (notebook/scene (lesson/show mover))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 39))

;; ## Clojure 实验 1.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.6")

(notebook/sketch "1.6")

(select-keys (:state (engine/run "1.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 40))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：用纯函数组成移动、边界环绕和显示，有限执行 30 帧供 notebook 求值。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 运动 101: 位置随速度改变.
(letfn [(make-mover [{:keys [width height]}]
          {:position [(m/random width) (m/random height)]
           :velocity [(m/random -2 2) (m/random -2 2)]})
        (step [{:keys [velocity] :as mover}]
          (-> mover (update :position m/v+ velocity)
              (lesson/wrap-edges lesson/env)))
        (show [{:keys [position]}]
          (d/scene [[:stroke-weight 2]] (d/circle position 48 [127])))]
  (binding [m/*rng* (m/seeded 42)]
    (let [mover (nth (iterate step (make-mover lesson/env)) 30)]
      {:mover mover :scene (notebook/scene
                             (d/scene [[:background 255]] (show mover)))})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 41))

^{:nextjournal.clerk/no-cache true}
(do
(let [velocity [1 0] acceleration [0.2 0.1] position [100 100]
      velocity (m/v+ velocity acceleration)
      position (m/v+ position velocity)]
  {:velocity velocity :position position})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 42))

^{:nextjournal.clerk/no-cache true}
(do
(let [width (:width lesson/env) height (:height lesson/env)]
  {:position [(/ width 2) (/ height 2)]
   :velocity [0 0] :acceleration [0 0]})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 43))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(update-mover [{:keys [position velocity acceleration] :as mover}]
          (let [velocity (m/v+ velocity acceleration)]
            (assoc mover :velocity velocity :position (m/v+ position velocity))))]
  (update-mover {:position [320 120] :velocity [0 0] :acceleration [-0.001 0.01]}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 44))

^{:nextjournal.clerk/no-cache true}
(do
(let [mover {:position [320 120] :velocity [0 0]}]
  (assoc mover :acceleration [-0.001 0.01]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 45))

^{:nextjournal.clerk/no-cache true}
(do
(let [velocity [12 16]] (m/limit velocity 10))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 46))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 当前大小超过 max
  ;; 把向量归一化
  ;; 再把大小设置为 max
(letfn [(limit-vector [v maximum]
          (if (> (m/mag v) maximum)
            (m/v* (m/normalize v) maximum)
            v))]
  {:limited (limit-vector [12 16] 10)
   :unchanged (limit-vector [3 4] 10)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 47))

;; ## Clojure 实验 1.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.7")

(notebook/sketch "1.7")

(select-keys (:state (engine/run "1.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 48))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 加速度是运动变化的关键.
  ;; 限制速度向量的最大大小.
  ;; 速度随加速度改变.
  ;; 位置随速度改变.
  ;; 与之前相同.
  ;; 与之前相同.
(letfn [(make-mover []
          {:position [320 120] :velocity [0 0]
           :acceleration [-0.001 0.01] :topspeed 10})
        (step [{:keys [velocity acceleration topspeed] :as mover}]
          (let [velocity (m/limit (m/v+ velocity acceleration) topspeed)]
            (-> mover (assoc :velocity velocity)
                (update :position m/v+ velocity)
                (lesson/wrap-edges lesson/env))))]
  (let [mover (nth (iterate step (make-mover)) 60)]
    {:mover mover :scene (notebook/scene
                           (d/scene [[:background 255]] (lesson/show mover)))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 49))

^{:nextjournal.clerk/no-cache true}
(do
  ;; random2D() 返回指向随机方向的单位向量.
(letfn [(random-step [{:keys [velocity topspeed] :as mover}]
          (let [acceleration (m/random-vector)
                velocity (m/limit (m/v+ velocity acceleration) topspeed)]
            (-> mover (assoc :acceleration acceleration :velocity velocity)
                (update :position m/v+ velocity))))]
  (binding [m/*rng* (m/seeded 42)]
    (random-step {:position [320 120] :velocity [0 0] :topspeed 10})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 50))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [acceleration (m/random-vector)] (m/v* acceleration 0.5)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 51))

;; ## Clojure 实验 1.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.8")

(notebook/sketch "1.8")

(select-keys (:state (engine/run "1.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 52))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [acceleration (m/random-vector)] (m/v* acceleration (m/random 2))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 53))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [direction (m/random-vector)]
    {:direction direction :length (m/mag direction)}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 54))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 0 y 5 x (+ x y)] x)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 55))

^{:nextjournal.clerk/no-cache true}
(do
(let [v [0 0] u [4 5] v (m/v+ v u)] v)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 56))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 0 y 5 z (+ x y)] {:x x :y y :z z})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 57))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原 p5 实例 add 会修改接收者。Clojure 向量不可变，因此 m/v+ 返回新值并保留 v 和 u。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 不要被迷惑, 这种写法不符合这里的目标.
(let [v [0 0] u [4 5] w (m/v+ v u)]
  {:v v :u u :w w :input-preserved? (= v [0 0])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 58))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：用 atom 明确演示有状态的实例加法；通常使用下面的纯函数版本。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(add-to [receiver other]
          (swap! receiver m/v+ other))]
  (let [v (atom [0 0]) u [4 5]]
    (add-to v u)
    @v))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 59))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(add-vectors [[x1 y1] [x2 y2]]
          [(+ x1 x2) (+ y1 y2)])]
  (let [v1 [0 0] v2 [4 5]]
    {:v1 v1 :v2 v2 :result (add-vectors v1 v2)}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 60))

^{:nextjournal.clerk/no-cache true}
(do
(let [v [0 0] u [4 5] w (m/v+ v u)]
  {:v v :u u :w w})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 61))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原片段只放了占位符；这里给出可直接运行的向量运算样本。")

^{:nextjournal.clerk/no-cache true}
(do
(let [v [2 3] u [4 5] w (m/v+ v u)]
  {:addition w :subtraction (m/v- v u)
   :scaled (m/v* v 2) :normalized (m/normalize v)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 62))

^{:nextjournal.clerk/no-cache true}
(do
(let [mouse (:mouse lesson/env)
      position [320 120]
      direction (m/v- mouse position)]
  direction)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 63))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 任意目标大小
(let [direction (m/v- (:mouse lesson/env) [320 120])
      anything 0.2]
  (m/v* (m/normalize direction) anything))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 64))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 任意目标大小
(let [direction (m/v- (:mouse lesson/env) [320 120])
      anything 0.2]
  (m/set-mag direction anything))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 65))

;; ## Clojure 实验 1.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.9")

(notebook/sketch "1.9")

(select-keys (:state (engine/run "1.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 66))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 第 1 步: 计算方向.
  ;; 第 2 步: 归一化.
  ;; 第 3 步: 缩放.
  ;; 第 4 步: 设置加速度.
(letfn [(update-toward-mouse [{:keys [position velocity topspeed] :as mover} mouse]
          (let [direction (m/v- mouse position)
                normalized (m/normalize direction)
                acceleration (m/v* normalized 0.2)
                velocity (m/limit (m/v+ velocity acceleration) topspeed)]
            (assoc mover :acceleration acceleration :velocity velocity
                   :position (m/v+ position velocity))))]
  (let [mover {:position [320 120] :velocity [0 0] :topspeed 10}]
    (update-toward-mouse mover (:mouse lesson/env))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch01" 67))

;; ## Clojure 实验 1.10

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "1.10")

(notebook/sketch "1.10")

(select-keys (:state (engine/run "1.10" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "1.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 1.1 · examples/01_vectors/exercise_1_1_walker_with_vectors

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/01_vectors/exercise_1_1_walker_with_vectors"))

(notebook/sketch (notebook/companion-id "examples/01_vectors/exercise_1_1_walker_with_vectors"))

(:_frame (:state (engine/run (notebook/companion-id "examples/01_vectors/exercise_1_1_walker_with_vectors") 12)))

;; ### Exercise 1.3 · examples/01_vectors/exercise_1_3_solution_3_d_bouncing_ball

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/01_vectors/exercise_1_3_solution_3_d_bouncing_ball"))

(notebook/sketch (notebook/companion-id "examples/01_vectors/exercise_1_3_solution_3_d_bouncing_ball"))

(:_frame (:state (engine/run (notebook/companion-id "examples/01_vectors/exercise_1_3_solution_3_d_bouncing_ball") 12)))

;; ### Exercise 1.5 · examples/01_vectors/exercise_1_5_solution_accelerate_and_decelerate

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/01_vectors/exercise_1_5_solution_accelerate_and_decelerate"))

(notebook/sketch (notebook/companion-id "examples/01_vectors/exercise_1_5_solution_accelerate_and_decelerate"))

(:_frame (:state (engine/run (notebook/companion-id "examples/01_vectors/exercise_1_5_solution_accelerate_and_decelerate") 12)))

;; ### Exercise 1.8 · examples/01_vectors/exercise_1_8_solution_attraction_magnitude

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/01_vectors/exercise_1_8_solution_attraction_magnitude"))

(notebook/sketch (notebook/companion-id "examples/01_vectors/exercise_1_8_solution_attraction_magnitude"))

(:_frame (:state (engine/run (notebook/companion-id "examples/01_vectors/exercise_1_8_solution_attraction_magnitude") 12)))

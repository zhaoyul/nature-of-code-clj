;; # 第 6 章: 物理库

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch06
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch06 :as ch]
            [noc.lessons.ch06 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 0))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：绘图入口使用notebook/scene；Quil已经由本工程的ClojureScript构建载入，不在正文嵌入JavaScript脚本。")

^{:nextjournal.clerk/no-cache true}
(do
(require '[noc.draw :as d]
         '[noc.notebook :as notebook])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 1))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Matter.js由noc.chapters.ch06的SAT碰撞、冲量响应与距离约束求解器替换，JVM和浏览器共用同一实现。")

^{:nextjournal.clerk/no-cache true}
(do
(require '[noc.math :as m]
         '[noc.chapters.ch06 :as ch])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 2))

^{:nextjournal.clerk/no-cache true}
(do
(let [v1 [1 -1]
      v2 [1 -1]]
  {:v1 v1 :v2 v2 :equal? (= v1 v2)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 3))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：向量运算返回新值，原输入保持不变；无需分别维护p5与Matter向量类型。")

^{:nextjournal.clerk/no-cache true}
(do
(let [a [1 -1] b [3 4]
      result (m/v+ a b)]
  {:original a :sum result})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [a [1 -1] b [3 4]
      c1 (m/v+ a b)
      c2 (m/v+ a b)]
  {:c1 c1 :c2 c2})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [v1 [1 -1] v2 [1 -1]]
  {:v1 (m/v* v1 4) :v2 (m/v* v2 4)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 6))

^{:nextjournal.clerk/no-cache true}
(do
(let [v [3 4]
      magnitude (m/mag v)
      unit-vector (m/normalize v)]
  {:magnitude magnitude :normalized unit-vector})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 7))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 为 Matter.Engine 创建较短的别名.
(let [world (lesson/make-world)
      dimensions {:width 640 :height 360}]
  {:world world :dimensions dimensions})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 8))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：重力方向通过环境参数传入，不修改全局引擎对象。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 关闭重力.
(let [horizontal-gravity (assoc lesson/env :gravity-vector [1 0])
      no-gravity (assoc lesson/env :gravity-vector [0 0])
      world (assoc (lesson/make-world) :bodies [lesson/box])]
  {:horizontal (-> (lesson/step-world world horizontal-gravity) :bodies first :velocity)
   :zero (-> (lesson/step-world world no-gravity) :bodies first :velocity)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 9))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：使用Clojure命名空间别名或局部函数绑定替代JavaScript对象解构。")

^{:nextjournal.clerk/no-cache true}
(do
(let [create-world lesson/make-world
      add-vectors m/v+
      create-box ch/box-body
      add-body lesson/add-body]
  (add-body (create-world) (create-box 0 (add-vectors [100 100] [0 0]) 50 50 1)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 10))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 100 y 100 width 50 height 50
      box (ch/box-body 0 [x y] width height 1)]
  box)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 11))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：密度乘面积得到质量；恢复系数由本求解器的环境参数控制。当前冲量求解器使用固定0.2摩擦系数，不声称支持Matter的逐体friction选项。")

^{:nextjournal.clerk/no-cache true}
(do
(let [width 50 height 50 density 0.002
      mass (* width height density)
      box (ch/box-body 0 [100 100] width height mass)
      settings (assoc lesson/env :restitution 0.8)
      world (lesson/add-body (lesson/make-world) box)]
  {:mass (:mass box) :world (lesson/step-world world settings)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 12))

^{:nextjournal.clerk/no-cache true}
(do
(let [box (assoc lesson/box :velocity [2 0] :angular-velocity 0.1)]
  (select-keys box [:position :velocity :angle :angular-velocity]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 13))

^{:nextjournal.clerk/no-cache true}
(do
(let [world (lesson/make-world)
      box lesson/box]
  (lesson/add-body world box))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 14))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：圆碰撞通过凸多边形近似进行SAT检测；摩擦参数沿用本求解器的固定系数。")

^{:nextjournal.clerk/no-cache true}
(do
(let [ball (ch/circle-body 0 [100 100] 20 1)
      settings (assoc lesson/env :restitution 0.8)]
  (lesson/step-world (lesson/add-body (lesson/make-world) ball) settings))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 15))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：渲染器消费世界快照并输出Quil画布，与物理更新解耦。")

^{:nextjournal.clerk/no-cache true}
(do
(let [world lesson/world
      commands (ch/world-scene world {:width 640 :height 360})]
  (notebook/scene commands))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 16))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：notebook以有限帧展示状态变化；相邻编号示例的Quil update循环持续调用同一物理函数。")

^{:nextjournal.clerk/no-cache true}
(do
(let [world lesson/world
      snapshots (take 4 (iterate lesson/step-world world))]
  (mapv #(mapv (fn [body] (select-keys body [:id :position :angle])) (:bodies %))
        snapshots))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 17))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [box (assoc (ch/box-body 0 [100 100] 50 50 1)
                 :velocity [5 0] :angular-velocity 0.1)
      ground (ch/static-body (ch/box-body 1 [320 355] 640 10 1))
      world (assoc (lesson/make-world) :bodies [box ground])
      settings (assoc lesson/env :restitution 0.75)
      result (lesson/simulate-rigid world settings 40)]
  (notebook/scene (lesson/world-scene result)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 19))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-box [x y] {:position [x y] :width 16})
        (show [{[x y] :position width :width}]
          [[:fill 127] [:stroke 0] [:stroke-weight 2]
           [:rect (- x (/ width 2)) (- y (/ width 2)) width width]])]
  (notebook/scene (d/scene (d/background) (show (make-box 200 120)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 20))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：把鼠标事件收到的坐标作为输入；相邻Quil示例实际监听点击。")

^{:nextjournal.clerk/no-cache true}
(do
(let [boxes []
      mouse [200 120]
      boxes (conj boxes {:position mouse :width 16})
      drawing (mapcat (fn [{[x y] :position width :width}]
                        [[:fill 127] [:stroke 0]
                         [:rect (- x (/ width 2)) (- y (/ width 2)) width width]]) boxes)]
  (notebook/scene (d/scene (d/background) drawing)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [world (lesson/make-world)
      boxes []]
  {:world world :boxes boxes :dimensions [640 240]})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 22))

^{:nextjournal.clerk/no-cache true}
(do
(let [world (lesson/step-world lesson/world)]
  (notebook/scene (lesson/world-scene world)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 23))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：封装层保留body-id；世界只保存一个刚体值，避免复制对象引用产生不同步状态。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-box [world id x y]
          (let [width 16
                body (ch/box-body id [x y] width width 1)]
            {:box {:width width :body-id id}
             :world (lesson/add-body world body)}))]
  (make-box (lesson/make-world) 0 200 120))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 24))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：局部顶点绕角度旋转后平移到世界坐标，对应push/translate/rotate/pop的绘图变换。")

^{:nextjournal.clerk/no-cache true}
(do
(let [body (assoc (ch/box-body 0 [200 120] 16 16 1) :angle 0.4)
      points (ch/shape-points body (first (:shapes body)))]
  (notebook/scene
    [[:background 255] [:fill 127] [:stroke 0] [:stroke-weight 2]
     [:polyline points true]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 25))

;; ## Clojure 实验 6.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.1")

(notebook/sketch "6.1")

(select-keys (:state (engine/run "6.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 26))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：同一世界集合同时承担更新、显示与移除，避免删除显示对象后仍保留物理刚体。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(spawn-box [world id position]
          (lesson/add-body world (ch/box-body id position 16 16 1)))
        (off-screen? [height body]
          (> (second (:position body)) (+ height 100)))
        (advance-and-remove [world height]
          (let [updated (lesson/step-world world)
                expired (map :id (filter #(off-screen? height %) (:bodies updated)))]
            (reduce lesson/remove-body updated expired)))]
  (let [world (-> (lesson/make-world)
                  (spawn-box 0 [200 120])
                  (spawn-box 1 [250 400]))
        world (advance-and-remove world 240)]
    (notebook/scene (lesson/world-scene world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 27))

^{:nextjournal.clerk/no-cache true}
(do
(let [ground (ch/static-body (ch/box-body 10 [320 355] 640 10 1))]
  (lesson/add-body (lesson/make-world) ground))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 28))

;; ## Clojure 实验 6.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.2")

(notebook/sketch "6.2")

(select-keys (:state (engine/run "6.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 29))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-boundary [id x y width height angle]
          (-> (ch/box-body id [x y] width height 1)
              ch/static-body
              (assoc :angle angle)))
        (show [boundary] (lesson/body-scene boundary))]
  (notebook/scene
    (d/scene (d/background) (show (make-boundary 0 320 300 300 15 0.2)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 30))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：自定义形状须为凸多边形；凹形用多个凸子形状构成compound-body。")

^{:nextjournal.clerk/no-cache true}
(do
(let [hexagon (ch/polygon-body 0 [100 100] (ch/random-polygon 6 25) 1)
      width 80 height 40 slope 0.25
      trapezoid (ch/polygon-body 1 [250 100]
                  [[(- (/ width 2)) (/ height 2)]
                   [(/ width 2) (/ height 2)]
                   [(* width (- 0.5 slope)) (- (/ height 2))]
                   [(* width (- slope 0.5)) (- (/ height 2))]] 1)
      custom (ch/polygon-body 2 [400 100] [[-20 -15] [25 -10] [10 25]] 1)]
  (notebook/scene
    (d/scene (d/background)
             (lesson/body-scene hexagon) (lesson/body-scene trapezoid)
             (lesson/body-scene custom))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 31))

;; ## Clojure 实验 6.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.3")

(notebook/sketch "6.3")

(select-keys (:state (engine/run "6.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 32))

^{:nextjournal.clerk/no-cache true}
(do
(let [body (assoc (ch/polygon-body 0 [250 120] (ch/random-polygon 6 30) 1)
                 :angle 0.3)
      vertices (ch/shape-points body (first (:shapes body)))]
  (notebook/scene
    [[:background 255] [:fill 127] [:stroke 0] [:stroke-weight 2]
     [:polyline vertices true]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 33))

^{:nextjournal.clerk/no-cache true}
(do
(let [part-a (lesson/translated-shape (ch/rectangle 40 10) [-20 0])
      part-b (lesson/translated-shape (ch/rectangle 40 10) [20 0])
      body (ch/compound-body 0 [250 120] [part-a part-b] 1)
      world (lesson/add-body (lesson/make-world) body)]
  (notebook/scene (lesson/world-scene world)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 34))

;; ## Clojure 实验 6.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.4")

(notebook/sketch "6.4")

(select-keys (:state (engine/run "6.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 35))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：圆与长柄共享位置、角度和角速度；恢复系数0.6在世界更新环境中传入。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-lollipop [id x y]
          (let [circle-part {:type :circle :radius 16 :offset [0 -20]}
                stem-part (lesson/translated-shape (ch/rectangle 8 60) [0 10])]
            (ch/compound-body id [x y] [circle-part stem-part] 1)))
        (show [body] (lesson/body-scene body))]
  (notebook/scene
    (d/scene (d/background) (show (assoc (make-lollipop 0 250 120) :angle 0.3)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 36))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：该求解器实现连接质心的距离约束，局部锚点均为[0 0]。")

^{:nextjournal.clerk/no-cache true}
(do
(let [body-a (ch/circle-body 0 [200 100] 10 1)
      body-b (ch/circle-body 1 [350 100] 10 1)
      constraint {:a 0 :b 1 :length 100 :stiffness 0.5}
      world (assoc (lesson/make-world)
                   :bodies [body-a body-b] :constraints [constraint])
      bodies (ch/solve-rigid-constraint (:bodies world) constraint)]
  {:distance (m/distance (:position (first bodies)) (:position (second bodies)))
   :bodies bodies})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 37))

;; ## Clojure 实验 6.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.5")

(notebook/sketch "6.5")

(select-keys (:state (engine/run "6.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 38))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-pendulum [x y length]
          (let [anchor (assoc (ch/static-body (ch/circle-body 0 [x y] 12 1))
                              :collision? false)
                bob (ch/circle-body 1 [(+ x length) y] 12 1)]
            (assoc (lesson/make-world) :bodies [anchor bob]
                   :constraints [{:a 0 :b 1 :length length :stiffness 1}])))]
  (let [world (make-pendulum 320 60 120)
        world (lesson/simulate-rigid world (assoc lesson/env :restitution 0.6) 40)]
    (notebook/scene (lesson/world-scene world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 39))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：固定世界坐标以静态且不碰撞的锚点表示，零长度约束形成中心铰链。")

^{:nextjournal.clerk/no-cache true}
(do
(let [body (ch/box-body 0 [320 150] 120 10 1)
      anchor (assoc (ch/static-body (ch/circle-body 1 [320 150] 1 1))
                    :collision? false)
      joint {:a 0 :b 1 :length 0 :stiffness 1}
      world (assoc (lesson/make-world) :bodies [body anchor] :constraints [joint])]
  (notebook/scene (lesson/world-scene (lesson/step-world world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 40))

;; ## Clojure 实验 6.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.6")

(notebook/sketch "6.6")

(select-keys (:state (engine/run "6.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 41))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-windmill [x y]
          (lesson/hinge (ch/box-body 0 [x y] 120 10 1) [x y]))]
  (let [world (make-windmill 320 160)
        world (update-in world [:bodies 0] assoc :angular-velocity 0.08)
        world (lesson/simulate-rigid world lesson/env 15)]
    (notebook/scene (lesson/world-scene world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 42))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Quil事件转成mouse与pressed?输入；求解器选取鼠标附近的动态刚体，再逐步向目标拉动。")

^{:nextjournal.clerk/no-cache true}
(do
(let [world (assoc (lesson/make-world) :mode :mouse :bodies [lesson/box])
      settings (assoc lesson/env :mouse (:position lesson/box) :pressed? true)
      selected (lesson/step-world world settings)]
  {:dragged-id (:drag selected)
   :position (-> selected :bodies first :position)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 43))

;; ## Clojure 实验 6.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.7")

(notebook/sketch "6.7")

(select-keys (:state (engine/run "6.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 44))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：当前求解器使用0.25的逐步拖拽权重；不假设Matter MouseConstraint对象存在。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 可选, 绘制抓取时的连接线.
(let [world (assoc (lesson/make-world) :mode :mouse :bodies [lesson/box])
      selected (lesson/step-world world
                 (assoc lesson/env :mouse (:position lesson/box) :pressed? true))
      mouse [200 80]
      dragged (lesson/step-world selected
                (assoc lesson/env :mouse mouse :pressed? true))
      body (first (:bodies dragged))]
  (notebook/scene
    (d/scene (lesson/world-scene dragged)
             (if (:drag dragged) (d/line (:position body) mouse) []))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 45))

^{:nextjournal.clerk/no-cache true}
(do
(let [body lesson/box
      force [0.01 0]
      body (lesson/force-on-body body (:position body) force)]
  (select-keys body [:velocity :angular-velocity]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 46))

;; ## Clojure 实验 6.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.8")

(notebook/sketch "6.8")

(select-keys (:state (engine/run "6.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 47))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(attract [body-a body-b gravitational-constant]
          (let [direction (m/v- (:position body-a) (:position body-b))
                distance (m/clamp (m/mag direction) 5 25)
                strength (/ (* gravitational-constant (:mass body-a) (:mass body-b))
                            (* distance distance))
                force (m/v* (m/normalize direction) strength)]
            (lesson/force-on-body body-b (:position body-b) force)))]
  (attract (ch/circle-body 0 [320 180] 20 10) lesson/box 0.4))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 48))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：新接触集合减去上一帧接触集合，形成collisionStart事件；重复接触不重复触发。")

^{:nextjournal.clerk/no-cache true}
(do
(let [before lesson/event-world
      after (lesson/step-world before (assoc lesson/env :gravity 0))]
  {:collision-start (:events after)
   :contact-set (:contacts after)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 49))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 根据碰撞双方执行逻辑.
(letfn [(handle-collisions [world events]
          (let [by-id (into {} (map (juxt :id identity) (:bodies world)))]
            (mapv (fn [[a b]]
                    {:body-a (get by-id a) :body-b (get by-id b)}) events)))]
  (let [world (lesson/step-world lesson/event-world (assoc lesson/env :gravity 0))]
    (handle-collisions world (:events world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 50))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：刚体map直接保存role与id，不需要plugin回指可变实例。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-particle [id x y radius]
          (assoc (ch/circle-body id [x y] radius 1) :role :particle))]
  (lesson/add-body (lesson/make-world) (make-particle 0 320 120 12)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 51))

;; ## Clojure 实验 6.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.9")

(notebook/sketch "6.9")

(select-keys (:state (engine/run "6.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 52))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(handle-collisions [world events]
          (let [by-id (into {} (map (juxt :id identity) (:bodies world)))
                changed (set (mapcat identity
                              (filter (fn [pair]
                                        (every? #(= :particle (:role (get by-id %))) pair))
                                      events)))]
            (update world :bodies
                    #(mapv (fn [body]
                             (if (changed (:id body))
                               (assoc body :color d/accent) body)) %))))]
  (let [world (update lesson/event-world :bodies
                       #(mapv (fn [body] (assoc body :role :particle)) %))
        world (lesson/step-world world (assoc lesson/env :gravity 0))]
    (notebook/scene
      (lesson/world-scene (handle-collisions world (:events world))))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 53))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [100 100] velocity [2 0] acceleration [0 0.5]
      next-velocity (m/v+ velocity acceleration)
      next-position (m/v+ position next-velocity)]
  {:position next-position :velocity next-velocity})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 54))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Toxiclibs由同一Clojure模块的Verlet积分、锁定节点与弹簧松弛函数替换。")

^{:nextjournal.clerk/no-cache true}
(do
(require '[noc.chapters.ch06 :as ch]
         '[noc.lessons.ch06 :as lesson])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 55))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：二维物理使用两个分量；三维数据仍可用普通Clojure向量表示。")

^{:nextjournal.clerk/no-cache true}
(do
(let [vec2 [1 -1]
      vec3 [1 -1 2]]
  {:vec2 vec2 :vec3 vec3 :dimensions (mapv count [vec2 vec3])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 56))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：统一使用不可变向量，addSelf的原地变更改为显式绑定新值。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; Vec2D, 原地修改使用 addSelf().
  ;; 创建新向量.
(let [a [1 -1] b [3 4]
      sum (m/v+ a b)
      copied-sum (m/v+ sum b)]
  {:original a :sum sum :copied-sum copied-sum
   :magnitude (m/mag sum) :unit-vector (m/normalize sum)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 57))

^{:nextjournal.clerk/no-cache true}
(do
(let [make-world lesson/make-verlet-world
      make-particle ch/particle
      make-spring ch/spring
      world (make-world)]
  (assoc world :particles [(make-particle 0 [320 120] false)]
               :springs []))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 58))

^{:nextjournal.clerk/no-cache true}
(do
(let [world (assoc (lesson/make-verlet-world)
                   :particles [(lesson/particle 0 320 120 8)])
      settings (assoc lesson/env :height 240 :gravity 0.5)
      next-world (ch/verlet-step world settings)]
  {:bounds [0 0 640 240]
   :position (-> next-world :particles first :position)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 59))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-particle [id x y radius]
          (assoc (ch/particle id [x y] false) :radius radius))
        (show [particle]
          (d/circle (:position particle) (* 2 (:radius particle)) [127]))]
  (notebook/scene
    (d/scene (d/background) (show (make-particle 0 320 120 8)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 60))

^{:nextjournal.clerk/no-cache true}
(do
(let [particle (lesson/particle 0 320 120 8)]
  (update (lesson/make-verlet-world) :particles conj particle))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 61))

^{:nextjournal.clerk/no-cache true}
(do
(let [length 80 strength 0.01
      spring (ch/spring 0 1 length strength)
      world (assoc (lesson/make-verlet-world)
                   :particles lesson/particles :springs [spring])]
  {:spring spring :relaxed (ch/relax-spring (:particles world) spring)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 62))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：拖拽时同步previous以避免释放后产生不期望的瞬时速度；固定节点的locked?由场景控制。")

^{:nextjournal.clerk/no-cache true}
(do
(let [world lesson/spring-world
      mouse [400 160]
      world (lesson/drag-particle world 1 mouse)]
  (-> world :particles second (select-keys [:position :previous :locked?])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 63))

;; ## Clojure 实验 6.10

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.10")

(notebook/sketch "6.10")

(select-keys (:state (engine/run "6.10" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 64))

^{:nextjournal.clerk/no-cache true}
(do
(let [length 120
      anchor (assoc (lesson/particle 0 320 20 8) :locked? true)
      bob (lesson/particle 1 (+ 320 length) 20 8)
      spring (ch/spring 0 1 length 0.01)
      world {:particles [anchor bob] :springs [spring] :attractors []}
      settings (assoc lesson/env :height 240 :gravity 0.5)
      world (lesson/simulate-springs world settings 25)]
  (notebook/scene (lesson/spring-scene world)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 65))

^{:nextjournal.clerk/no-cache true}
(do
(let [total 20 spacing 10
      particles (mapv (fn [id]
                        (lesson/particle id (* id spacing) 10 4))
                      (range total))]
  (assoc (lesson/make-verlet-world) :particles particles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 66))

^{:nextjournal.clerk/no-cache true}
(do
(let [total 20 spacing 10
      particles (mapv #(lesson/particle % (* % spacing) 10 4) (range total))
      springs (mapv #(ch/spring % (inc %) spacing 0.01) (range (dec total)))]
  {:particles (assoc-in particles [0 :locked?] true)
   :springs springs :attractors []})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 67))

;; ## Clojure 实验 6.11

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.11")

(notebook/sketch "6.11")

(select-keys (:state (engine/run "6.11" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 68))

^{:nextjournal.clerk/no-cache true}
(do
(let [world (lesson/simulate-springs (lesson/chain 20 10) lesson/env 20)
      particles (:particles world)
      points (mapv :position particles)
      last-particle (peek particles)]
  (notebook/scene
    (d/scene (d/background)
             [[:stroke 0] [:no-fill] [:polyline points false]]
             (d/circle (:position last-particle) (* 2 (:radius last-particle)) [127]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 69))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：构造函数自动注册改为返回更新后的世界，所有状态变更明确可见。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(add-particle [world id x y radius]
          (update world :particles conj (lesson/particle id x y radius)))
        (add-spring [world a b]
          (let [by-id (into {} (map (juxt :id identity) (:particles world)))
                length (m/distance (:position (get by-id a)) (:position (get by-id b)))]
            (update world :springs conj (ch/spring a b length 0.01))))]
  (-> (lesson/make-verlet-world)
      (add-particle 0 200 100 4)
      (add-particle 1 300 100 4)
      (add-spring 0 1)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 70))

;; ## Clojure 实验 6.12

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.12")

(notebook/sketch "6.12")

(select-keys (:state (engine/run "6.12" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 71))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 内部支撑.
(let [positions [[200 25] [400 25] [350 125]
                 [400 225] [200 225] [250 125]]
      particles (mapv (fn [id [x y]] (lesson/particle id x y 4)) (range) positions)
      links [[0 1] [1 2] [2 3] [3 4] [4 5] [5 0]
             [5 2] [0 3] [1 4]]
      springs (mapv (fn [[a b]]
                      (ch/spring a b (m/distance (nth positions a) (nth positions b)) 0.01))
                    links)
      world {:particles particles :springs springs :attractors []}
      world (lesson/simulate-springs world (assoc lesson/env :height 240 :gravity 0.5) 20)
      points (mapv :position (:particles world))]
  (notebook/scene
    [[:background 255] [:fill 127] [:stroke 0] [:polyline points true]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 72))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [n 8
        particles (mapv (fn [id]
                          (lesson/particle id (m/random 319 321) (m/random 119 121) 4))
                        (range n))]
    {:particles particles :springs [] :attractors []}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 73))

^{:nextjournal.clerk/no-cache true}
(do
(let [n 8 length 60
      springs (vec (for [a (range n) b (range (inc a) n)]
                     (ch/spring a b length 0.01)))]
  {:spring-count (count springs) :springs springs})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 74))

;; ## Clojure 实验 6.13

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.13")

(notebook/sketch "6.13")

(select-keys (:state (engine/run "6.13" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 75))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [n (+ 2 (m/random-int 18))
        length (m/random 10 120)
        world (lesson/cluster n length)
        world (lesson/simulate-springs world (assoc lesson/env :height 240 :gravity 0) 40)]
    (notebook/scene (lesson/spring-scene world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 76))

^{:nextjournal.clerk/no-cache true}
(do
(let [particle (lesson/particle 0 320 120 8)
      behavior {:position (:position particle) :radius 20 :strength 0.1}
      world {:particles [(lesson/particle 1 330 120 4)]
             :springs [] :attractors [behavior]}
      next-world (ch/verlet-step world (assoc lesson/env :gravity 0))]
  {:behavior behavior :position (-> next-world :particles first :position)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 77))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-repeller [id x y radius]
          (let [particle (lesson/particle id x y radius)]
            {:particle particle
             :behavior {:position (:position particle) :radius (* radius 4) :strength -1}}))]
  (let [{:keys [particle behavior]} (make-repeller 0 320 120 8)
        world {:particles [particle (lesson/particle 1 330 120 4)]
               :springs [] :attractors [behavior]}
        next-world (ch/verlet-step world (assoc lesson/env :gravity 0))]
    (notebook/scene (lesson/spring-scene next-world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 78))

;; ## Clojure 实验 6.14

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.14")

(notebook/sketch "6.14")

(select-keys (:state (engine/run "6.14" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 79))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：固定吸引子供有限实验使用；近场负强度排斥、远场正强度吸引，实际交互例子允许移动吸引子。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 在较大范围内吸引.
  ;; 在自身半径内强烈排斥.
(letfn [(make-attractor [id x y radius width]
          (let [particle (lesson/particle id x y radius)]
            {:particle (assoc particle :locked? true)
             :behaviors [{:position [x y] :radius width :strength 0.1}
                         {:position [x y] :radius radius :strength -10}]}))]
  (let [{:keys [particle behaviors]} (make-attractor 0 320 120 20 640)
        world {:particles [particle (lesson/particle 1 330 120 4)
                           (lesson/particle 2 450 120 4)]
               :springs [] :attractors behaviors}
        next-world (ch/verlet-step world (assoc lesson/env :gravity 0))]
    (notebook/scene (lesson/spring-scene next-world))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch06" 80))

;; ## Clojure 实验 6.15

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "6.15")

(notebook/sketch "6.15")

(select-keys (:state (engine/run "6.15" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "6.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 6.2 · examples/06_libraries/exercise_6_2_boxes

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/exercise_6_2_boxes"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/exercise_6_2_boxes"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/exercise_6_2_boxes") 12)))

;; ### Example 6.5: Multiple Shapes on One Body · examples/06_libraries/6_5_compound_bodies_error

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/6_5_compound_bodies_error"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/6_5_compound_bodies_error"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/6_5_compound_bodies_error") 12)))

;; ### Exercise 6.5 · examples/06_libraries/exercise_6_5_bridge

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/exercise_6_5_bridge"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/exercise_6_5_bridge"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/exercise_6_5_bridge") 12)))

;; ### Exercise 6.7 · examples/06_libraries/exercise_6_7_windmill_motor

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/exercise_6_7_windmill_motor"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/exercise_6_7_windmill_motor"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/exercise_6_7_windmill_motor") 12)))

;; ### Exercise 6.10 · examples/06_libraries/exercise_6_10_cloth_simulation

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/exercise_6_10_cloth_simulation"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/exercise_6_10_cloth_simulation"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/exercise_6_10_cloth_simulation") 12)))

;; ### Exercise 6.11 · examples/06_libraries/exercise_6_11_soft_body_character_enhanced

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/exercise_6_11_soft_body_character_enhanced"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/exercise_6_11_soft_body_character_enhanced"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/exercise_6_11_soft_body_character_enhanced") 12)))

;; ### A Force-Directed Graph · examples/06_libraries/figure_6_18

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/figure_6_18"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/figure_6_18"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/figure_6_18") 12)))

;; ### Exercise 6.13 · examples/06_libraries/exercise_6_13_force_directed_graph

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/06_libraries/exercise_6_13_force_directed_graph"))

(notebook/sketch (notebook/companion-id "examples/06_libraries/exercise_6_13_force_directed_graph"))

(:_frame (:state (engine/run (notebook/companion-id "examples/06_libraries/exercise_6_13_force_directed_graph") 12)))

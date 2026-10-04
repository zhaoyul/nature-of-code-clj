;; # 第 5 章: 自主智能体

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch05
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch05 :as ch]
            [noc.lessons.ch05 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 0))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：类的实例字段改为不可变map；后续函数返回更新后的载具。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 还需要添加什么?
(let [vehicle {:position [(/ (:width lesson/env) 2)
                         (/ (:height lesson/env) 2)]
               :velocity [0 0]
               :acceleration [0 0]}]
  vehicle)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 1))

^{:nextjournal.clerk/no-cache true}
(do
(let [desired [8 0]
      velocity [2 -1]
      steer (m/v- desired velocity)]
  steer)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 2))

^{:nextjournal.clerk/no-cache true}
(do
(let [target (:mouse lesson/env)
      position (:position lesson/vehicle)
      desired (m/v- target position)]
  desired)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 3))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：教材留空的参数取可调整初值8，保证片段可以执行。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 最高速度
(let [vehicle {:position [320 180] :velocity [0 0]
               :acceleration [0 0] :max-speed 8}]
  vehicle)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [vehicle lesson/vehicle
      desired (m/set-mag (m/v- (:mouse lesson/env) (:position vehicle))
                         (:max-speed vehicle))]
  desired)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 5))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 计算以最高速度指向目标的期望速度.
  ;; Reynolds 转向力公式
  ;; 使用物理模型, 把力施加到
  ;; 对象的加速度.
(letfn [(seek [vehicle target]
          (let [desired (m/set-mag (m/v- target (:position vehicle))
                                  (:max-speed vehicle))
                steer (m/v- desired (:velocity vehicle))]
            (update vehicle :acceleration m/v+ steer)))]
  (seek lesson/vehicle (:mouse lesson/env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 6))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 最高速度
  ;; 现在再加入最大转向力.
(let [vehicle {:position [320 180] :velocity [0 0]
               :acceleration [0 0] :max-speed 8 :max-force 0.2}]
  vehicle)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 7))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 限制转向力的大小.
(letfn [(seek [vehicle target]
          (let [desired (m/set-mag (m/v- target (:position vehicle))
                                  (:max-speed vehicle))
                steer (m/limit (m/v- desired (:velocity vehicle))
                               (:max-force vehicle))]
            (lesson/apply-force vehicle steer)))]
  (seek lesson/vehicle (:mouse lesson/env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 8))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 9))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：绘图输出是noc.draw指令；相邻示例5.1由同一套向量规则驱动实际Quil画布。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 额外的尺寸变量
  ;; 最高速度和最大力使用任意初值, 可以尝试调整
  ;; 标准更新函数
  ;; 牛顿第二定律, 省略质量计算
  ;; 寻找行为的转向力算法
  ;; 载具绘制成朝向速度方向的三角形.
(letfn [(make-vehicle [x y]
          {:id 0 :position [x y] :velocity [0 0] :acceleration [0 0]
           :radius 6 :max-speed 8 :max-force 0.2})
        (apply-force [vehicle force]
          (update vehicle :acceleration m/v+ force))
        (advance [vehicle]
          (let [velocity (m/limit (m/v+ (:velocity vehicle)
                                      (:acceleration vehicle))
                                  (:max-speed vehicle))]
            (assoc vehicle :velocity velocity
                   :position (m/v+ (:position vehicle) velocity)
                   :acceleration [0 0])))
        (seek [vehicle target]
          (let [desired (m/set-mag (m/v- target (:position vehicle))
                                  (:max-speed vehicle))
                steer (m/limit (m/v- desired (:velocity vehicle))
                               (:max-force vehicle))]
            (apply-force vehicle steer)))
        (show [vehicle]
          (lesson/triangle-scene vehicle))]
  (let [vehicle (-> (make-vehicle 160 180)
                    (seek (:mouse lesson/env))
                    advance)]
    (notebook/scene (d/scene (d/background) (show vehicle)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 10))

^{:nextjournal.clerk/no-cache true}
(do
(let [vehicle lesson/vehicle
      desired (m/set-mag (m/v- (:mouse lesson/env) (:position vehicle))
                         (:max-speed vehicle))]
  desired)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 11))

^{:nextjournal.clerk/no-cache true}
(do
(let [desired (m/v- (:mouse lesson/env) (:position lesson/vehicle))]
  (m/v* desired 0.5))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 12))

^{:nextjournal.clerk/no-cache true}
(do
(let [desired (m/v- (:mouse lesson/env) (:position lesson/vehicle))]
  (m/v* desired 0.05))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 13))

;; ## Clojure 实验 5.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.1")

(notebook/sketch "5.1")

(select-keys (:state (engine/run "5.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 14))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 距离等于这个向量的大小:
  ;; 它从当前位置指向
  ;; 目标位置.
  ;; 如果距离小于 100 像素...
  ;; ...根据距离设置期望速度大小.
  ;; 否则以最高速度前进.
  ;; 常规转向力 = 期望速度 - 当前速度
(letfn [(arrive [vehicle target]
          (let [delta (m/v- target (:position vehicle))
                distance (m/mag delta)
                speed (if (< distance 100)
                        (m/map-range distance 0 100 0 (:max-speed vehicle))
                        (:max-speed vehicle))
                desired (m/set-mag delta speed)
                steer (m/limit (m/v- desired (:velocity vehicle))
                               (:max-force vehicle))]
            (lesson/apply-force vehicle steer)))]
  (arrive lesson/vehicle [190 190]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 15))

;; ## Clojure 实验 5.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.2")

(notebook/sketch "5.2")

(select-keys (:state (engine/run "5.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 16))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：nil对应尚未需要转向；保持原例先检查x、再检查y的顺序。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 方法接收距边缘的偏移量.
  ;; 期望速度初始为 null.
  ;; 构造期望速度, 保留载具的 y 方向,
  ;; 同时让 x 方向直接远离
  ;; 画布边缘.
  ;; 若期望速度非 null, 则应用转向力.
(letfn [(boundaries [vehicle offset width height]
          (let [[x y] (:position vehicle)
                [vx vy] (:velocity vehicle)
                speed (:max-speed vehicle)
                desired-x (cond (< x offset) [speed vy]
                                (> x (- width offset)) [(- speed) vy])
                desired (cond (< y offset) [vx speed]
                              (> y (- height offset)) [vx (- speed)]
                              :else desired-x)]
            (if desired
              (lesson/apply-force vehicle
                (lesson/steer vehicle (m/set-mag desired speed)))
              vehicle)))]
  (boundaries (assoc lesson/vehicle :position [10 180]) 40 640 360))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 17))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 网格分辨率, 单位为相对于画布的像素
  ;; 网格有多少列和多少行?
  ;; 流场是一个向量二维数组.
(let [resolution 20
      cols (long (m/floor (/ (:width lesson/env) resolution)))
      rows (long (m/floor (/ (:height lesson/env) resolution)))
      flow {:resolution resolution :cols cols :rows rows
            :field (vec (repeat cols (vec (repeat rows nil))))}]
  (select-keys flow [:resolution :cols :rows]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 18))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 总列数等于宽度除以分辨率.
  ;; 总行数等于高度除以分辨率.
  ;; 流场是一个向量二维数组.
(let [resolution 10
      cols (long (m/floor (/ (:width lesson/env) resolution)))
      rows (long (m/floor (/ (:height lesson/env) resolution)))]
  {:resolution resolution :cols cols :rows rows
   :field (vec (repeat cols (vec (repeat rows nil))))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 19))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 用嵌套循环访问每一列
  ;; 以及流场的每一行.
  ;; 任意设定每个向量都指向右侧
(let [cols 4 rows 3
      field (mapv (fn [_] (mapv (fn [_] [1 0]) (range rows)))
                  (range cols))]
  field)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 20))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 随机向量
(binding [m/*rng* (m/seeded 42)]
  (let [cols 4 rows 3]
    (mapv (fn [_] (mapv (fn [_] (m/random-vector)) (range rows)))
          (range cols))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 21))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 二维噪声
(let [cols 4 rows 3
      field (mapv (fn [column]
                    (mapv (fn [row]
                            (let [angle (* m/tau (m/noise (* column 0.1)
                                                        (* row 0.1)))]
                              (m/from-angle angle)))
                          (range rows)))
                  (range cols))]
  field)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 22))

^{:nextjournal.clerk/no-cache true}
(do
(let [cols 4 rows 3 width 640 height 360]
  (mapv (fn [column]
          (mapv (fn [row]
                  (let [x (* column (/ width cols))
                        y (* row (/ height rows))
                        to-center [(- (/ width 2) x) (- (/ height 2) y)]]
                    (m/rotate to-center (/ m/pi 2))))
                (range rows)))
        (range cols)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 23))

^{:nextjournal.clerk/no-cache true}
(do
(let [[x y] (:position lesson/vehicle)
      resolution (:resolution lesson/flow)]
  {:column (long (m/floor (/ x resolution)))
   :row (long (m/floor (/ y resolution)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 24))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Clojure向量不可变，因此无需copy即可安全返回。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 使用 constrain().
  ;; 使用 copy() 确保返回向量副本.
(letfn [(lookup [{:keys [resolution cols rows field]} [x y]]
          (let [column (long (m/clamp (m/floor (/ x resolution)) 0 (dec cols)))
                row (long (m/clamp (m/floor (/ y resolution)) 0 (dec rows)))]
            (get-in field [column row])))]
  (lookup lesson/flow (:position lesson/vehicle)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 25))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：noc.math的Perlin噪声使用确定梯度；种子转换为噪声坐标偏移以生成不同的连续流场。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 计算列数和行数.
  ;; 流场是向量二维数组, 示例用独立代码创建该数组.
  ;; init() 用向量填充二维数组.
  ;; 每次为新流场重新设置噪声种子.
  ;; 本例使用 Perlin 噪声创建向量.
  ;; 根据位置返回向量的函数
(letfn [(make-flow [resolution seed]
          (let [cols (long (m/floor (/ (:width lesson/env) resolution)))
                rows (long (m/floor (/ (:height lesson/env) resolution)))
                offset (* seed 0.01)]
            {:resolution resolution :cols cols :rows rows
             :field (mapv (fn [column]
                            (mapv (fn [row]
                                    (m/from-angle
                                      (* m/tau (m/noise (+ offset (* column 0.1))
                                                          (+ offset (* row 0.1))))))
                                  (range rows)))
                          (range cols))}))
        (lookup [flow position]
          (lesson/lookup flow position))]
  (let [flow (make-flow 20 42)]
    {:cols (:cols flow) :rows (:rows flow)
     :direction (lookup flow (:position lesson/vehicle))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 26))

;; ## Clojure 实验 5.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.3")

(notebook/sketch "5.3")

(select-keys (:state (engine/run "5.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 27))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 该位置的流场向量是什么?
  ;; 转向力等于期望速度减去当前速度.
(letfn [(follow-flow [vehicle flow]
          (let [desired (m/set-mag (lesson/lookup flow (:position vehicle))
                                  (:max-speed vehicle))
                steer (lesson/steer vehicle desired)]
            (lesson/apply-force vehicle steer)))]
  (follow-flow lesson/vehicle lesson/flow))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 28))

^{:nextjournal.clerk/no-cache true}
(do
  ;; p5.Vector 提供计算点积的方法.
(let [a [-3 5]
      b [10 1]
      n (m/dot a b)]
  n)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 29))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：二维输入的第三个分量补为0；同一函数也接受三维向量。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 对于二维向量, z 为 0.
(letfn [(dot [[ax ay az] [bx by bz]]
          (+ (* ax bx) (* ay by) (* (or az 0) (or bz 0))))]
  (dot [-3 5] [10 1]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 30))

^{:nextjournal.clerk/no-cache true}
(do
(let [a [10 2]
      b [4 -3]
      angle (Math/acos (double (/ (m/dot a b) (* (m/mag a) (m/mag b)))))]
  {:radians angle :degrees (* angle (/ 180 m/pi))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 31))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：限制余弦值避免浮点误差越界；零向量约定返回0。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(angle-between [a b]
          (let [denominator (* (m/mag a) (m/mag b))]
            (if (zero? denominator)
              0
              (Math/acos (double (m/clamp (/ (m/dot a b) denominator) -1 1))))))]
  (angle-between [10 2] [4 -3]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 32))

;; ## Clojure 实验 5.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.4")

(notebook/sketch "5.4")

(select-keys (:state (engine/run "5.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 33))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 路径具有表示宽度的半径.
  ;; 路径只有起点和终点两个点.
  ;; 绘制路径.
(letfn [(make-path [width height]
          {:radius 20 :start [0 (/ height 3)]
           :end [width (/ (* height 2) 3)]})
        (show [{:keys [radius start end]}]
          (d/scene [[:stroke-weight (* radius 2)] [:stroke 0 100]]
                   (d/line start end [0 100])
                   [[:stroke-weight 1]] (d/line start end [0])))]
  (notebook/scene (show (make-path 640 360))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 34))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 先复制速度向量.
  ;; 把大小设为 25, 向前预测 25 像素.
  ;; 与当前位置相加得到未来位置.
(let [vehicle lesson/vehicle
      future (m/v+ (:position vehicle) (m/set-mag (:velocity vehicle) 25))]
  future)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 35))

^{:nextjournal.clerk/no-cache true}
(do
(let [future lesson/future
      path lesson/path
      a (m/v- future (:start path))]
  a)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 36))

^{:nextjournal.clerk/no-cache true}
(do
(let [path lesson/path
      b (m/v- (:end path) (:start path))]
  b)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 37))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 取得起点到法足的距离.
  ;; 把向量 b 缩放到该距离.
  ;; 将缩放后的 b 与路径起点相加得到法足.
(let [path lesson/path
      a (m/v- lesson/future (:start path))
      b (m/v- (:end path) (:start path))
      theta (lesson/angle-between a b)
      distance (* (m/mag a) (m/cos theta))
      normal-point (m/v+ (:start path) (m/set-mag b distance))]
  normal-point)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 38))

^{:nextjournal.clerk/no-cache true}
(do
  ;; theta 是 A 与 B 的夹角.
(let [path lesson/path
      a (m/v- lesson/future (:start path))
      b (m/v- (:end path) (:start path))
      theta (lesson/angle-between a b)
      distance (* (m/mag a) (m/cos theta))]
  (m/v+ (:start path) (m/set-mag b distance)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 39))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：标量投影可以为负；用v*保留投影符号，避免负长度处理歧义。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 归一化 b, 并用点积设置 b 的长度.
(let [path lesson/path
      a (m/v- lesson/future (:start path))
      b (m/normalize (m/v- (:end path) (:start path)))
      scalar-projection (m/dot a b)
      normal-point (m/v+ (:start path) (m/v* b scalar-projection))]
  {:scalar-projection scalar-projection :normal-point normal-point})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 40))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 若载具位于路径外, 则寻找目标.
  ;; 期望速度和转向力可复用示例 5.1 的 seek().
(let [vehicle lesson/vehicle path lesson/path
      future lesson/future normal-point lesson/normal
      target (m/v+ normal-point
                   (m/set-mag (m/v- (:end path) (:start path)) 25))
      distance (m/distance future normal-point)]
  (if (> distance (:radius path))
    (lesson/seek vehicle target)
    vehicle))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 41))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 把大小任意设为 25 像素.
  ;; 把 b 加到 normalPoint, 得到路径前方 25 像素的目标.
  ;; 寻找目标.
(let [vehicle lesson/vehicle path lesson/path
      future lesson/future normal-point lesson/normal
      distance (m/distance future normal-point)]
  (if (> distance (:radius path))
    (let [ahead (m/set-mag (m/v- (:end path) (:start path)) 25)
          target (m/v+ normal-point ahead)]
      (lesson/seek vehicle target))
    vehicle))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 42))

;; ## Clojure 实验 5.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.5")

(notebook/sketch "5.5")

(select-keys (:state (engine/run "5.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 43))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 步骤 1: 预测载具未来位置.
  ;; 步骤 2: 求路径上的法足.
  ;; 步骤 3: 沿路径向前观察并设置目标.
  ;; 为优化, 可仅在偏离路径时计算目标.
  ;; 步骤 4: 若偏离路径,
  ;; 寻找该目标以返回路径.
(letfn [(follow-path [vehicle {:keys [start end radius]}]
          (let [future (m/v+ (:position vehicle)
                             (m/set-mag (:velocity vehicle) 25))
                normal-point (lesson/normal-point future start end)
                target (m/v+ normal-point (m/set-mag (m/v- end start) 25))
                distance (m/distance normal-point future)]
            (if (> distance radius)
              (lesson/seek vehicle target)
              vehicle)))]
  (follow-path lesson/vehicle lesson/path))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 44))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 从 a 指向 position 的向量
  ;; 从 a 指向 b 的向量
  ;; 使用点积计算标量投影.
  ;; 求线段上的法足.
(letfn [(normal-point [position a b]
          (let [vector-a (m/v- position a)
                vector-b (m/normalize (m/v- b a))
                projection (m/v* vector-b (m/dot vector-a vector-b))]
            (m/v+ a projection)))]
  (normal-point lesson/future (:start lesson/path) (:end lesson/path)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 45))

;; ## Clojure 实验 5.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.6")

(notebook/sketch "5.6")

(select-keys (:state (engine/run "5.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 46))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 路径现在是点, 即 p5.Vector, 的数组.
  ;; 此方法用于向路径添加点.
  ;; 用较粗灰线绘制路径半径.
  ;; 用细线绘制路径中心.
(letfn [(make-path [] {:radius 20 :points []})
        (add-point [path x y] (update path :points conj [x y]))
        (show [{:keys [radius points]}]
          [[:stroke 200] [:stroke-weight (* radius 2)] [:no-fill]
           [:polyline points false] [:stroke 0] [:stroke-weight 1]
           [:polyline points false]])]
  (-> (make-path)
      (add-point 0 120)
      (add-point 160 70)
      (add-point 320 280)
      show
      notebook/scene))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 47))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 计算每条线段的法足.
(let [future lesson/future]
  (mapv (fn [[a b]]
          {:segment [a b] :normal-point (lesson/normal-point future a b)})
        (partition 2 1 (:points lesson/poly-path))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 48))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原例的x范围检查适用于从左向右的路径；任意方向路径可用ch/nearest-point的投影参数限制。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 如果无法得到在线段内的法足,
  ;; 使用线段终点作为法足.
(let [a [0 120] b [160 70]
      normal-point (lesson/normal-point [200 90] a b)]
  (if (or (< (first normal-point) (first a))
          (> (first normal-point) (first b)))
    b
    normal-point))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 49))

;; ## Clojure 实验 5.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.7")

(notebook/sketch "5.7")

(select-keys (:state (engine/run "5.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 50))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：只在刷新最短距离时保存对应线段的前方目标，避免原片段在之后的线段上继续累加方向。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 从易于被打破的极大纪录开始, 例如 Infinity.
  ;; 若刷新纪录, 该点成为目标.
  ;; 沿线段方向在法足前方寻找目标.
(let [future lesson/future
      best (reduce
             (fn [record [a b]]
               (let [normal (lesson/normal-point future a b)
                     normal (if (or (< (first normal) (first a))
                                    (> (first normal) (first b))) b normal)
                     distance (m/distance future normal)]
                 (if (< distance (:distance record))
                   {:distance distance :normal normal
                    :target (m/v+ normal (m/set-mag (m/v- b a) 25))}
                   record)))
             {:distance Double/POSITIVE_INFINITY :target nil}
             (partition 2 1 (:points lesson/poly-path)))]
  best)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 51))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 声明 Vehicle 对象数组.
  ;; 初始化数组并填入
  ;; 一组载具.
(binding [m/*rng* (m/seeded 42)]
  (let [vehicles (mapv (fn [id]
                         (lesson/make-vehicle id (m/random (:width lesson/env))
                                              (m/random (:height lesson/env))))
                       (range 100))]
    {:count (count vehicles) :sample (take 3 vehicles)}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 52))

^{:nextjournal.clerk/no-cache true}
(do
(let [vehicles (mapv lesson/advance lesson/vehicles)]
  (notebook/scene (lesson/scene vehicles)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 53))

^{:nextjournal.clerk/no-cache true}
(do
(let [mouse (:mouse lesson/env)]
  (lesson/seek lesson/vehicle mouse))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 54))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：分离行为必须感知其他载具；不给邻居时返回零力，下一块传入整个群体。")

^{:nextjournal.clerk/no-cache true}
(do
(let [vehicle lesson/vehicle
      neighbors []
      force (lesson/separate-force vehicle neighbors)]
  (lesson/apply-force vehicle force))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 55))

^{:nextjournal.clerk/no-cache true}
(do
(let [vehicle lesson/vehicle
      force (lesson/separate-force vehicle lesson/vehicles)]
  (lesson/apply-force vehicle force))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 56))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这是本节真正的新内容: 让
  ;; Vehicle 在计算分离力时检查
  ;; 其他所有载具.
(binding [m/*rng* (m/seeded 42)]
  (let [vehicles (mapv (fn [id]
                         (lesson/make-vehicle id (m/random 640) (m/random 240)))
                       (range 100))
        next-vehicles (mapv (fn [vehicle]
                              (-> vehicle
                                  (lesson/apply-force
                                    (lesson/separate-force vehicle vehicles))
                                  lesson/advance))
                            vehicles)]
    (notebook/scene (lesson/scene next-vehicles))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 57))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(separate [vehicle vehicles]
          ;; 尚未加入邻居规则时，分离力为零。
          [0 0])]
  (separate lesson/vehicle lesson/vehicles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 58))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 此变量规定多近算作过近.
  ;; 当前载具与其他载具的距离是多少?
  ;; 若载具在 20 像素内, 执行此处代码.
(let [vehicle lesson/vehicle
      desired-separation 20]
  (filterv (fn [other]
             (let [distance (m/distance (:position vehicle) (:position other))]
               (and (not= (:id vehicle) (:id other))
                    (< distance desired-separation))))
           lesson/vehicles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 59))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 指离其他载具位置的向量
(let [vehicle lesson/vehicle other (second lesson/vehicles)
      distance (m/distance (:position vehicle) (:position other))]
  (when (and (not= (:id vehicle) (:id other)) (< distance 20))
    (m/normalize (m/v- (:position vehicle) (:position other)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 60))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 从空向量开始.
  ;; 记录有多少载具过近.
  ;; 累加向量并增加计数.
  ;; 确保至少存在一个过近
  ;; 载具. 若没有过近对象则无需操作,
  ;; 而且不能
  ;; 除以 0.
(let [vehicle lesson/vehicle
      near (lesson/nearby vehicle lesson/vehicles 20)
      sum (reduce m/v+ [0 0]
                  (map #(m/normalize (m/v- (:position vehicle) (:position %))) near))
      count-near (count near)]
  {:count count-near :sum sum
   :average (if (pos? count-near) (m/vdiv sum count-near) [0 0])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 61))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 把平均向量缩放到最高速度
  ;; 它将成为期望速度.
  ;; Reynolds 转向公式
  ;; 把力施加到载具.
(let [vehicle lesson/vehicle
      near (lesson/nearby vehicle lesson/vehicles 20)
      sum (reduce m/v+ [0 0]
                  (map #(m/normalize (m/v- (:position vehicle) (:position %))) near))]
  (if (seq near)
    (let [desired (m/set-mag sum (:max-speed vehicle))
          steer (lesson/steer vehicle desired)]
      (lesson/apply-force vehicle steer))
    vehicle))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 62))

;; ## Clojure 实验 5.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.8")

(notebook/sketch "5.8")

(select-keys (:state (engine/run "5.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 63))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：只检查0<距离的邻居，防止重合载具导致1/d除零。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 期望分离距离基于
  ;; 载具尺寸.
  ;; 远离邻居的 p5.Vector
  ;; 应具有多大大小?
  ;; 越近越应强烈逃离.
  ;; 越远影响越小, 因此大小设为
  ;; 与距离成反比.
(letfn [(separate [vehicle vehicles]
          (let [near (lesson/nearby vehicle vehicles (* 2 (:radius vehicle)))
                sum (reduce m/v+ [0 0]
                            (map (fn [other]
                                   (let [diff (m/v- (:position vehicle) (:position other))
                                         distance (m/mag diff)]
                                     (m/set-mag diff (/ 1.0 distance)))) near))]
            (if (seq near)
              (lesson/apply-force vehicle
                (lesson/steer vehicle (m/set-mag sum (:max-speed vehicle))))
              vehicle)))]
  (separate lesson/vehicle lesson/vehicles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 64))

^{:nextjournal.clerk/no-cache true}
(do
(let [wind [0.001 0]
      gravity [0 0.1]]
  (-> lesson/vehicle
      (lesson/apply-force wind)
      (lesson/apply-force gravity)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 65))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(apply-behaviors [vehicle vehicles target]
          (-> vehicle
              (lesson/apply-force (lesson/separate-force vehicle vehicles))
              (lesson/seek target)))]
  (apply-behaviors lesson/vehicle lesson/vehicles (:mouse lesson/env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 66))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 在这里施力, 因为 seek() 和 separate() 不再直接施力.
(letfn [(apply-behaviors [vehicle vehicles target]
          (let [separate (lesson/separate-force vehicle vehicles)
                seek (lesson/seek-force vehicle target)]
            (-> vehicle
                (lesson/apply-force separate)
                (lesson/apply-force seek))))]
  (apply-behaviors lesson/vehicle lesson/vehicles (:mouse lesson/env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 67))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：seek-force只返回转向力，不同时施力，避免行为组合时重复施加同一力。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 不直接施力, 而是返回向量.
(letfn [(seek-force [vehicle target]
          (let [desired (m/set-mag (m/v- target (:position vehicle))
                                  (:max-speed vehicle))
                steer (m/limit (m/v- desired (:velocity vehicle))
                               (:max-force vehicle))]
            steer))]
  (seek-force lesson/vehicle (:mouse lesson/env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 68))

;; ## Clojure 实验 5.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.9")

(notebook/sketch "5.9")

(select-keys (:state (engine/run "5.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 69))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这些权重可以按需要设定.
  ;; 它们可以按每个载具定制,
  ;; 也可以随时间变化.
(let [vehicle lesson/vehicle
      separate (m/v* (lesson/separate-force vehicle lesson/vehicles) 1.5)
      seek (m/v* (lesson/seek-force vehicle (:mouse lesson/env)) 0.5)]
  (-> vehicle
      (lesson/apply-force separate)
      (lesson/apply-force seek)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 70))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 三条鸟群规则
  ;; 这些力的任意权重, 可以尝试不同值
  ;; 施加全部力.
(let [vehicle lesson/vehicle
      near (lesson/nearby vehicle lesson/vehicles 50)
      separation (lesson/separate-force vehicle lesson/vehicles)
      alignment (if (seq near)
                  (lesson/steer vehicle
                    (m/set-mag (lesson/average-vector (map :velocity near))
                               (:max-speed vehicle))) [0 0])
      cohesion (if (seq near)
                 (lesson/seek-force vehicle
                   (lesson/average-vector (map :position near))) [0 0])]
  (-> vehicle
      (lesson/apply-force (m/v* separation 1.5))
      (lesson/apply-force (m/v* alignment 1.0))
      (lesson/apply-force (m/v* cohesion 1.0))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 71))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 累加全部速度并除以总数,
  ;; 得到平均速度.
  ;; 载具希望以最高速度朝该方向运动.
(let [vehicle lesson/vehicle
      boids lesson/vehicles
      sum (reduce m/v+ [0 0] (map :velocity boids))
      average (if (seq boids) (m/vdiv sum (count boids)) [0 0])
      desired (m/set-mag average (:max-speed vehicle))]
  (lesson/steer vehicle desired))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 72))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这是可因 boid 而异的任意值.
  ;; 为了求平均, 记录
  ;; 范围内 boid 数量.
  ;; 若没有近邻, 转向力为零.
(letfn [(align [vehicle boids]
          (let [near (lesson/nearby vehicle boids 50)]
            (if (seq near)
              (let [average (lesson/average-vector (map :velocity near))]
                (lesson/steer vehicle (m/set-mag average (:max-speed vehicle))))
              [0 0])))]
  (align lesson/vehicle lesson/vehicles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 73))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 累加其他个体的位置.
  ;; 使用 seek() 函数
  ;; 来自示例 5.10. 目标
  ;; 是邻居的
  ;; 平均位置.
(letfn [(cohesion [vehicle boids]
          (let [near (lesson/nearby vehicle boids 50)]
            (if (seq near)
              (let [center (lesson/average-vector (map :position near))]
                (lesson/seek-force vehicle center))
              [0 0])))]
  (cohesion lesson/vehicle lesson/vehicles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 74))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每个 Boid 必须知道
  ;; 所有其他 boid.
(letfn [(make-flock [] {:boids []})
        (add-boid [flock boid] (update flock :boids conj boid))
        (run [flock] (update flock :boids lesson/flock-step))]
  (-> (make-flock)
      (add-boid lesson/vehicle)
      (add-boid (second lesson/vehicles))
      run))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 75))

;; ## Clojure 实验 5.10

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.10")

(notebook/sketch "5.10")

(select-keys (:state (engine/run "5.10" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 76))

^{:nextjournal.clerk/no-cache true}
(do
  ;; Flock 对象管理
  ;; 整个群体.
  ;; 鸟群初始包含 120 个 boid.
(binding [m/*rng* (m/seeded 42)]
  (let [boids (mapv (fn [id]
                      (assoc (lesson/make-vehicle id 320 120)
                             :velocity (m/random-vector)))
                    (range 120))
        flock {:boids boids}
        next-flock (update flock :boids lesson/flock-step)]
    (notebook/scene (lesson/scene (:boids next-flock)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 77))

^{:nextjournal.clerk/no-cache true}
(do
(let [boids []]
  boids)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 78))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每个单元为 40x40 像素.
  ;; 根据宽高计算网格列数和行数.
  ;; 创建二维数组.
(let [resolution 40
      cols (long (m/floor (/ (:width lesson/env) resolution)))
      rows (long (m/floor (/ (:height lesson/env) resolution)))]
  {:resolution resolution :cols cols :rows rows
   :grid (vec (repeat cols (vec (repeat rows nil))))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 79))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每个网格单元一个数组
(let [cols 4 rows 3
      grid (mapv (fn [_] (mapv (fn [_] []) (range rows))) (range cols))]
  grid)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 80))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每帧把各网格单元重置为空数组.
  ;; 把每个 boid 放入对应网格单元.
  ;; 求正确的列和行.
  ;; 限制在数组边界内.
  ;; 添加 boid.
(let [resolution 40
      cols (long (m/floor (/ 640 resolution)))
      rows (long (m/floor (/ 360 resolution)))
      empty-grid (vec (repeat cols (vec (repeat rows []))))
      grid (reduce (fn [grid boid]
                     (let [[x y] (:position boid)
                           column (long (m/clamp (m/floor (/ x resolution)) 0 (dec cols)))
                           row (long (m/clamp (m/floor (/ y resolution)) 0 (dec rows)))]
                       (update-in grid [column row] conj boid)))
                   empty-grid lesson/vehicles)]
  {:occupied-cells (count (filter seq (mapcat identity grid)))
   :cell-4-4 (get-in grid [4 4])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 81))

;; ## Clojure 实验 5.11

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.11")

(notebook/sketch "5.11")

(select-keys (:state (engine/run "5.11" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 82))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：查询本格及八个相邻格，避免格子边界两侧的近邻被漏掉；完整四叉树示例5.13复用ch/quadtree与ch/query-tree。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 只检查这些 boid. 邻接单元的处理参见完整代码.
(let [vehicle lesson/vehicle resolution 40
      grid (ch/grid-index lesson/vehicles resolution)
      neighbors (ch/grid-neighbors grid vehicle resolution 640 360)
      force (lesson/flock-force vehicle neighbors)
      next-vehicle (-> vehicle (lesson/apply-force force) lesson/advance)]
  (notebook/scene
    (d/scene (d/background)
             (lesson/triangle-scene
               (update next-vehicle :position m/wrap 640 360))
             (d/label (str "候选邻居: " (count neighbors))))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 83))

;; ## Clojure 实验 5.12

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.12")

(notebook/sketch "5.12")

(select-keys (:state (engine/run "5.12" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 84))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(magnitude [[x y]]
          (m/sqrt (+ (* x x) (* y y))))]
  (magnitude [6 8]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 85))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 执行操作
(let [v [6 9]]
  (when (> (m/mag v) 10)
    {:outside? true :magnitude (m/mag v)}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 86))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 执行操作
(letfn [(magnitude-squared [[x y]]
          (+ (* x x) (* y y)))]
  (let [v [6 9]]
    (when (> (magnitude-squared v) 100)
      {:outside? true :magnitude-squared (magnitude-squared v)})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 87))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：返回有限摘要，避免在notebook输出10000行；计算次数保持原例。")

^{:nextjournal.clerk/no-cache true}
(do
(let [values (mapv (fn [_] (m/sin m/pi)) (range 10000))]
  {:calls (count values) :sample (take 3 values)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 88))

^{:nextjournal.clerk/no-cache true}
(do
(let [radians (fn [degrees] (* degrees (/ m/pi 180)))
      sin-values (mapv #(m/sin (radians %)) (range 360))
      cos-values (mapv #(m/cos (radians %)) (range 360))]
  {:sin-values sin-values :cos-values cos-values})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 89))

^{:nextjournal.clerk/no-cache true}
(do
(let [sin-values (mapv #(m/sin (* % (/ m/pi 180))) (range 360))
      angle 180
      values (mapv (fn [_] (nth sin-values angle)) (range 10000))]
  {:lookups (count values) :sample (take 3 values)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 90))

;; ## Clojure 实验 5.13

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.13")

(notebook/sketch "5.13")

(select-keys (:state (engine/run "5.13" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 91))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：故意把目标构造放在循环内，用于对比下一块的共享目标。")

^{:nextjournal.clerk/no-cache true}
(do
(let [vehicles lesson/vehicles
      updated (mapv (fn [vehicle]
                      (let [mouse (vec (:mouse lesson/env))]
                        (lesson/seek vehicle mouse))) vehicles)]
  {:target-constructions (count vehicles) :vehicles updated})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 92))

^{:nextjournal.clerk/no-cache true}
(do
(let [mouse (:mouse lesson/env)
      updated (mapv #(lesson/seek % mouse) lesson/vehicles)]
  {:target-constructions 1 :vehicles updated})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 93))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：atom保存跨帧目标。Clojure不可变向量仍会分配新值；它共享一次更新，不声称消除向量分配。")

^{:nextjournal.clerk/no-cache true}
(do
(let [mouse (atom [0 0])
      update-frame (fn [target vehicles]
                     (reset! mouse target)
                     (mapv #(lesson/seek % @mouse) vehicles))]
  (update-frame (:mouse lesson/env) lesson/vehicles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 94))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 创建新向量保存转向力.
(let [vehicle lesson/vehicle
      desired (m/set-mag (m/v- (:mouse lesson/env) (:position vehicle))
                         (:max-speed vehicle))
      steer (m/limit (m/v- desired (:velocity vehicle)) (:max-force vehicle))]
  steer)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 95))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：用线程宏消除临时变量；不可变向量保持值语义，该写法不原地修改输入。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 直接在 desired 向量中计算转向力.
(let [vehicle lesson/vehicle]
  (-> (m/v- (:mouse lesson/env) (:position vehicle))
      (m/set-mag (:max-speed vehicle))
      (m/v- (:velocity vehicle))
      (m/limit (:max-force vehicle))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch05" 96))

;; ## Clojure 实验 5.14

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "5.14")

(notebook/sketch "5.14")

(select-keys (:state (engine/run "5.14" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "5.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 5.3 · examples/05_steering/exercise_5_2_pursuit

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/05_steering/exercise_5_2_pursuit"))

(notebook/sketch (notebook/companion-id "examples/05_steering/exercise_5_2_pursuit"))

(:_frame (:state (engine/run (notebook/companion-id "examples/05_steering/exercise_5_2_pursuit") 12)))

;; ### Exercise 5.4 · examples/05_steering/exercise_5_4_wander

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/05_steering/exercise_5_4_wander"))

(notebook/sketch (notebook/companion-id "examples/05_steering/exercise_5_4_wander"))

(:_frame (:state (engine/run (notebook/companion-id "examples/05_steering/exercise_5_4_wander") 12)))

;; ### Exercise 5.9 · examples/05_steering/exercise_5_9_angle_between

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/05_steering/exercise_5_9_angle_between"))

(notebook/sketch (notebook/companion-id "examples/05_steering/exercise_5_9_angle_between"))

(:_frame (:state (engine/run (notebook/companion-id "examples/05_steering/exercise_5_9_angle_between") 12)))

;; ### Exercise 5.13 · examples/05_steering/exercise_5_13_crowd_path_following

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/05_steering/exercise_5_13_crowd_path_following"))

(notebook/sketch (notebook/companion-id "examples/05_steering/exercise_5_13_crowd_path_following"))

(:_frame (:state (engine/run (notebook/companion-id "examples/05_steering/exercise_5_13_crowd_path_following") 12)))

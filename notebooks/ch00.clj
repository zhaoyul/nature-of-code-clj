;; # 第 0 章: 随机性

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch00
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch00 :as ch]
            [noc.lessons.ch00 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 0))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Clojure 用映射保存对象字段，用函数完成构造。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 对象拥有构造函数, 并在这里完成初始化.
(letfn [(walker [{:keys [width height]}]
          {:x (/ width 2) :y (/ height 2)})]
  (walker lesson/env))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 1))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：返回的绘图命令由本工程的 Quil renderer 执行。")

^{:nextjournal.clerk/no-cache true}
(do
(let [{:keys [x y]} (lesson/walker)]
  (notebook/scene [[:stroke 0] [:point x y]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 2))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (long (m/floor (m/random 4))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 3))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 0、1、2 或 3.
(letfn [(step [{:keys [x y]}]
          (let [choice (m/random-int 4)]
            (case choice
              0 {:x (inc x) :y y}
              1 {:x (dec x) :y y}
              2 {:x x :y (inc y)}
              3 {:x x :y (dec y)})))]
  (binding [m/*rng* (m/seeded 42)]
    (vec (take 8 (iterate step (lesson/walker))))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [walker nil]
  {:walker walker :initialized? (some? walker)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [canvas {:width 640 :height 240}
      walker (lesson/walker canvas)]
  {:canvas canvas :walker walker :scene (notebook/scene [[:background 255]])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 6))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 7))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：用有限的 60 帧替代无限 draw 循环；下面的交互草图持续执行相同的随机游走算法。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(make-walker [{:keys [width height]}]
          {:x (/ width 2) :y (/ height 2)})
        (step [walker]
          (let [[dx dy] (m/choose [[1 0] [-1 0] [0 1] [0 -1]])]
            (-> walker (update :x + dx) (update :y + dy))))
        (show [{:keys [x y]}] [[:stroke 0] [:point x y]])]
  (binding [m/*rng* (m/seeded 42)]
    (let [canvas {:width 640 :height 240}
          trail (vec (take 60 (iterate step (make-walker canvas))))]
      {:canvas canvas :walker (peek trail)
       :scene (notebook/scene (d/scene [[:background 255]] (mapcat show trail)))})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 8))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [walker (lesson/walker)
        xstep (dec (m/random-int 3))
        ystep (dec (m/random-int 3))]
    (-> walker (update :x + xstep) (update :y + ystep))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 9))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [walker (lesson/walker)
        xstep (m/random -1 1) ystep (m/random -1 1)]
    (-> walker (update :x + xstep) (update :y + ystep))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 10))

;; ## Clojure 实验 0.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "0.1")

(notebook/sketch "0.1")

(select-keys (:state (engine/run "0.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 11))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：240 次抽样可直接检查均匀分布，并生成原例的直方图绘图命令。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 记录每个随机数被选中的次数.
  ;; 计数槽总数.
(binding [m/*rng* (m/seeded 42)]
  (let [total 20
        counts (reduce (fn [counts _]
                         (update counts (m/random-int (count counts)) inc))
                       (vec (repeat total 0)) (range 240))]
    {:samples (reduce + counts) :counts counts
     :scene (notebook/scene (lesson/histogram counts lesson/env))}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 12))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [stuff [1 1 2 3 3]]
    (m/choose stuff)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 13))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 10% 的概率.
  ;; 0 到 1 的随机浮点数.
(binding [m/*rng* (m/seeded 42)]
  (let [probability 0.1 r (m/random)]
    {:random r :action (when (< r probability) "Sing!")}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 14))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [num (m/random)]
    (cond (< num 0.6) "Sing!"
          (< num 0.7) "Dance!"
          :else "Sleep!")))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 15))

;; ## Clojure 实验 0.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "0.2")

(notebook/sketch "0.2")

(select-keys (:state (engine/run "0.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 16))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 40% 的概率向右.
(letfn [(biased-step [walker]
          (let [r (m/random)
                [dx dy] (cond (< r 0.4) [1 0]
                              (< r 0.6) [-1 0]
                              (< r 0.8) [0 1]
                              :else [0 -1])]
            (-> walker (update :x + dx) (update :y + dy))))]
  (binding [m/*rng* (m/seeded 42)]
    (vec (take 12 (iterate biased-step (lesson/walker))))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 17))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [h (m/random 200 300)] h))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 18))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [draw (fn [] (m/gaussian))]
    (mapv (fn [_] (draw)) (range 8))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 19))

^{:nextjournal.clerk/no-cache true}
(do
(let [scores [85 82 88 86 85 93 98 40 73 83]]
  {:scores scores :mean (/ (reduce + scores) (double (count scores)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 20))

;; ## Clojure 实验 0.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "0.3")

(notebook/sketch "0.3")

(select-keys (:state (engine/run "0.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 21))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [x (m/gaussian 320 60)]
    (notebook/scene [[:no-stroke] [:fill 0 10] [:circle x 120 16]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 22))

^{:nextjournal.clerk/no-cache true}
(do
(let [rng (m/seeded 42)
      direct (binding [m/*rng* rng] (m/gaussian 320 60))
      affine (binding [m/*rng* (m/seeded 42)]
               (+ (* 60 (m/gaussian)) 320))]
  {:direct direct :affine affine :same? (= direct affine)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 23))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [r (m/random)
        jump? (< r 0.01)
        bound (if jump? 100 1)
        xstep (m/random (- bound) bound)
        ystep (m/random (- bound) bound)]
    {:rare-jump? jump? :step [xstep ystep]}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 24))

;; ## Clojure 实验 0.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "0.4")

(notebook/sketch "0.4")

(select-keys (:state (engine/run "0.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 25))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 候选随机值.
  ;; 资格随机值.
(letfn [(accept-reject []
          (loop []
            (let [candidate (m/random) probability candidate
                  threshold (m/random)]
              (if (< threshold probability) candidate (recur)))))]
  (binding [m/*rng* (m/seeded 42)]
    (mapv (fn [_] (accept-reject)) (range 12))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 26))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [step-size 10 stepx (m/random (- step-size) step-size)
        stepy (m/random (- step-size) step-size)]
    (-> (lesson/walker) (update :x + stepx) (update :y + stepy))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 27))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [x (m/random 0 (:width lesson/env))]
    (notebook/scene [[:circle x 180 16]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 28))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原片段故意展示错误写法：noise 的第二参数是采样坐标，不能用它指定输出范围。此处计算两者，实际绘制正确的缩放结果。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 诱人的写法, 但不正确.
(let [width (:width lesson/env)
      incorrect-x (m/noise 0 width)
      correct-x (m/map-range (m/noise 0) 0 1 0 width)]
  {:two-noise-coordinates incorrect-x :mapped-x correct-x
   :scene (notebook/scene [[:circle correct-x 180 16]])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 29))

^{:nextjournal.clerk/no-cache true}
(do
(let [n (m/noise 3)] n)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 30))

^{:nextjournal.clerk/no-cache true}
(do
(let [t 3 draw (fn [] (m/noise t))]
  (mapv (fn [_] (draw)) (range 4)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 31))

^{:nextjournal.clerk/no-cache true}
(do
(let [samples (mapv (fn [frame]
                      (let [t (* frame 0.01)] {:t t :n (m/noise t)}))
                    (range 8))]
  samples)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 32))

^{:nextjournal.clerk/no-cache true}
(do
(let [width (:width lesson/env)
      samples (mapv (fn [frame]
                      (let [t (* frame 0.01) n (m/noise t)
                            x (m/map-range n 0 1 0 width)]
                        {:t t :n n :x x}))
                    (range 12))]
  {:samples samples
   :scene (notebook/scene (mapv (fn [{:keys [x]}] [:ellipse x 180 16 16]) samples))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 33))

;; ## Clojure 实验 0.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "0.5")

(notebook/sketch "0.5")

(select-keys (:state (engine/run "0.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 34))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(step [{:keys [tx ty] :as walker}]
          (assoc walker
                 :x (m/map-range (m/noise tx) 0 1 0 (:width lesson/env))
                 :y (m/map-range (m/noise ty) 0 1 0 (:height lesson/env))
                 :tx (+ tx 0.01) :ty (+ ty 0.01)))]
  (vec (take 8 (iterate step {:tx 0 :ty 10000 :x 0 :y 0}))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 35))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：以 8×6 像素样本演示同样的 RGBA 索引算法，避免 notebook 输出整个画布的巨大数组。")

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (let [width 8 height 6
        pixels (vec (repeat (* width height 4) 0))
        pixels (reduce (fn [pixels [x y]]
                         (let [index (* (+ x (* y width)) 4)
                               bright (m/random 255)]
                           (reduce (fn [p [offset value]]
                                     (assoc p (+ index offset) value))
                                   pixels [[0 bright] [1 bright] [2 bright] [3 255]])))
                       pixels (for [x (range width) y (range height)] [x y]))]
    {:width width :height height :rgba pixels}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 36))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 0.12 y 0.18
      bright (m/map-range (m/noise x y) 0 1 0 255)]
  bright)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 37))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：像素坐标乘以 0.01 得到连续噪声采样坐标，保留原例两层循环及 RGBA 索引语义。")

^{:nextjournal.clerk/no-cache true}
(do
(let [width 8 height 6
      pixels (reduce
              (fn [pixels [x y]]
                (let [xoff (* x 0.01) yoff (* y 0.01)
                      bright (m/map-range (m/noise xoff yoff) 0 1 0 255)
                      index (* (+ x (* y width)) 4)]
                  (reduce (fn [p [offset value]] (assoc p (+ index offset) value))
                          pixels [[0 bright] [1 bright] [2 bright] [3 255]])))
              (vec (repeat (* width height 4) 0))
              (for [x (range width) y (range height)] [x y]))]
  {:width width :height height :rgba pixels})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch00" 38))

;; ## Clojure 实验 0.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "0.6")

(notebook/sketch "0.6")

(select-keys (:state (engine/run "0.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "0.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 0.1 · examples/00_randomness/exercise_0_1_solution_skewed_random_walker

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_1_solution_skewed_random_walker"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_1_solution_skewed_random_walker"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_1_solution_skewed_random_walker") 12)))

;; ### Exercise 0.3 · examples/00_randomness/exercise_0_3_solution_random_walk_towards_mouse

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_3_solution_random_walk_towards_mouse"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_3_solution_random_walk_towards_mouse"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_3_solution_random_walk_towards_mouse") 12)))

;; ### Exercise 0.3 · examples/00_randomness/figure_0_2_bell_curve_low

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_0_2_bell_curve_low"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_0_2_bell_curve_low"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_0_2_bell_curve_low") 12)))

;; ### Exercise 0.3 · examples/00_randomness/figure_0_2_bell_curve_high

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_0_2_bell_curve_high"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_0_2_bell_curve_high"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_0_2_bell_curve_high") 12)))

;; ### Exercise 0.4 · examples/00_randomness/exercise_0_4_solution_paint_splatter

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_4_solution_paint_splatter"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_4_solution_paint_splatter"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_4_solution_paint_splatter") 12)))

;; ### Exercise 0.5 · examples/00_randomness/exercise_0_5_solution_gaussian_random_walker

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_5_solution_gaussian_random_walker"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_5_solution_gaussian_random_walker"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_5_solution_gaussian_random_walker") 12)))

;; ### Exercise 0.6 · examples/00_randomness/exercise_0_6_solution_quadratic_random_walker

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_6_solution_quadratic_random_walker"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_6_solution_quadratic_random_walker"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_6_solution_quadratic_random_walker") 12)))

;; ### Exercise 0.6 · examples/00_randomness/figure_i_4_noise

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_i_4_noise"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_i_4_noise"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_i_4_noise") 12)))

;; ### Exercise 0.6 · examples/00_randomness/figure_i_5_random

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_i_5_random"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_i_5_random"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_i_5_random") 12)))

;; ### Exercise 0.7 · examples/00_randomness/exercise_0_7_solution_perlin_noise_walker

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_7_solution_perlin_noise_walker"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_7_solution_perlin_noise_walker"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_7_solution_perlin_noise_walker") 12)))

;; ### Two-Dimensional Noise · examples/00_randomness/figure_i_noise_2_d

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_i_noise_2_d"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_i_noise_2_d"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_i_noise_2_d") 12)))

;; ### Exercise 0.8 · examples/00_randomness/exercise_0_8_solution_2_d_noise_parameterized

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_8_solution_2_d_noise_parameterized"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_8_solution_2_d_noise_parameterized"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_8_solution_2_d_noise_parameterized") 12)))

;; ### Exercise 0.9 · examples/00_randomness/exercise_0_9_solution_2_d_noise_animated

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_0_9_solution_2_d_noise_animated"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_0_9_solution_2_d_noise_animated"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_0_9_solution_2_d_noise_animated") 12)))

;; ### Exercise 0.10 · examples/00_randomness/exercise_i_10_noise_terrain

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/exercise_i_10_noise_terrain"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/exercise_i_10_noise_terrain"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/exercise_i_10_noise_terrain") 12)))

;; ### Exercise 0.10 · examples/00_randomness/figure_i_11_tree_stochastic_noise

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_i_11_tree_stochastic_noise"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_i_11_tree_stochastic_noise"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_i_11_tree_stochastic_noise") 12)))

;; ### Exercise 0.10 · examples/00_randomness/figure_i_12_flow_field_with_perlin_noise

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/00_randomness/figure_i_12_flow_field_with_perlin_noise"))

(notebook/sketch (notebook/companion-id "examples/00_randomness/figure_i_12_flow_field_with_perlin_noise"))

(:_frame (:state (engine/run (notebook/companion-id "examples/00_randomness/figure_i_12_flow_field_with_perlin_noise") 12)))

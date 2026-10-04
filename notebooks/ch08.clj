;; # 第 8 章: 分形

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch08
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch08 :as ch]
            [noc.lessons.ch08 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 0))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(some-function [] [[:background 0]])]
  (notebook/scene (some-function)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 1))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原例的无终止条件递归会一直调用自身；本执行单元显式添加 remaining，记录十层调用后终止。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(some-function [remaining]
          (if (zero? remaining)
            [:finished]
            (into [remaining] (some-function (dec remaining)))))]
  (some-function 10))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 2))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(factorial [n]
          (reduce *' 1 (range 1 (inc n))))]
  (mapv factorial (range 0 7)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 3))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(factorial [n]
          (if (<= n 1) 1 (*' n (factorial (dec n)))))]
  (mapv factorial (range 0 7)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 4))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 5))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(draw-circles [x y r]
          (cons [:circle x y (* r 2)]
                (when (> r 4) (draw-circles x y (* r 0.75)))))]
  (notebook/scene (d/scene [[:stroke 0] [:no-fill]] (draw-circles 320 120 100))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 6))

;; ## Clojure 实验 8.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.1")

(notebook/sketch "8.1")

(select-keys (:state (engine/run "8.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 7))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(draw-circles [x y radius]
          (cons [:circle x y (* radius 2)]
                (when (> radius 4)
                  (let [r (/ radius 2)]
                    (concat (draw-circles (+ x r) y r)
                            (draw-circles (- x r) y r))))))]
  (notebook/scene (d/scene (d/background) [[:stroke 0] [:no-fill]]
                           (draw-circles 320 120 160))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 8))

;; ## Clojure 实验 8.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.2")

(notebook/sketch "8.2")

(select-keys (:state (engine/run "8.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 9))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(draw-circles [x y radius]
          (cons [:circle x y (* radius 2)]
                (when (> radius 16)
                  (let [r (/ radius 2)]
                    (mapcat (fn [[a b]] (draw-circles a b r))
                            [[(+ x r) y] [(- x r) y] [x (+ y r)] [x (- y r)]])))))]
  (notebook/scene (d/scene (d/background) [[:stroke 0] [:no-fill]]
                           (draw-circles 320 120 100))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 10))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(cantor [x y length] [[:line x y (+ x length) y]])]
  (notebook/scene (cantor 10 20 620)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 11))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene [[:line 10 20 630 20]])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 12))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(cantor [x y length]
          [[:line x y (+ x length) y]
           [:line x (+ y 20) (+ x (/ length 3)) (+ y 20)]
           [:line (+ x (/ (* 2 length) 3)) (+ y 20) (+ x length) (+ y 20)]])]
  (notebook/scene (cantor 10 20 620)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 13))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原片段缺少停止条件；显式限定深度，保留左右各三分之一的递归规则。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(cantor [x y length depth]
          (when (pos? depth)
            (concat [[:line x y (+ x length) y]]
                    (cantor x (+ y 20) (/ length 3) (dec depth))
                    (cantor (+ x (/ (* 2 length) 3)) (+ y 20) (/ length 3) (dec depth)))))]
  (notebook/scene (vec (cantor 10 20 620 6))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 14))

;; ## Clojure 实验 8.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.3")

(notebook/sketch "8.3")

(select-keys (:state (engine/run "8.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 15))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(cantor [x y length]
          (when (> length 1)
            (concat [[:line x y (+ x length) y]]
                    (cantor x (+ y 20) (/ length 3))
                    (cantor (+ x (/ (* 2 length) 3)) (+ y 20) (/ length 3)))))]
  (notebook/scene (vec (cantor 10 20 620))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 16))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(koch-line [a b] {:start a :end b})
        (show [{:keys [start end]}] (d/line start end))]
  (notebook/scene (show (koch-line [10 200] [630 200]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 17))

^{:nextjournal.clerk/no-cache true}
(do
(let [segments [[[0 200] [640 200]]]]
  (notebook/scene
   (d/scene (d/background) (mapcat (fn [[a b]] (d/line a b)) segments))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [segments [[[0 200] [640 200]]]
      next-segments (vec (mapcat (fn [segment]
                                  (let [[a b c d e] (ch/koch-points segment)]
                                    [[a b] [b c] [c d] [d e]])) segments))]
  (notebook/scene (ch/line-scene next-segments "一条线段被四条新线段替换")))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 19))

^{:nextjournal.clerk/no-cache true}
(do
(let [a [0 200] e [640 200]
      v (m/vdiv (m/v- e a) 3)
      b (m/v+ a v) d (m/v+ b v)
      c (m/v+ b (m/rotate v (- (/ m/pi 3))))]
  [a b c d e])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 20))

;; ## Clojure 实验 8.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.4")

(notebook/sketch "8.4")

(select-keys (:state (engine/run "8.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [segments (nth (iterate ch/koch-generation [[[0 200] [640 200]]]) 5)]
  (notebook/scene (ch/line-scene segments (str "线段数 " (count segments)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 22))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：加入短树干使坐标原点的平移可见。")

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene [[:push] [:translate 320 240]
                 [:line 0 0 0 -100] [:pop]])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 23))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene [[:push] [:translate 320 240] [:line 0 0 0 -100] [:pop]])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 24))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene [[:push] [:translate 320 140] [:rotate (/ m/pi 6)]
                 [:line 0 0 0 -100] [:pop]])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 25))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene
 [[:push] [:translate 320 240] [:line 0 0 0 -100] [:translate 0 -100]
  [:push] [:rotate (/ m/pi 6)] [:line 0 0 0 -100] [:pop]
  [:push] [:rotate (- (/ m/pi 6))] [:line 0 0 0 -100] [:pop] [:pop]])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 26))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：原片段每次分支仍为固定长度且没有终止条件；示范单元限定三层，防止无限递归。")

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(branch [depth]
          (concat [[:line 0 0 0 -40] [:translate 0 -40]]
                  (when (pos? depth)
                    (concat [[:push] [:rotate (/ m/pi 6)]] (branch (dec depth)) [[:pop]]
                            [[:push] [:rotate (- (/ m/pi 6))]] (branch (dec depth)) [[:pop]]))))]
  (notebook/scene (d/scene [[:push] [:translate 320 240]] (branch 3) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 27))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(branch [length angle]
          (let [next-length (* length 0.67)]
            (concat [[:line 0 0 0 (- length)] [:translate 0 (- length)]]
                    (when (> next-length 2)
                      (concat [[:push] [:rotate angle]] (branch next-length angle) [[:pop]]
                              [[:push] [:rotate (- angle)]] (branch next-length angle) [[:pop]])))))]
  (notebook/scene (d/scene [[:push] [:translate 320 240]]
                           (branch 80 (/ m/pi 6)) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 28))

;; ## Clojure 实验 8.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.5")

(notebook/sketch "8.5")

(select-keys (:state (engine/run "8.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 29))

^{:nextjournal.clerk/no-cache true}
(do
(let [mouse-x 320 angle (m/map-range mouse-x 0 640 0 (/ m/pi 2))]
  (notebook/scene
   (d/scene (d/background) [[:push] [:translate 320 240] [:stroke 0] [:stroke-weight 2]]
            (lesson/branch-length 80 angle) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 30))

^{:nextjournal.clerk/no-cache true}
(do
(m/random 0 (/ m/pi 3))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 31))

;; ## Clojure 实验 8.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.6")

(notebook/sketch "8.6")

(select-keys (:state (engine/run "8.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 32))

^{:nextjournal.clerk/no-cache true}
(do
(let [lines (ch/tree-lines [320 240] (- (/ m/pi 2)) 80 0.67 0 7 true)]
  (notebook/scene (ch/line-scene lines "每个节点随机产生一至三个分支")))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 33))

^{:nextjournal.clerk/no-cache true}
(do
(let [message1 "Hello!" message2 (str message1 " Goodbye!")]
  {:message message2 :characters (mapv str message2)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 34))

^{:nextjournal.clerk/no-cache true}
(do
(mapv (fn [generation sentence] {:generation generation :sentence sentence})
      (range 5)
      (take 5 (iterate #(ch/rewrite-sentence % {"A" "AB" "B" "A"}) "A")))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 35))

^{:nextjournal.clerk/no-cache true}
(do
(let [current "A"
      next-generation (apply str (map (fn [c]
                                        (case c \A "AB" \B "A" (str c))) current))]
  next-generation)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 36))

;; ## Clojure 实验 8.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.7")

(notebook/sketch "8.7")

(select-keys (:state (engine/run "8.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 37))

^{:nextjournal.clerk/no-cache true}
(do
(let [rules {"A" "AB" "B" "A"}
      sentences (take 10 (iterate #(ch/rewrite-sentence % rules) "A"))]
  (notebook/scene
   (d/scene (d/background) [[:fill 0] [:no-stroke] [:text-size 16]]
            (map-indexed (fn [generation sentence]
                           [:text (str generation ": " sentence) 4 (+ 20 (* generation 16))])
                         (rest sentences)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 38))

^{:nextjournal.clerk/no-cache true}
(do
(let [length 20 angle (/ m/pi 6)]
  {:F [[:line 0 0 0 length] [:translate 0 length]]
   :G [[:translate 0 length]] :+ [[:rotate angle]] :- [[:rotate (- angle)]]
   :open [[:push]] :close [[:pop]]})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 39))

^{:nextjournal.clerk/no-cache true}
(do
(let [sentence "F[+F][-F]G" length 40 angle (/ m/pi 6)
      commands (mapcat (fn [c]
                         (case c
                           \F [[:line 0 0 length 0] [:translate length 0]]
                           \G [[:translate length 0]]
                           \+ [[:rotate angle]] \- [[:rotate (- angle)]]
                           \[ [[:push]] \] [[:pop]] [])) sentence)]
  (notebook/scene (d/scene [[:push] [:translate 280 120]] commands [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 40))

;; ## Clojure 实验 8.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.8")

(notebook/sketch "8.8")

(select-keys (:state (engine/run "8.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 41))

^{:nextjournal.clerk/no-cache true}
(do
(let [rules {"F" "FF+[+F-F-F]-[-F+F+F]"}
      sentence (nth (iterate #(ch/rewrite-sentence % rules) "F") 4)
      lines (ch/turtle-lines sentence 4 (* 25 (/ m/pi 180)))]
  (notebook/scene (ch/line-scene lines "L 系统植物")))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 42))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(l-system [axiom rules] {:sentence axiom :rules rules})
        (generate [{:keys [sentence rules] :as system}]
          (assoc system :sentence (apply str (map #(get rules (str %) (str %)) sentence))))]
  (nth (iterate generate (l-system "A" {"A" "AB" "B" "A"})) 4))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 43))

^{:nextjournal.clerk/no-cache true}
(do
(let [initial {:length 8 :angle (* 25 (/ m/pi 180))
               :position [320 240] :direction (- (/ m/pi 2)) :stack [] :lines []}
      sentence (nth (iterate #(ch/rewrite-sentence % ch/plant-rules) "F") 3)
      turtle (reduce lesson/turtle-step initial sentence)]
  (notebook/scene (ch/line-scene (:lines turtle) "海龟把位置和方向压栈/出栈")))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch08" 44))

;; ## Clojure 实验 8.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "8.9")

(notebook/sketch "8.9")

(select-keys (:state (engine/run "8.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "8.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 8.1 · examples/08_fractals/exercise_8_1_fractal_lines

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_1_fractal_lines"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_1_fractal_lines"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_1_fractal_lines") 12)))

;; ### Exercise 8.2 · examples/08_fractals/exercise_8_2_koch_snowflake

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_2_koch_snowflake"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_2_koch_snowflake"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_2_koch_snowflake") 12)))

;; ### Exercise 8.3 · examples/08_fractals/exercise_8_3_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_3_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_3_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_3_contributed_solution") 12)))

;; ### Exercise 8.4 · examples/08_fractals/exercise_8_4_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_4_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_4_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_4_contributed_solution") 12)))

;; ### Exercise 8.5 · examples/08_fractals/exercise_8_5_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_5_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_5_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_5_contributed_solution") 12)))

;; ### Exercise 8.7 · examples/08_fractals/exercise_8_7_branch_thickness

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_7_branch_thickness"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_7_branch_thickness"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_7_branch_thickness") 12)))

;; ### Exercise 8.8 · examples/08_fractals/exercise_8_8_branch_objects_animation

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_8_branch_objects_animation"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_8_branch_objects_animation"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_8_branch_objects_animation") 12)))

;; ### Exercise 8.9 · examples/08_fractals/exercise_8_9_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_9_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_9_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_9_contributed_solution") 12)))

;; ### Exercise 8.11 · examples/08_fractals/exercise_8_11_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_11_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_11_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_11_contributed_solution") 12)))

;; ### Exercise 8.12 · examples/08_fractals/exercise_8_12_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_12_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_12_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_12_contributed_solution") 12)))

;; ### Exercise 8.13 · examples/08_fractals/exercise_8_13_contributed_solution

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/08_fractals/exercise_8_13_contributed_solution"))

(notebook/sketch (notebook/companion-id "examples/08_fractals/exercise_8_13_contributed_solution"))

(:_frame (:state (engine/run (notebook/companion-id "examples/08_fractals/exercise_8_13_contributed_solution") 12)))

;; # 第 7 章: 元胞自动机

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch07
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch07 :as ch]
            [noc.lessons.ch07 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 0))

^{:nextjournal.clerk/no-cache true}
(do
[1 0 1 0 0 0 0 1 0 1 1 1 0 0 0 1 1 1 0 0]
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 1))

^{:nextjournal.clerk/no-cache true}
(do
(let [cells lesson/cells width 30]
  (notebook/scene
   (d/scene (d/background)
            (mapcat (fn [[i cell]]
                      [[:fill (if (zero? cell) 255 0)] [:stroke 0]
                       [:rect (* i width) 0 width width]])
                    (map-indexed vector cells)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 2))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：边界安全限定在内部元胞；sequential 故意读已更新的左邻，比较结果即可观察原地更新的问题。")

^{:nextjournal.clerk/no-cache true}
(do
(let [cells lesson/cells]
  {:sequential (lesson/sequential-next cells)
   :synchronous (lesson/synchronous-next cells)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 3))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：这是原书的原地更新反例：索引不越界，但读取已改变的邻居仍然改变规则含义。")

^{:nextjournal.clerk/no-cache true}
(do
(reduce (fn [cells i]
          (let [left (nth cells (dec i)) middle (nth cells i) right (nth cells (inc i))]
            (assoc cells i (lesson/rules left middle right))))
        lesson/cells (range 1 (dec (count lesson/cells))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [cells lesson/cells
      next-generation (reduce (fn [next i]
                                (assoc next i (lesson/rules (nth cells (dec i))
                                                           (nth cells i) (nth cells (inc i)))))
                              cells (range 1 (dec (count cells))))]
  next-generation)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [ruleset [0 1 0 1 1 0 1 0]
      rules (fn [a b c]
              (cond (= [a b c] [1 1 1]) (nth ruleset 0)
                    (= [a b c] [1 1 0]) (nth ruleset 1)
                    (= [a b c] [1 0 1]) (nth ruleset 2)
                    (= [a b c] [1 0 0]) (nth ruleset 3)
                    (= [a b c] [0 1 1]) (nth ruleset 4)
                    (= [a b c] [0 1 0]) (nth ruleset 5)
                    (= [a b c] [0 0 1]) (nth ruleset 6)
                    :else (nth ruleset 7)))]
  (mapv #(apply rules %) [[1 1 1] [1 1 0] [1 0 1] [1 0 0] [0 1 1] [0 1 0] [0 0 1] [0 0 0]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 6))

^{:nextjournal.clerk/no-cache true}
(do
(let [ruleset [0 1 0 1 1 0 1 0]
      rules (fn [a b c]
              (let [index (Long/parseLong (str a b c) 2)]
                (nth ruleset (- 7 index))))]
  (mapv #(apply rules %) [[1 1 1] [1 1 0] [1 0 1] [1 0 0] [0 1 1] [0 1 0] [0 0 1] [0 0 0]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 7))

^{:nextjournal.clerk/no-cache true}
(do
(let [cells (assoc (vec (repeat 64 0)) 32 1)
      next-generation (fn [cells] (ch/elementary-next cells 90))]
  (vec (take 10 (iterate next-generation cells))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 8))

^{:nextjournal.clerk/no-cache true}
(do
(let [width 640 cell-width 10]
  (vec (repeat (quot width cell-width) 0)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 9))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 10))

^{:nextjournal.clerk/no-cache true}
(do
(let [width 640 cell-width 10
      cells (assoc (vec (repeat (quot width cell-width) 0)) 32 1)
      history (take 24 (iterate #(ch/elementary-next % 90) cells))]
  (notebook/scene
   (d/scene (d/background) [[:fill 0] [:no-stroke]]
            (for [[generation row] (map-indexed vector history)
                  [i cell] (map-indexed vector row) :when (= cell 1)]
              [:rect (* i cell-width) (* generation cell-width) cell-width cell-width]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 11))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：Clojure 棋盘使用 [y x] 索引，即外层为行、内层为列。")

^{:nextjournal.clerk/no-cache true}
(do
(let [width 640 height 240 w 8 columns (quot width w) rows (quot height w)]
  {:columns columns :rows rows :board (vec (repeat rows (vec (repeat columns 0))))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 12))

^{:nextjournal.clerk/no-cache true}
(do
(let [columns 8 rows 5]
  (mapv (fn [_] (mapv (fn [_] (m/random-int 2)) (range columns))) (range rows)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 13))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(create-2d-array [columns rows]
          (vec (repeat rows (vec (repeat columns 0)))))]
  (create-2d-array 8 5))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [board lesson/board x 2 y 2]
  (- (reduce + (for [dy [-1 0 1] dx [-1 0 1]]
                 (get-in board [(+ y dy) (+ x dx)])))
     (get-in board [y x])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 15))

^{:nextjournal.clerk/no-cache true}
(do
(let [board lesson/board x 2 y 2
      alive (get-in board [y x]) neighbor-sum (lesson/neighbor-sum board x y)]
  (cond (and (= alive 1) (< neighbor-sum 2)) 0
        (and (= alive 1) (> neighbor-sum 3)) 0
        (and (= alive 0) (= neighbor-sum 3)) 1
        :else alive))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 16))

;; ## Clojure 实验 7.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "7.1")

(notebook/sketch "7.1")

(select-keys (:state (engine/run "7.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 17))

^{:nextjournal.clerk/no-cache true}
(do
(let [board lesson/board rows (count board) columns (count (first board))
      next-generation #(ch/life-next % columns rows)
      next-board (next-generation board)]
  (notebook/scene
   (ch/life-scene {:board next-board :previous board :columns columns :rows rows
                   :size 40 :generation 1} false)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [board (vec (repeat 3 (vec (repeat 4 nil))))
      x 2 y 1 w 20 state (m/random-int 2)]
  (assoc-in board [y x] (lesson/cell state (* x w) (* y w) w)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 19))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(cell [state x y w]
          {:state state :previous state :x x :y y :width w})
        (save-previous [c] (assoc c :previous (:state c)))]
  (save-previous (assoc (cell 0 40 20 20) :state 1)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 20))

;; ## Clojure 实验 7.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "7.2")

(notebook/sketch "7.2")

(select-keys (:state (engine/run "7.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [cell {:state 1 :previous 0 :x 40 :y 20 :width 40}
      {:keys [state previous x y width]} cell
      color (cond (and (= previous 0) (= state 1)) [0 0 255]
                  (= state 1) [0 0 0]
                  (and (= previous 1) (= state 0)) [255 0 0]
                  :else [255 255 255])]
  (notebook/scene [[:stroke 0] (into [:fill] color) [:rect x y width width]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 22))

^{:nextjournal.clerk/no-cache true}
(do
(let [old-board lesson/board
      rows (count old-board) columns (count (first old-board))
      board (ch/life-next old-board columns rows)]
  (notebook/scene
   (ch/life-scene {:board board :previous old-board :columns columns :rows rows
                   :size 40 :generation 1} true)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 23))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(hexagon [x y radius]
          (let [vertices (mapv (fn [i]
                                 (let [angle (* i (/ m/tau 6))]
                                   [(+ x (* radius (m/cos angle)))
                                    (+ y (* radius (m/sin angle)))]))
                               (range 6))]
            [[:stroke 0] [:no-fill] [:polygon vertices]]))]
  (notebook/scene (hexagon 320 120 80)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch07" 24))

;; ## Clojure 实验 7.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "7.3")

(notebook/sketch "7.3")

(select-keys (:state (engine/run "7.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "7.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 7.8 · examples/07_ca/exercise_7_8_hexagon_ca

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/07_ca/exercise_7_8_hexagon_ca"))

(notebook/sketch (notebook/companion-id "examples/07_ca/exercise_7_8_hexagon_ca"))

(:_frame (:state (engine/run (notebook/companion-id "examples/07_ca/exercise_7_8_hexagon_ca") 12)))

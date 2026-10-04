;; # 引言

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.introduction
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.lessons.introduction :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 0))

^{:nextjournal.clerk/no-cache true}
(do
(let [x 0] (inc x))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 1))

^{:nextjournal.clerk/no-cache true}
(do
(let [[x y] [10 20] [vx vy] [2 -1]]
  [(+ x vx) (+ y vy)])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 2))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 该画布尺寸是为了适应书籍版式, 对示例本身并不关键.
  ;; 每次进入 draw() 时绘制一个随机圆.
(let [width 640 height 240 spacing 60]
  (notebook/scene
   (vec (concat [[:background 255] [:fill 240 99 164]]
                (for [x (range 0 width spacing)] [:circle x (/ height 2) 40])))
   {:width width :height height}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 3))

^{:nextjournal.clerk/no-cache true}
(do
(let [width 640 height 240 spacing 40]
  (notebook/scene
   (vec (concat [[:background 255] [:fill 240 99 164]]
                (for [x (range 0 width spacing) y (range 0 height spacing)]
                  [:circle x y 24])))
   {:width width :height height}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 4))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 使圆呈现粉色的 RGB 值.
(notebook/scene [[:background 255] [:fill 240 99 164] [:circle 320 120 200]]
                {:height 240})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 5))

^{:nextjournal.clerk/no-cache true}
(do
{:width 640 :height 240 :renderer :quil}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 6))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene [[:background 255]] {:height 240})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 7))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene [[:fill 240 99 164] [:circle 320 120 200]] {:height 240})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 8))

^{:nextjournal.clerk/no-cache true}
(do
(binding [m/*rng* (m/seeded 42)]
  (notebook/scene
   (vec (concat [[:background 255] [:rect-mode :center] [:fill 127]]
                (repeatedly 40 #(vector :rect (m/random 640) (m/random 240) 20 20))))
   {:height 240}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 9))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 在这里填写随机大小
(binding [m/*rng* (m/seeded 42)]
  (notebook/scene
   (vec (concat [[:background 255] [:fill 0 25] [:stroke 0 50]]
                (repeatedly 80 #(vector :circle (m/random 640) (m/random 240) 48))))
   {:height 240}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "introduction" 10))

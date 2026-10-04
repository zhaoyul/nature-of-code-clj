(ns noc.chapters.ch08
  "第 8 章：递归、Koch 曲线与 L 系统。"
  (:require [noc.math :as m] [noc.draw :as d]))

(defn recursive-circles [x y radius branches threshold]
  (cons [:circle x y (* 2 radius)]
        (when (> radius threshold)
          (let [r (if (= branches 1) (* radius 0.75) (/ radius 2))
                centers (case branches 1 [[x y]] 2 [[(+ x r) y] [(- x r) y]]
                              4 [[(+ x r) y] [(- x r) y] [x (+ y r)] [x (- y r)]])]
            (mapcat (fn [[a b]] (recursive-circles a b r branches threshold)) centers)))))

(defn cantor [x y length depth]
  (when (pos? depth)
    (concat [[:line x y (+ x length) y]]
            (cantor x (+ y 34) (/ length 3) (dec depth))
            (cantor (+ x (* 2 (/ length 3))) (+ y 34) (/ length 3) (dec depth)))))

(defn koch-points [[a e]]
  (let [v (m/v* (m/v- e a) (/ 1 3)) b (m/v+ a v) d (m/v+ b v) c (m/v+ b (m/rotate v (- (/ m/pi 3))))]
    [a b c d e]))
(defn koch-generation [segments]
  (vec (mapcat #(partition 2 1 (koch-points %)) segments)))
(defn koch-lines [generations]
  (nth (iterate koch-generation [[[10 290] [630 290]]]) (long generations)))

(defn tree-lines [start angle length ratio branch-angle depth stochastic?]
  (let [end (m/v+ start (m/v* (m/from-angle angle) length))]
    (cons [start end]
          (when (pos? depth)
            (let [angles (if stochastic?
                           (repeatedly (inc (m/random-int 3)) #(m/random (- (/ m/pi 2)) (/ m/pi 2)))
                           [branch-angle (- branch-angle)])]
              (mapcat #(tree-lines end (+ angle %) (* length ratio) ratio branch-angle (dec depth) stochastic?) angles))))))

(defn rewrite-sentence [sentence rules]
  (apply str (map #(get rules (str %) (str %)) sentence)))
(def plant-rules {"F" "FF+[+F-F-F]-[-F+F+F]"})
(defn turtle-lines [sentence length angle]
  (:lines
   (reduce (fn [{:keys [position direction stack] :as turtle} symbol]
             (case (str symbol)
               "F" (let [end (m/v+ position (m/v* (m/from-angle direction) length))]
                     (-> turtle (assoc :position end) (update :lines conj [position end])))
               "G" (assoc turtle :position (m/v+ position (m/v* (m/from-angle direction) length)))
               "+" (update turtle :direction + angle)
               "-" (update turtle :direction - angle)
               "[" (update turtle :stack conj [position direction])
               "]" (if (seq stack) (-> turtle (assoc :position (first (peek stack)) :direction (second (peek stack))) (update :stack pop)) turtle)
               turtle))
           {:position [320 350] :direction (- (/ m/pi 2)) :stack [] :lines []} sentence)))
(defn line-scene [lines caption]
  (d/scene (d/background) [[:no-fill] [:stroke 39 52 69] [:stroke-weight 1]]
           (mapcat (fn [[a b]] (d/line a b)) lines) (d/label caption)))
(defn unchanged [state _] state)
(defn empty-setup [_] {})
(def generations-param {:generations {:label "迭代次数" :default 4 :min 0 :max 5 :step 1}})
(defn circle-spec [id title original branches threshold]
  {:id id :chapter 8 :title title :original-title original :width 640 :height 360
   :params {:threshold {:label "递归终止半径" :default threshold :min 4 :max 40 :step 2}}
   :setup empty-setup :step unchanged
   :scene (fn [_ {:keys [threshold]}]
            (d/scene (d/background) [[:no-fill] [:stroke 39 52 69] [:stroke-weight 1]]
                     (recursive-circles 320 180 160 branches threshold)))
   :explanation (str "先绘制当前圆，再递归调用 " branches " 次。终止条件限制最小半径，保证计算结束。调整阈值，观察增加细节如何放大计算量。")})
(def examples
  [(circle-spec "8.1" "一次递归的同心圆" "Recursive Circles Once" 1 4)
   (circle-spec "8.2" "两次递归的圆" "Recursive Circles Twice" 2 8)
   (circle-spec "8.3" "四次递归的圆" "Recursive Circles Four Times" 4 16)
   {:id "8.4" :chapter 8 :title "Cantor 集" :original-title "The Cantor Set" :width 640 :height 360
    :params {:depth {:label "递归层数" :default 7 :min 1 :max 9 :step 1}} :setup empty-setup :step unchanged
    :scene (fn [_ {:keys [depth]}] (d/scene (d/background) [[:stroke 39 52 69] [:stroke-weight 3]] (cantor 10 50 620 (long depth))))
    :explanation "每一层保留线段左侧和右侧的三分之一，删除中间三分之一。递归函数接收起点、长度和剩余深度，原规则在任意尺度上重复。"}
   {:id "8.5" :chapter 8 :title "Koch 曲线" :original-title "The Koch Curve" :width 640 :height 360 :params generations-param
    :setup empty-setup :step unchanged
    :scene (fn [_ {:keys [generations]}] (line-scene (koch-lines generations) (str "线段数 = 4^" generations)))
    :explanation "把每条线段分成三份，以中间一份为底边构造等边三角形，使用四条新线段替代旧线段。向量旋转 −π/3 得到三角形顶点。迭代 n 次后线段数为 4ⁿ。"}
   {:id "8.6" :chapter 8 :title "递归树" :original-title "A Recursive Tree" :width 640 :height 360
    :params {:angle {:label "分支角度" :default 25 :min 0 :max 90 :step 1} :depth {:label "递归深度" :default 7 :min 1 :max 9 :step 1}}
    :setup empty-setup :step unchanged
    :scene (fn [_ {:keys [angle depth mouse pressed?]}]
             (let [a (if pressed? (m/map-range (first mouse) 0 640 0 (/ m/pi 2)) (* angle (/ m/pi 180)))]
               (line-scene (tree-lines [320 350] (- (/ m/pi 2)) 100 0.67 a (long depth) false) "拖动画布或调节角度")))
    :explanation "树干末端产生左右两个分支，长度缩为原来的 0.67。递归时每个分支保存独立的位置和方向，这与图形矩阵的 push/pop 等价。"}
   {:id "8.7" :chapter 8 :title "随机树" :original-title "A Stochastic Tree" :width 640 :height 360
    :params {:depth {:label "递归深度" :default 7 :min 1 :max 9 :step 1}}
    :setup (fn [{:keys [depth]}] {:lines (vec (tree-lines [320 350] (- (/ m/pi 2)) 100 0.67 0 (long depth) true))})
    :step (fn [state {:keys [clicked? depth frame]}]
            (if (or clicked? (zero? (mod frame 90)))
              {:lines (vec (tree-lines [320 350] (- (/ m/pi 2)) 100 0.67 0 (long depth) true))} state))
    :scene (fn [{:keys [lines]} _] (line-scene lines "点击生成一棵新树"))
    :explanation "递归结构不变，但每个节点随机产生 1 至 3 个分支，分支角度在 −π/2 到 π/2 之间取值。随机选择只在生成时发生，因此一帧内的树是稳定的。"}
   {:id "8.8" :chapter 8 :title "简单 L 系统的字符串生成" :original-title "Simple L-system Sentence Generation" :width 640 :height 360
    :params {:generations {:label "生成代数" :default 9 :min 1 :max 10 :step 1}} :setup empty-setup :step unchanged
    :scene (fn [_ {:keys [generations]}]
             (d/scene (d/background) [[:fill 39 52 69] [:no-stroke]]
                      (map-indexed (fn [i sentence] [:text (str i ": " sentence) 12 (+ 32 (* i 29))])
                                   (take (long generations) (iterate #(rewrite-sentence % {"A" "AB" "B" "A"}) "A")))))
    :explanation "L 系统同时替换上一代的所有符号。公理是 A，规则 A→AB、B→A。这里先观察字符串增长，再把相同的替换机制与海龟绘图连接。"}
   {:id "8.9" :chapter 8 :title "L 系统植物" :original-title "An L-system" :width 640 :height 360
    :params {:generations {:label "迭代次数" :default 4 :min 0 :max 5 :step 1} :angle {:label "转向角度" :default 25 :min 5 :max 60 :step 1}}
    :setup empty-setup :step unchanged
    :scene (fn [_ {:keys [generations angle]}]
             (let [sentence (nth (iterate #(rewrite-sentence % plant-rules) "F") (long generations))
                   length (/ 70 (m/pow 2 generations))]
               (line-scene (turtle-lines sentence length (* angle (/ m/pi 180)))
                           (str "规则 F→FF+[+F-F-F]-[-F+F+F] · 符号数 " (count sentence)))))
    :explanation "F 画线并前进，+ 和 − 转向，[ 把位置与方向压栈，] 恢复它们。生成字符串和解释字符串是两个独立步骤，因此同一套绘制器可以使用不同公理和生成规则。"}])

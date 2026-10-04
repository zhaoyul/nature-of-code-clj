(ns noc.chapters.ch07
  "第 7 章：同步更新的元胞自动机。"
  (:require [noc.math :as m] [noc.draw :as d]))

(defn elementary-next [cells rule]
  (let [n (count cells)]
    (mapv (fn [i]
            (if (or (zero? i) (= i (dec n))) 0
                (let [index (+ (* 4 (nth cells (dec i))) (* 2 (nth cells i)) (nth cells (inc i)))]
                  (bit-and 1 (bit-shift-right (long rule) index)))))
          (range n))))

(defn life-next
  "读取旧棋盘，构造整个新棋盘，避免更新顺序改变邻居计数。"
  [board columns rows]
  (mapv (fn [y]
          (mapv (fn [x]
                  (if (or (zero? x) (zero? y) (= x (dec columns)) (= y (dec rows))) 0
                      (let [neighbors (reduce + (for [dy [-1 0 1] dx [-1 0 1]
                                                      :when (not (and (zero? dx) (zero? dy)))]
                                                  (get-in board [(+ y dy) (+ x dx)])))
                            alive (get-in board [y x])]
                        (if (or (= neighbors 3) (and (= alive 1) (= neighbors 2))) 1 0))))
                (range columns)))
        (range rows)))

(defn life-setup [{:keys [density cell-size] :or {density 0.4 cell-size 10}}]
  (let [columns (long (/ 640 cell-size)) rows (long (/ 360 cell-size))
        board (mapv (fn [y] (mapv (fn [x] (if (or (zero? x) (zero? y) (= x (dec columns)) (= y (dec rows))) 0
                                              (if (< (m/random) density) 1 0))) (range columns))) (range rows))]
    {:board board :previous board :columns columns :rows rows :size cell-size :generation 0}))

(defn life-step [{:keys [board columns rows] :as state} {:keys [clicked? mouse]}]
  (let [board (if clicked?
                (let [[x y] mouse ix (long (/ x (:size state))) iy (long (/ y (:size state)))]
                  (if (and (< 0 ix (dec columns)) (< 0 iy (dec rows)))
                    (assoc-in board [iy ix] (- 1 (get-in board [iy ix]))) board)) board)]
    (assoc state :previous board :board (life-next board columns rows) :generation (inc (:generation state)))))

(defn life-scene [{:keys [board previous columns rows size generation]} transitions?]
  (d/scene (d/background)
           [[:stroke 222 222 222] [:stroke-weight 0.4]]
           (mapcat (fn [[x y]]
                     (let [alive (get-in board [y x]) before (get-in previous [y x])
                           color (cond (and transitions? (= before 0) (= alive 1)) [28 112 214]
                                       (and transitions? (= before 1) (= alive 0)) [224 75 75]
                                       (= alive 1) [39 52 69] :else [250 249 246])]
                       [(into [:fill] color) [:rect (* x size) (* y size) size size]]))
                   (for [y (range rows) x (range columns)] [x y]))
           (d/label (str "世代 " generation " · 点击切换元胞") 12 348)))

(def life-params {:density {:label "初始存活概率" :default 0.4 :min 0.05 :max 0.8 :step 0.05}
                  :cell-size {:label "元胞边长" :default 10 :min 8 :max 20 :step 2}})
(def examples
  [{:id "7.1" :chapter 7 :title "Wolfram 初等元胞自动机" :original-title "Wolfram Elementary Cellular Automata" :width 640 :height 360
    :params {:rule {:label "Wolfram 规则编号" :default 90 :min 0 :max 255 :step 1}}
    :setup (fn [_] {:cells (assoc (vec (repeat 80 0)) 40 1) :history [] :generation 0})
    :step (fn [{:keys [cells history generation] :as state} {:keys [rule] :or {rule 90}}]
            (if (< generation 45)
              (assoc state :cells (elementary-next cells rule) :history (conj history cells) :generation (inc generation)) state))
    :scene (fn [{:keys [history generation]} _]
             (d/scene (d/background) [[:no-stroke] [:fill 39 52 69]]
                      (for [[y row] (map-indexed vector history) [x cell] (map-indexed vector row) :when (= cell 1)]
                        [:rect (* 8 x) (* 8 y) 8 8])
                      (d/label (str "世代 " generation " · 比较规则 30、90、110"))))
    :explanation "每个元胞只有 0 和 1 两种状态。左邻、自身、右邻组成三位二进制数，用它读取规则编号中的对应位。每一行都由上一行同步生成；边界固定为 0。规则 90 从中心种子生成三角形分形。"}
   {:id "7.2" :chapter 7 :title "生命游戏" :original-title "Game of Life" :width 640 :height 360 :params life-params
    :setup life-setup :step life-step :scene (fn [state _] (life-scene state false))
    :explanation "生命游戏使用八个邻居：活元胞在邻居少于 2 或多于 3 时死亡，在 2 或 3 时存活；死元胞恰有 3 个活邻居时诞生。先读旧棋盘，再一次替换整个棋盘，才能实现真正的同步时间。"}
   {:id "7.3" :chapter 7 :title "保留前一状态的生命游戏" :original-title "Object-Oriented Game of Life" :width 640 :height 360 :params life-params
    :setup life-setup :step life-step :scene (fn [state _] (life-scene state true))
    :explanation "原例把状态与 previous 放在 Cell 对象中。Clojure 把它们保存为两个不可变棋盘：蓝色表示新生，红色表示死亡，深色表示继续存活。数据和绘制函数分离，同时保留原例的状态转换语义。"}])

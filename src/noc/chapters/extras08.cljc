(ns noc.chapters.extras08
  "第 8 章官网练习：动画、三维海龟与音乐 L 系统。"
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.ch08 :as f]))

(defn fractal-lines [[x1 y1] [x2 y2]]
  (let [dx (- x2 x1) dy (- y2 y1)]
    (cons [[x1 y1] [x2 y2]]
          (cond
            (and (zero? dx) (> dy 4))
            (concat (fractal-lines [(- x1 (/ dy 3)) y1] [(+ x1 (/ dy 3)) y1])
                    (fractal-lines [(- x1 (/ dy 3)) y2] [(+ x1 (/ dy 3)) y2]))
            (and (zero? dy) (> dx 4))
            (concat (fractal-lines [x1 (- y1 (/ dx 3))] [x1 (+ y1 (/ dx 3))])
                    (fractal-lines [x2 (- y1 (/ dx 3))] [x2 (+ y1 (/ dx 3))]))))))
(defn snowflake [generations]
  (let [a [210 95] b [430 95] c [320 (+ 95 (* 220 (m/cos (/ m/pi 6))))]]
    (nth (iterate f/koch-generation [[a b] [b c] [c a]]) (long generations))))
(defn animated-koch-setup [{:keys [generations]}]
  {:segments (mapv (fn [[a b]] {:a a :b b :angle 0})
                   (nth (iterate f/koch-generation [[[-300 120] [300 120]]]) (long generations)))})
(defn animated-koch-step [state _]
  (update state :segments #(mapv (fn [segment]
                                   (let [x (first (:a segment)) direction (if (pos? x) -1 1)]
                                     (update segment :angle + (* direction 0.025 (- 1 (/ (m/abs x) 640)))))) %)))
(defn cantor-array [generations]
  (let [next-gen (fn [segments]
                   (vec (mapcat (fn [[a b]]
                                  (let [v (m/v* (m/v- b a) (/ 1 3)) offset [0 35]]
                                    [[(m/v+ a offset) (m/v+ a v offset)] [(m/v+ b (m/v* v -1) offset) (m/v+ b offset)]])) segments)))]
    (vec (mapcat identity (take (inc (long generations)) (iterate next-gen [[[10 40] [630 40]]]))))))
(defn sierpinski [a b c depth]
  (if (zero? depth)
    [(into [:triangle] (concat a b c))]
    (let [ab (m/v* (m/v+ a b) 0.5) bc (m/v* (m/v+ b c) 0.5) ca (m/v* (m/v+ c a) 0.5)]
      (concat (sierpinski a ab ca (dec depth)) (sierpinski b bc ab (dec depth)) (sierpinski c ca bc (dec depth))))))
(defn thick-tree [start angle length branch-angle depth]
  (let [end (m/v+ start (m/v* (m/from-angle angle) length))]
    (concat [[:stroke-weight (m/map-range length 2 120 1 16)] (into [:line] (concat start end))]
            (when (pos? depth)
              (mapcat #(thick-tree end (+ angle %) (* length 0.67) branch-angle (dec depth)) [branch-angle (- branch-angle)])))))
(defn growing-branch [start velocity timer]
  {:start start :end start :velocity velocity :timer timer :timer-start timer :growing? true})
(defn growing-setup [_] {:branches [(growing-branch [320 350] [0 -1] 100)] :leaves []})
(defn growing-step [{:keys [branches leaves] :as state} _]
  (let [result
        (reduce (fn [{:keys [next-branches] :as acc} {:keys [end velocity timer timer-start growing?] :as branch}]
                  (if-not growing? (update acc :next-branches conj branch)
                          (let [end (m/v+ end velocity) timer (dec timer) split? (neg? timer)
                                branch (assoc branch :end end :timer timer :growing? (not split?))
                                acc (update acc :next-branches conj branch)]
                            (if split?
                              (if (< (+ (count branches) (count (:children acc))) 1024)
                                (update acc :children into (map #(growing-branch end (m/rotate velocity (* % (/ m/pi 180))) (* timer-start 0.66)) [30 -25]))
                                (update acc :leaves conj end)) acc))))
                {:next-branches [] :children [] :leaves leaves} branches)]
    (assoc state :branches (into (:next-branches result) (:children result)) :leaves (:leaves result))))
(defn growing-scene [{:keys [branches leaves]} _]
  (d/scene (d/background) [[:stroke 39 52 69] [:stroke-weight 1]]
           (map #(into [:line] (concat (:start %) (:end %))) branches)
           [[:fill 24 153 145] [:no-stroke]] (map (fn [[x y]] [:circle x y 5]) leaves)
           (d/label (str "生长中的分支 " (count branches) " · 叶片 " (count leaves)))))

(defn sway-branches [depth]
  (loop [all [{:parent nil :angle (- (/ m/pi 2)) :length 110 :seed (m/random)}] frontier [0] level 0]
    (if (= level depth) (assoc {:branches all} :leaves frontier)
        (let [children (vec (for [parent frontier delta [(- (/ m/pi 8)) (/ m/pi 8)]
                                  :let [p (nth all parent)]]
                              {:parent parent :angle (+ (:angle p) delta) :length (* 0.67 (:length p)) :seed (m/random)}))
              next-frontier (vec (range (count all) (+ (count all) (count children))))]
          (recur (into all children) next-frontier (inc level))))))
(defn sway-scene [{:keys [branches leaves time]} _]
  (let [result (reduce (fn [{:keys [ends] :as acc} {:keys [parent angle length seed]}]
                         (let [start (if (nil? parent) [320 350] (nth ends parent))
                               weight (m/map-range length 2 110 1 15)
                               noise-angle (m/map-range (m/noise time seed) 0.3 0.7 (- (/ m/pi 5)) (/ m/pi 5))
                               end (m/v+ start (m/v* (m/from-angle (+ angle (/ noise-angle weight))) length))]
                           (-> acc (update :ends conj end) (update :commands into [[:stroke-weight weight] (into [:line] (concat start end))]))))
                       {:ends [] :commands []} branches)]
    (d/scene (d/background) [[:stroke 139 69 19]] (:commands result)
             [[:no-stroke] [:fill 34 139 34]]
             (map (fn [index] (let [[x y] (nth (:ends result) index)] [:circle x y 5])) leaves))))

(defn v3+ [& vs] (reduce #(mapv + %1 %2) [0 0 0] vs))
(defn v3* [v scalar] (mapv #(* % scalar) v))
(defn basis-rotate [[right up forward] axis angle]
  (let [c (m/cos angle) s (m/sin angle)]
    (case axis
      :z [(v3+ (v3* right c) (v3* up s)) (v3+ (v3* right (- s)) (v3* up c)) forward]
      :x [right (v3+ (v3* up c) (v3* forward s)) (v3+ (v3* up (- s)) (v3* forward c))]
      :y [(v3+ (v3* right c) (v3* forward (- s))) up (v3+ (v3* right s) (v3* forward c))])))
(defn turtle3d [sentence length angle]
  (:lines
   (reduce (fn [{:keys [position basis stack] :as turtle} symbol]
             (case (str symbol)
               "F" (let [end (v3+ position (v3* (second basis) (- length)))] (-> turtle (assoc :position end) (update :lines conj [position end])))
               "G" (assoc turtle :position (v3+ position (v3* (second basis) (- length))))
               "+" (assoc turtle :basis (basis-rotate basis :z angle))
               "-" (assoc turtle :basis (basis-rotate basis :z (- angle)))
               "&" (assoc turtle :basis (basis-rotate basis :x angle))
               "^" (assoc turtle :basis (basis-rotate basis :x (- angle)))
               "<" (assoc turtle :basis (basis-rotate basis :y angle))
               ">" (assoc turtle :basis (basis-rotate basis :y (- angle)))
               "[" (update turtle :stack conj [position basis])
               "]" (if (seq stack) (-> turtle (assoc :position (first (peek stack)) :basis (second (peek stack))) (update :stack pop)) turtle)
               turtle))
           {:position [0 0 0] :basis [[1 0 0] [0 1 0] [0 0 1]] :stack [] :lines []} sentence)))
(defn project3d [[x y z] rotation]
  (let [x1 (+ (* x (m/cos rotation)) (* z (m/sin rotation))) z1 (+ (* x (- (m/sin rotation))) (* z (m/cos rotation)))
        tilt (* -25 (/ m/pi 180)) y1 (- (* y (m/cos tilt)) (* z1 (m/sin tilt))) z2 (+ (* y (m/sin tilt)) (* z1 (m/cos tilt)))
        scale (/ 500 (max 100 (+ 500 z2)))]
    [(+ 320 (* x1 scale)) (+ 340 (* y1 scale))]))
(def minor-pentatonic [44 46 48 49 51 54 56 58 61 63 66 68 70 73 75 78 80 82])
(defn musical-turtle [sentence length angle]
  (:segments
   (reduce (fn [{:keys [position direction note stack] :as turtle} symbol]
             (case (str symbol)
               "F" (let [end (m/v+ position (m/v* (m/from-angle direction) length))]
                     (-> turtle (assoc :position end) (update :segments conj {:line [position end] :note (nth minor-pentatonic note)})))
               "G" (-> turtle (assoc :position (m/v+ position (m/v* (m/from-angle direction) length))) (update :segments conj nil))
               "+" (-> turtle (update :direction + angle) (update :note #(mod (inc %) (count minor-pentatonic))))
               "-" (-> turtle (update :direction - angle) (update :note #(mod (dec %) (count minor-pentatonic))))
               "[" (update turtle :stack conj [position direction note])
               "]" (if (seq stack) (-> turtle (assoc :position (nth (peek stack) 0) :direction (nth (peek stack) 1) :note (nth (peek stack) 2))
                                       (update :stack pop) (update :segments conj nil)) turtle)
               turtle))
           {:position [320 350] :direction (- (/ m/pi 2)) :note 9 :stack [] :segments []} sentence)))
(defn midi-frequency [note] (* 440 (m/pow 2 (/ (- note 69) 12))))
(defn music-setup [{:keys [generations]}]
  (let [sentence (nth (iterate #(f/rewrite-sentence % f/plant-rules) "F") (long generations))]
    {:segments (musical-turtle sentence (/ 70 (m/pow 2 generations)) (* 25 (/ m/pi 180))) :index -1 :playing? false :tick 0 :new-note? false}))
(defn music-step [state {:keys [clicked? interval]}]
  (let [state (if clicked? (update state :playing? not) state) tick (inc (:tick state))
        next? (and (:playing? state) (zero? (mod tick (max 1 (long (* 30 interval))))))]
    (cond-> (assoc state :tick tick :new-note? next?) next? (update :index #(mod (inc %) (count (:segments state)))))))
(defn music-scene [{:keys [segments index playing? new-note? tick]} _]
  (let [current (when (<= 0 index) (nth segments index)) shown (take (inc index) segments)]
    (d/scene (d/background) [[:stroke 39 52 69]]
             (keep #(when % (into [:line] (apply concat (:line %)))) shown)
             (when (and current new-note?) [[:tone (midi-frequency (:note current)) 0.17 (str tick "-" index)]])
             (d/label (str "点击" (if playing? "暂停" "播放") " · 小调五声音阶 · " (if current (str "MIDI " (:note current)) "休止"))))))

(defn spec [number title directory explanation params setup step scene]
  {:id (str "exercise-" number) :chapter 8 :title (str "练习 " number "：" title) :original-title (str "Exercise " number)
   :directory (str "examples/08_fractals/" directory) :width 640 :height 360 :params params :setup setup :step step :scene scene :explanation explanation})
(def generations {:generations {:label "迭代次数" :default 4 :min 0 :max 5 :step 1}})
(def examples
  [(spec "8.1" "交替方向的递归线段" "exercise_8_1_fractal_lines"
         "水平线的两个端点产生竖直线，竖直线的两个端点产生水平线。每一级缩为前一级的三分之二，直到线段短于 4 像素。" {}
         f/empty-setup f/unchanged (fn [_ _] (f/line-scene (fractal-lines [160 180] [480 180]) "递归线段")))
   (spec "8.2" "Koch 雪花" "exercise_8_2_koch_snowflake"
         "从等边三角形的三条边同时执行 Koch 替换，得到封闭的雪花边界。" generations
         f/empty-setup f/unchanged (fn [_ {:keys [generations]}] (f/line-scene (snowflake generations) "三条初始边的 Koch 雪花")))
   (spec "8.3" "旋转的 Koch 线段" "exercise_8_3_contributed_solution"
         "原解答由 Rick Sidwell 提供。每条 Koch 线段独立绕画布中心旋转，旋转方向与速度由线段起点的横坐标决定。" generations
         animated-koch-setup animated-koch-step
         (fn [{:keys [segments]} _] (f/line-scene (map (fn [{:keys [a b angle]}] [(m/v+ [320 180] (m/rotate a angle)) (m/v+ [320 180] (m/rotate b angle))]) segments) "独立线段旋转")))
   (spec "8.4" "保存所有世代的 Cantor 线段" "exercise_8_4_contributed_solution"
         "原解答由 Rick Sidwell 提供。使用向量拆分线段，并保留每一代，而非只保存最后一代；这是递归与显式集合迭代的等价实现。" generations
         f/empty-setup f/unchanged (fn [_ {:keys [generations]}] (f/line-scene (cantor-array generations) "每代两条保留线段")))
   (spec "8.5" "Sierpinski 三角形" "exercise_8_5_contributed_solution"
         "原解答由 Rick Sidwell 提供。计算三条边的中点，用三个角上的小三角形替代旧三角形，递归 n 层产生 3ⁿ 个三角形。" {:depth {:label "递归深度" :default 5 :min 0 :max 7 :step 1}}
         f/empty-setup f/unchanged (fn [_ {:keys [depth]}] (d/scene (d/background) [[:fill 39 52 69] [:no-stroke]] (sierpinski [320 35] [145 338] [495 338] (long depth)))))
   (spec "8.7" "有粗细的树枝" "exercise_8_7_branch_thickness"
         "树枝长度映射为笔画宽度。粗壮树干逐渐变成细小末梢，递归拓扑与基本树例相同。" {:angle {:label "分支角度" :default 25 :min 0 :max 90 :step 1}}
         f/empty-setup f/unchanged (fn [_ {:keys [angle]}] (d/scene (d/background) [[:stroke 39 52 69]] (thick-tree [320 350] (- (/ m/pi 2)) 100 (* angle (/ m/pi 180)) 8))))
   (spec "8.8" "逐步生长的分支对象" "exercise_8_8_branch_objects_animation"
         "每个分支逐帧延长，计时结束后停止并生成两个子分支。没有递归函数，数组增长体现同样的递归结构；达到 1024 条分支后生成叶片。" {}
         growing-setup growing-step growing-scene)
   (spec "8.9" "随风摇摆的树" "exercise_8_9_contributed_solution"
         "原解答由 Rick Sidwell 提供。父分支端点成为子分支起点，噪声改变角度，细枝比粗枝摇摆得更多。不可变数据通过父索引保存拓扑关系。" {:depth {:label "分支深度" :default 8 :min 3 :max 10 :step 1}}
         (fn [{:keys [depth]}] (assoc (sway-branches (long depth)) :time 0)) (fn [state _] (update state :time + 0.0075)) sway-scene)
   (spec "8.11" "把 L 系统转换为线段数据" "exercise_8_11_contributed_solution"
         "原解答由 Rick Sidwell 提供。解释海龟指令后保存线段集合，再独立绘制。这使几何可用于碰撞、动画、导出和其他分析。" generations
         (fn [{:keys [generations]}] {:lines (f/turtle-lines (nth (iterate #(f/rewrite-sentence % f/plant-rules) "F") (long generations)) (/ 70 (m/pow 2 generations)) (* 25 (/ m/pi 180)))})
         f/unchanged (fn [{:keys [lines]} _] (f/line-scene lines (str "线段数据 " (count lines)))))
   (spec "8.12" "三维 L 系统海草" "exercise_8_12_contributed_solution"
         "原解答由 Rick Sidwell 提供。除平面旋转外，^ 和 & 控制俯仰，< 和 > 控制滚转。此实现维护三维位置与正交基，再把真实三维线段透视投影到 Quil 画布。" generations
         (fn [{:keys [generations]}] {:lines (turtle3d (nth (iterate #(f/rewrite-sentence % {"F" "FF-[&F^F^F]+[^F&F&F]>"}) "F") (long generations)) (/ 60 (m/pow 2 generations)) (* 22 (/ m/pi 180))) :rotation 0})
         (fn [state _] (update state :rotation + 0.025))
         (fn [{:keys [lines rotation]} _] (f/line-scene (map (fn [[a b]] [(project3d a rotation) (project3d b rotation)]) lines) "三维海龟：旋转观察")))
   (spec "8.13" "L 系统生成音乐" "exercise_8_13_contributed_solution"
         "原解答由 Rick Sidwell 提供。F 绘制线段并播放当前音符，+ 和 − 在小调五声音阶上移动，G 与 ] 产生休止。点击画布启用浏览器音频播放，几何和音符共用同一串生成规则。" (merge generations {:interval {:label "每个音符秒数" :default 0.2 :min 0.1 :max 0.8 :step 0.1}})
         music-setup music-step music-scene)])

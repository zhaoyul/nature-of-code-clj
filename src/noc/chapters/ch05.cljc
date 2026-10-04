(ns noc.chapters.ch05
  "Steering, paths, flocking, spatial indexes and trigonometric lookup."
  (:require [noc.math :as m] [noc.draw :as d]))

(defn vehicle [id position]
  {:id id :position position :velocity (m/v* (m/random-vector) 2)})

(defn seek [v target speed force]
  (m/limit (m/v- (m/set-mag (m/v- target (:position v)) speed) (:velocity v)) force))

(defn arrive [v target speed force radius]
  (let [delta (m/v- target (:position v))
        desired (m/set-mag delta (* speed (min 1 (/ (m/mag delta) radius))))]
    (m/limit (m/v- desired (:velocity v)) force)))

(defn move-vehicle [v force {:keys [width height max-speed]}]
  (let [velocity (m/limit (m/v+ (:velocity v) force) max-speed)]
    (assoc v :velocity velocity :position (m/wrap (m/v+ (:position v) velocity) width height))))

(defn nearest-point [point a b]
  (let [ab (m/v- b a) t (if (zero? (m/mag-sq ab)) 0 (/ (m/dot (m/v- point a) ab) (m/mag-sq ab)))]
    (m/v+ a (m/v* ab (m/clamp t 0 1)))))

(defn path-target [v path]
  (let [future (m/v+ (:position v) (m/set-mag (:velocity v) 35))
        candidates (map (fn [[a b]]
                          (let [normal (nearest-point future a b)]
                            {:future future :normal normal :distance (m/distance future normal)
                             :target (m/v+ normal (m/set-mag (m/v- b a) 30))}))
                        (partition 2 1 path))]
    (apply min-key :distance candidates)))

(defn neighbor? [v other radius]
  (let [distance (m/distance (:position v) (:position other))]
    (and (not= (:id v) (:id other)) (< 0 distance radius))))

(defn separation [v neighbors speed force radius]
  (let [near (filter #(neighbor? v % radius) neighbors)
        sum (reduce m/v+ [0 0]
                    (map #(let [delta (m/v- (:position v) (:position %))]
                            (m/vdiv (m/normalize delta) (m/mag delta))) near))]
    (if (seq near) (m/limit (m/v- (m/set-mag (m/vdiv sum (count near)) speed) (:velocity v)) force) [0 0])))

(defn flock-force [v neighbors {:keys [max-speed max-force neighborhood separation-weight alignment-weight cohesion-weight]}]
  (let [near (vec (filter #(neighbor? v % neighborhood) neighbors))
        alignment (if (seq near)
                    (m/limit (m/v- (m/set-mag (m/vdiv (reduce m/v+ [0 0] (map :velocity near)) (count near)) max-speed) (:velocity v)) max-force)
                    [0 0])
        cohesion (if (seq near)
                   (seek v (m/vdiv (reduce m/v+ [0 0] (map :position near)) (count near)) max-speed max-force) [0 0])]
    (m/v+ (m/v* (separation v neighbors max-speed max-force 25) separation-weight)
          (m/v* alignment alignment-weight) (m/v* cohesion cohesion-weight))))

(defn grid-index [vehicles resolution]
  (group-by (fn [v] (mapv #(long (m/floor (/ % resolution))) (:position v))) vehicles))

(defn grid-neighbors [grid v resolution width height]
  (let [[column row] (mapv #(long (m/floor (/ % resolution))) (:position v))
        columns (long (#?(:clj Math/ceil :cljs js/Math.ceil) (/ width resolution)))
        rows (long (#?(:clj Math/ceil :cljs js/Math.ceil) (/ height resolution)))]
    (vec (mapcat #(get grid %) (for [dx [-1 0 1] dy [-1 0 1]]
                                [(mod (+ column dx) columns) (mod (+ row dy) rows)])))))

(defn in-rect? [[x y] [left top width height]]
  (and (<= left x (+ left width)) (<= top y (+ top height))))

(defn intersects? [[ax ay aw ah] [bx by bw bh]]
  (and (<= ax (+ bx bw)) (<= bx (+ ax aw)) (<= ay (+ by bh)) (<= by (+ ay ah))))

(defn quadtree
  ([points bounds capacity] (quadtree points bounds capacity 0))
  ([points [x y width height :as bounds] capacity depth]
   (if (or (<= (count points) capacity) (>= depth 12))
     {:bounds bounds :points points}
     (let [hw (/ width 2) hh (/ height 2)
           quadrants [[x y hw hh] [(+ x hw) y hw hh] [x (+ y hh) hw hh] [(+ x hw) (+ y hh) hw hh]]
           groups (group-by (fn [[px py]] (+ (if (< px (+ x hw)) 0 1) (if (< py (+ y hh)) 0 2))) points)]
       {:bounds bounds :children (mapv (fn [i b] (quadtree (vec (get groups i [])) b capacity (inc depth))) (range 4) quadrants)}))))

(defn query-tree [{:keys [bounds points children]} area]
  (if (intersects? bounds area)
    (if children (mapcat #(query-tree % area) children) (filter #(in-rect? % area) points)) []))

(defn tree-boxes [{:keys [bounds children]}]
  (cons bounds (mapcat tree-boxes children)))

(defn setup [mode {:keys [width height population]}]
  (let [path (if (#{:path-simple :path-object} mode)
               [[0 (/ height 3)] [width (* height (/ 2.0 3.0))]]
               [[0 (* height 0.5)] [(* width 0.2) (* height 0.2)] [(* width 0.5) (* height 0.8)] [(* width 0.8) (* height 0.25)] [width (* height 0.5)]])
        group? (#{:separation :seek-separation :flock :grid} mode)
        points (when (= mode :quadtree)
                 (vec (repeatedly 900 #(vector (m/clamp (m/gaussian (/ width 2) (/ width 8)) 0 width)
                                              (m/clamp (m/gaussian (/ height 2) (/ height 8)) 0 height)))))]
    {:mode mode :path path
     :vehicles (mapv #(vehicle % [(m/random width) (m/random height)]) (range (if group? (long population) 1)))
     :points points :tree (when points (quadtree points [0 0 width height] 8))
     :sine (mapv #(m/sin (* % m/pi (/ 0.5 180))) (range 720))
     :cosine (mapv #(m/cos (* % m/pi (/ 0.5 180))) (range 720)) :time 0}))

(defn flow-direction [[x y] time]
  (m/from-angle (* m/tau (m/noise (+ (* x 0.008) time) (* y 0.008)))))

(defn step [{:keys [mode vehicles path tree] :as state} {:keys [width height mouse max-speed max-force arrival-radius path-radius neighborhood clicked?] :as env}]
  (cond
    (#{:path-object :multi-path :lookup} mode) (update state :time inc)
    (= mode :quadtree) (assoc state :selection (vec (query-tree tree [(- (first mouse) 50) (- (second mouse) 50) 100 100])))
    :else
    (let [vehicles (if (and clicked? (#{:flock :grid} mode)) (conj vehicles (vehicle (count vehicles) mouse)) vehicles)
          grid (when (= mode :grid) (grid-index vehicles neighborhood))
          diagnostics (atom [])
          next-vehicles
          (mapv (fn [v]
                  (let [neighbors (if grid (grid-neighbors grid v neighborhood width height) vehicles)
                        _ (swap! diagnostics conj (count neighbors))
                        target (when (#{:path-simple :path-follow} mode) (path-target v path))
                        force
                        (case mode
                          :seek (seek v mouse max-speed max-force)
                          :arrive (arrive v mouse max-speed max-force arrival-radius)
                          :walls (let [[x y] (:position v) [vx vy] (:velocity v) margin 40
                                       desired [(cond (< x margin) max-speed (> x (- width margin)) (- max-speed) :else vx)
                                                (cond (< y margin) max-speed (> y (- height margin)) (- max-speed) :else vy)]]
                                   (if (or (< x margin) (> x (- width margin)) (< y margin) (> y (- height margin)))
                                     (m/limit (m/v- (m/set-mag desired max-speed) (:velocity v)) max-force) [0 0]))
                          :flow (m/limit (m/v- (m/v* (flow-direction (:position v) (* 0.003 (:time state))) max-speed) (:velocity v)) max-force)
                          :path-simple (if (> (:distance target) path-radius) (seek v (:target target) max-speed max-force) [0 0])
                          :path-follow (if (> (:distance target) path-radius) (seek v (:target target) max-speed max-force) [0 0])
                          :separation (separation v neighbors max-speed max-force 32)
                          :seek-separation (m/v+ (m/v* (separation v neighbors max-speed max-force 32) 1.5) (seek v mouse max-speed max-force))
                          :flock (flock-force v neighbors env)
                          :grid (flock-force v neighbors env)
                          [0 0])]
                    (cond-> (move-vehicle v force env) target (assoc :path-target target)))) vehicles)]
      (assoc state :vehicles next-vehicles :grid grid :neighbor-checks (reduce + @diagnostics) :time (inc (:time state))))))

(defn vehicle-scene [{:keys [position velocity]}]
  (let [a (+ (m/heading velocity) (/ m/pi 2))
        points (mapv #(m/v+ position (m/rotate % a)) [[0 -8] [-4 5] [4 5]])]
    [[:stroke 39 52 69] [:fill 24 153 145] [:polyline points true]]))

(defn path-scene [path radius]
  [[:no-fill] [:stroke 210 214 219] [:stroke-weight (* 2 radius)] [:polyline path false]
   [:stroke 39 52 69] [:stroke-weight 1] [:polyline path false]])

(defn scene [{:keys [mode vehicles path tree points selection sine cosine time neighbor-checks]} {:keys [width height mouse path-radius neighborhood arrival-radius]}]
  (vec (concat
        (d/background)
        (when (#{:path-object :multi-path :path-simple :path-follow} mode) (path-scene path path-radius))
        (when (= mode :walls) [[:no-fill] [:stroke 180] [:rect 40 40 (- width 80) (- height 80)]])
        (when (= mode :flow)
          (mapcat #(let [[x y] %] (d/arrow [x y] (m/v* (flow-direction % (* 0.003 time)) 15) [190 195 199]))
                  (for [x (range 10 width 25) y (range 10 height 25)] [x y])))
        (when (= mode :grid)
          (concat [[:stroke 224] [:stroke-weight 1]]
                  (map #(vector :line % 0 % height) (range 0 width neighborhood))
                  (map #(vector :line 0 % width %) (range 0 height neighborhood))))
        (when (= mode :quadtree)
          (concat [[:no-fill] [:stroke 210] [:stroke-weight 1]]
                  (map #(into [:rect] %) (tree-boxes tree))
                  [[:no-stroke] [:fill 185]]
                  (map #(vector :circle (first %) (second %) 2) points)
                  [[:stroke 218 57 108] [:no-fill] [:rect (- (first mouse) 50) (- (second mouse) 50) 100 100] [:fill 218 57 108]]
                  (map #(vector :circle (first %) (second %) 4) selection)
                  (d/label (str "四叉树查询结果: " (count selection) " / " (count points)))))
        (when (= mode :lookup)
          (let [radius (+ 65 (* 55 (nth sine (mod time 720))))]
            (concat [[:no-stroke] [:fill 24 153 145]]
                    (for [i (range 0 720 10)]
                      [:circle (+ (/ width 2) (* radius (nth cosine i))) (+ (/ height 2) (* radius (nth sine i))) 5])
                    (d/label "0.5° 精度的正弦 / 余弦查找表"))))
        (when-not (#{:quadtree :lookup :path-object :multi-path} mode) (mapcat vehicle-scene vehicles))
        (when (#{:seek :arrive :seek-separation} mode)
          (concat [[:no-fill] [:stroke 218 57 108] [:circle (first mouse) (second mouse) 16]]
                  (when (= mode :arrive) [[:stroke 210] [:circle (first mouse) (second mouse) (* 2 arrival-radius)]])))
        (when (#{:path-simple :path-follow} mode)
          (mapcat (fn [{:keys [path-target]}]
                    (when path-target
                      (concat (d/line (:future path-target) (:normal path-target) [218 57 108])
                              (d/circle (:target path-target) 7 d/accent)))) vehicles))
        (when (#{:flock :grid} mode) (d/label (str "鸟群 " (count vehicles) " / 候选邻居检查 " neighbor-checks))))))

(def titles
  [["5.1" "寻找目标" "Seek" :seek] ["5.2" "到达目标" "Arrive" :arrive]
   ["5.3" "保持在墙内" "Stay within Walls" :walls] ["5.4" "跟随流场" "Flow Field Following" :flow]
   ["5.5" "创建路径对象" "A Path Object" :path-object] ["5.6" "简单路径跟随" "Simple Path Following" :path-simple]
   ["5.7" "多线段路径对象" "A Path with Multiple Segments" :multi-path] ["5.8" "路径跟随" "Path Following" :path-follow]
   ["5.9" "分离" "Separation" :separation] ["5.10" "组合寻找与分离" "Combining Seek and Separation" :seek-separation]
   ["5.11" "鸟群" "Flocking" :flock] ["5.12" "箱格空间细分" "Bin-Lattice Spatial Subdivision" :grid]
   ["5.13" "四叉树" "Quadtree" :quadtree] ["5.14" "Sin/Cos 查找表" "Sin/Cos Lookup Table" :lookup]])

(def examples
  (mapv (fn [[id title original-title mode]]
          {:id id :title title :original-title original-title :chapter 5 :width 640 :height 360
           :params {:max-speed {:label "最大速度" :default 3 :min 0.5 :max 8 :step 0.1}
                    :max-force {:label "最大转向力" :default 0.12 :min 0.01 :max 0.5 :step 0.01}
                    :population {:label "载具数量" :default 70 :min 5 :max 180 :step 5}
                    :arrival-radius {:label "减速范围" :default 90 :min 10 :max 150 :step 5}
                    :path-radius {:label "路径半径" :default 20 :min 5 :max 45 :step 1}
                    :neighborhood {:label "邻域半径" :default 55 :min 30 :max 100 :step 5}
                    :separation-weight {:label "分离权重" :default 1.5 :min 0 :max 3 :step 0.1}
                    :alignment-weight {:label "对齐权重" :default 1 :min 0 :max 3 :step 0.1}
                    :cohesion-weight {:label "聚合权重" :default 1 :min 0 :max 3 :step 0.1}}
           :setup (partial setup mode) :step step :scene scene :source "noc/chapters/ch05.cljc"
           :explanation (case mode
                          :seek "转向力等于期望速度减去当前速度, 并限制最大转向力. 鼠标就是目标位置."
                          :arrive "进入目标周围的减速范围后, 期望速度随距离线性减小, 使载具平稳停下."
                          :walls "接近边缘时改变期望速度的对应分量, 逐渐转向画布内部."
                          :flow "空间中的每个网格由 Perlin 噪声确定方向. 载具采样当前位置的流向并转向."
                          :path-object "路径保存起点、终点和半径, 半径表示可接受的路线走廊."
                          :path-simple "预测未来位置并投影到线段. 当预测点离路径过远, 载具寻找投影点前方的目标."
                          :multi-path "路径保存多个折线顶点, 可以用相邻点组成线段, 为完整路径跟随做准备."
                          :path-follow "对每条线段计算受范围限制的投影, 选择最近的投影, 再沿对应线段向前寻找目标."
                          :separation "邻居距离越近, 贡献的分离方向越强; 平均这些方向后形成受限转向力."
                          :seek-separation "寻找鼠标和避免拥挤同时产生转向力. 权重控制两种行为的取舍."
                          :flock "分离、速度对齐和向邻居中心聚合三种局部规则共同产生鸟群运动. 点击添加鸟."
                          :grid "每帧构建箱格索引, 只检查本格和八个相邻格; 画面显示实际候选邻居检查次数."
                          :quadtree "按容量递归分成四个子区域. 鼠标矩形查询只递归检查相交区域, 返回区域内的点."
                          :lookup "初始化720个值的0.5度正弦余弦查找表, 后续逐帧通过索引绘制脉动点阵圆.")}) titles))

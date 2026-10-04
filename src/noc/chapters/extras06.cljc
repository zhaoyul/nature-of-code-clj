(ns noc.chapters.extras06
  "第 6 章官网随附物理练习。"
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.ch06 :as p]))

(defn fixed [body] (assoc body :static? true :inv-mass 0 :inv-inertia 0))
(def physics-params {:gravity {:label "重力" :default 0.3 :min 0 :max 1 :step 0.05}
                     :iterations {:label "约束迭代次数" :default 5 :min 1 :max 12 :step 1}})
(defn empty-world [_] {:bodies [] :constraints [] :events [] :next-id 0})
(defn falling-step [state {:keys [pressed? mouse] :as env}]
  (let [state (if pressed? (-> state (update :bodies conj (p/box-body (:next-id state) mouse 16 16 1)) (update :next-id inc)) state)
        state (p/rigid-step state env)]
    (update state :bodies #(filterv (fn [b] (< (second (:position b)) 390)) %))))
(defn bridge-setup [_]
  {:bodies (mapv (fn [id] (cond-> (p/circle-body id [(* id 16) 130] 8 1) (or (= id 0) (= id 40)) fixed)) (range 41))
   :constraints (mapv (fn [id] {:a id :b (inc id) :length 16 :stiffness 1}) (range 40)) :events [] :next-id 41})
(defn bridge-step [state {:keys [clicked? mouse] :as env}]
  (let [drop? (or clicked? (< (m/random) 0.025))
        position (if clicked? mouse [320 -10])
        state (if drop? (-> state (update :bodies conj (p/box-body (:next-id state) position (m/random 8 16) (m/random 8 16) 2)) (update :next-id inc)) state)]
    (update (p/rigid-step state env) :bodies
            #(filterv (fn [body] (or (< (:id body) 41) (< (second (:position body)) 420))) %))))

(defn cloth-setup [{:keys [columns rows]}]
  (let [columns (long columns) rows (long rows) spacing (/ 620 (dec columns))
        id (fn [x y] (+ x (* y columns)))
        particles (mapv (fn [[x y]] (p/particle (id x y) [(+ 10 (* x spacing)) (+ 25 (* y spacing))] (and (zero? y) (zero? (mod x 4)))))
                        (for [y (range rows) x (range columns)] [x y]))
        springs (vec (concat (for [y (range rows) x (range (dec columns))] (p/spring (id x y) (id (inc x) y) spacing 0.25))
                             (for [y (range (dec rows)) x (range columns)] (p/spring (id x y) (id x (inc y)) spacing 0.25))))]
    {:particles particles :springs springs :attractors []}))
(defn cloth-scene [{:keys [particles springs]} _]
  (let [positions (into {} (map (juxt :id :position) particles))]
    (d/scene (d/background) [[:stroke 90 116 132] [:stroke-weight 1]]
             (map (fn [{:keys [a b]}] (into [:line] (concat (get positions a) (get positions b)))) springs)
             (d/label (str "布料节点 " (count particles) " · 弹簧 " (count springs) " · 拖动节点")))))
(def character-positions
  [[200 70] [250 70] [300 70] [350 70] [400 70] [350 170] [400 270] [350 270] [300 270] [250 270] [200 270] [250 170]
   [275 120] [325 120] [250 20] [350 20]])
(defn enhanced-character-setup [_]
  (let [particles (mapv (fn [id position] (p/particle id position false)) (range 16) character-positions)
        pairs (concat (for [i (range 12) j (range (inc i) 12)] [i j])
                      (for [i (range 12) eye [12 13]] [i eye])
                      [[14 1] [15 3] [14 3] [15 1] [14 0] [15 4] [15 2] [14 2] [14 15] [12 15] [12 14] [13 14] [13 15]])]
    {:particles particles :springs (mapv (fn [[a b]] (p/spring a b (m/distance (nth character-positions a) (nth character-positions b)) 0.001)) pairs)
     :attractors [] :show-springs? false :last-key nil}))
(defn enhanced-character-step [state {:keys [key pressed? mouse] :as env}]
  (let [key (if (keyword? key) (name key) key)
        state (cond-> state (and (contains? #{" " "space"} key) (not= key (:last-key state))) (update :show-springs? not))
        state (if pressed? (update state :particles #(assoc % 0 (assoc (nth % 0) :position mouse :previous mouse))) state)]
    (assoc (p/verlet-step state (assoc env :gravity 0 :pressed? false)) :last-key key)))
(defn enhanced-character-scene [{:keys [particles show-springs?] :as state} env]
  (let [position #(get-in particles [% :position])]
    (d/scene (if show-springs? (p/spring-scene state env) (d/background))
             [[:stroke 112 50 126] [:stroke-weight 4]
              (into [:line] (concat (position 1) (position 14))) (into [:line] (concat (position 3) (position 15)))
              [:fill 112 50 126] [:no-stroke]
              (into [:circle] (conj (position 14) 16)) (into [:circle] (conj (position 15) 16))
              [:stroke 39 52 69] [:stroke-weight 2] [:fill 45 197 244 190]
              [:polyline (mapv :position (take 12 particles)) true]]
             (mapcat (fn [id] (d/scene (d/circle (position id) 24 [252 238 33]) (d/circle (position id) 8 d/ink))) [12 13])
             (d/label "拖动角色 · 空格显示弹簧"))))

(defn graph-setup [{:keys [clusters] :or {clusters 9}}]
  (let [groups (mapv (fn [_] {:count (+ 3 (m/random-int 5)) :length (m/random 20 100)}) (range (long clusters)))
        offsets (vec (butlast (reductions + 0 (map :count groups))))
        nodes (vec (mapcat (fn [[group-index group]]
                             (map (fn [i] (assoc (p/particle (+ (nth offsets group-index) i) [(m/random 319 321) (m/random 179 181)] false) :group group-index)) (range (:count group))))
                           (map-indexed vector groups)))
        springs (vec (for [i (range (count nodes)) j (range (inc i) (count nodes))
                           :let [g1 (:group (nth nodes i)) g2 (:group (nth nodes j))
                                 internal? (= g1 g2) length (/ (+ (:length (nth groups g1)) (:length (nth groups g2))) 2)]]
                       (assoc (p/spring i j length (if internal? 0.01 0.05)) :minimum? (not internal?) :internal? internal?)))]
    {:particles nodes :springs springs :attractors [] :show-links? true :show-nodes? true :last-key nil}))
(defn graph-step [state {:keys [key] :as env}]
  (let [key (if (keyword? key) (name key) key)
        new-key? (and key (not= key (:last-key state)))
        state (cond-> state
                (and new-key? (= key "n")) (merge (graph-setup env))
                (and new-key? (= key "c")) (update :show-links? not)
                (and new-key? (= key "p")) (update :show-nodes? not))]
    (assoc (p/verlet-step state (assoc env :gravity 0)) :last-key key)))
(defn graph-scene [{:keys [particles springs show-links? show-nodes?]} all-links?]
  (let [positions (into {} (map (juxt :id :position) particles)) colors [d/accent d/teal [111 84 170] [228 157 52] [56 120 165]]]
    (d/scene (d/background)
             (when show-links?
               (mapcat (fn [{:keys [a b internal?]}]
                         (when (or internal? all-links?)
                           (d/scene [[:stroke-weight (if internal? 1.5 0.3)]] (d/line (get positions a) (get positions b) [70 85 100 (if internal? 100 25)])))) springs))
             (when show-nodes?
               (mapcat #(d/circle (:position %) 9 (nth colors (mod (:group %) (count colors)))) particles))
             (d/label "n：新图 · c：连线 · p：节点 · 同簇弹簧，异簇最小距离约束"))))

(defn compound-error-setup [_]
  {:bodies [(fixed (p/box-body 0 [320 340] 630 15 1))]
   :constraints [] :events [] :next-id 1})
(defn compound-error-step [state {:keys [clicked? mouse frame] :as env}]
  (let [drop? (and (< (count (:bodies state)) 101) (or clicked? (zero? (mod frame 30))))
        state (if drop?
                (-> state (update :bodies conj (assoc (p/compound-body (:next-id state) (if clicked? mouse [(m/random 60 580) 30])
                                                                       [(update (p/rectangle 24 4) :vertices #(mapv (fn [v] (m/v+ v [-6 0])) %))
                                                                        {:type :circle :offset [6 0] :radius 8}] 1) :angular-velocity 0.1))
                    (update :next-id inc)) state)]
    (update (p/rigid-step state env) :bodies
            #(filterv (fn [body] (or (< (:id body) 2) (< (second (:position body)) 420))) %))))
(defn compound-error-scene [state {:keys [pressed?] :as env}]
  (d/scene (p/world-scene state env)
           (when-not pressed?
             (mapcat (fn [{:keys [id position angle]}]
                       (when (pos? id)
                         [[:push] (into [:translate] position) [:rotate angle] [:no-fill] [:stroke 218 57 108]
                          [:rect -12 -2 24 4] [:circle 12 0 16] [:pop]])) (:bodies state)))
           (d/label (if pressed? "按住：质心坐标下的正确复合几何" "红色：误把父质心当首部件原点后的错位"))))
(defn motor-setup [_]
  {:bodies [(assoc (fixed (p/circle-body 0 [320 270] 2 1)) :collision? false) (p/box-body 1 [320 270] 120 10 4)]
   :constraints [{:a 0 :b 1 :length 0 :stiffness 1}] :events [] :next-id 2})
(defn motor-step [state {:keys [motor frame] :as env}]
  (let [state (update state :bodies #(mapv (fn [b] (if (= 1 (:id b)) (update b :angular-velocity + motor) b)) %))
        state (if (zero? (mod frame 22))
                (-> state (update :bodies conj (p/circle-body (:next-id state) [(m/random 260 380) 20] 8 1)) (update :next-id inc)) state)]
    (update (p/rigid-step state env) :bodies
            #(filterv (fn [body] (or (< (:id body) 2) (< (second (:position body)) 420))) %))))

(defn spec [id title directory explanation params setup step scene]
  {:id (str "extra-6-" id) :chapter 6 :title title :original-title title :directory (str "examples/06_libraries/" directory)
   :width 640 :height 360 :params params :setup setup :step step :scene scene :explanation explanation})
(def examples
  [(spec 1 "练习 6.2：及时删除掉出画布的刚体" "exercise_6_2_boxes"
         "按住鼠标生成方块。方块越过画布底部后，同时从状态与物理世界移除，防止不可见刚体继续占用计算。" physics-params empty-world falling-step p/world-scene)
   (spec 2 "复合刚体的质心与错误绘制" "6_5_compound_bodies_error"
         "官网用这一对比演示复合刚体质心不同于首部件中心。物理几何始终正确，红色轮廓故意把父质心当成首部件中心；按住鼠标隐藏错误轮廓，观察正确几何。" physics-params compound-error-setup compound-error-step compound-error-scene)
   (spec 3 "练习 6.5：约束连接的桥" "exercise_6_5_bridge"
         "一串圆形刚体用固定长度约束连接，桥两端锁定。掉落的方块与桥发生真实碰撞，约束迭代把负载传递给相邻节点。" physics-params bridge-setup bridge-step p/world-scene)
   (spec 4 "练习 6.7：带电机的风车" "exercise_6_7_windmill_motor"
         "风车转轴由零长度约束固定。持续的角加速度模拟在叶片端施加切向力产生的力矩，落下的圆形刚体参与碰撞并交换动量。" (merge physics-params {:motor {:label "电机角加速度" :default 0.003 :min -0.01 :max 0.01 :step 0.001}})
         motor-setup motor-step p/world-scene)
   (spec 5 "练习 6.10：布料模拟" "exercise_6_10_cloth_simulation"
         "在规则网格中连接水平与竖直邻居，每隔四个顶端节点固定一次。重力作用下，Verlet 积分与多次弹簧约束迭代让布料下垂。调整节点数量可改变网格分辨率。" (merge physics-params {:columns {:label "横向节点数" :default 65 :min 17 :max 65 :step 4} :rows {:label "纵向节点数" :default 20 :min 5 :max 25 :step 1}})
         cloth-setup p/verlet-step cloth-scene)
   (spec 6 "练习 6.11：增强的柔体角色" "exercise_6_11_soft_body_character_enhanced"
         "角色外轮廓、眼睛和触角都由粒子组成。轮廓粒子两两连接，眼睛和触角添加额外支撑弹簧。拖动第一个轮廓节点，形变通过约束传遍全身；空格切换弹簧显示。" physics-params enhanced-character-setup enhanced-character-step enhanced-character-scene)
   (spec 7 "图 6.18：力导向图" "figure_6_18"
         "每簇内部使用双向弹簧，簇之间使用仅在距离太短时生效的最小距离约束。局部拉力和簇间排斥逐渐形成布局，此图只显示簇内部连线。" (merge physics-params {:clusters {:label "簇数量" :default 9 :min 3 :max 12 :step 1}})
         graph-setup graph-step (fn [state _] (graph-scene state false)))
   (spec 8 "练习 6.13：展示所有力导向连接" "exercise_6_13_force_directed_graph"
         "与图 6.18 使用相同的弹簧与最小距离算法，并以细线展示不同簇之间的约束。n 重新生成随机图，c 切换连线，p 切换节点。" (merge physics-params {:clusters {:label "簇数量" :default 8 :min 3 :max 12 :step 1}})
         graph-setup graph-step (fn [state _] (graph-scene state true)))])

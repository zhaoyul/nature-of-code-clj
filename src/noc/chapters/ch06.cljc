(ns noc.chapters.ch06
  "Portable Clojure rigid-body SAT/impulse solver and Verlet spring solver.
  These replace Matter.js and Toxiclibs, so notebook models run on JVM and browser."
  (:require [noc.math :as m] [noc.draw :as d]))

(defn cross [[ax ay] [bx by]] (- (* ax by) (* ay bx)))
(defn perpendicular [[x y]] [(- y) x])
(defn rectangle [width height]
  (let [x (/ width 2) y (/ height 2)]
    {:type :polygon :vertices [[(- x) (- y)] [x (- y)] [x y] [(- x) y]]}))

(defn rigid-body [id position shapes mass]
  (let [radius (apply max 1 (mapcat (fn [shape]
                                    (if (= :circle (:type shape))
                                      [(+ (m/mag (:offset shape [0 0])) (:radius shape))]
                                      (map m/mag (:vertices shape)))) shapes))
        inertia (* mass radius radius 0.5)]
    {:id id :position position :velocity [0 0] :angle 0 :angular-velocity 0
     :mass mass :inertia inertia :inv-mass (/ 1 mass) :inv-inertia (/ 1 inertia)
     :radius radius :shapes shapes :color d/teal :static? false}))
(defn box-body [id position width height mass] (rigid-body id position [(rectangle width height)] mass))
(defn circle-body [id position radius mass] (rigid-body id position [{:type :circle :radius radius :offset [0 0]}] mass))
(defn polygon-body [id position vertices mass] (rigid-body id position [{:type :polygon :vertices vertices}] mass))
(defn compound-body [id position shapes mass] (rigid-body id position shapes mass))
(defn static-body [body] (assoc body :static? true :inv-mass 0 :inv-inertia 0))

(defn shape-points [{:keys [position angle]} shape]
  (let [points (if (= :circle (:type shape))
                 (mapv #(m/v+ (:offset shape [0 0]) (m/v* (m/from-angle (* m/tau (/ % 16))) (:radius shape))) (range 16))
                 (:vertices shape))]
    (mapv #(m/v+ position (m/rotate % angle)) points)))

(defn axes [points]
  (mapv (fn [[a b]] (m/normalize (perpendicular (m/v- b a))))
        (partition 2 1 (conj points (first points)))))
(defn projection [points axis]
  (let [values (map #(m/dot % axis) points)] [(apply min values) (apply max values)]))
(defn support [points axis]
  (let [largest (apply max (map #(m/dot % axis) points))
        edge (filter #(< (m/abs (- (m/dot % axis) largest)) 0.5) points)]
    (m/vdiv (reduce m/v+ [0 0] edge) (count edge))))

(defn sat-contact [a b ap bp]
  (let [candidates
        (mapv (fn [axis]
                (let [[amin amax] (projection ap axis) [bmin bmax] (projection bp axis)]
                  {:normal axis :depth (- (min amax bmax) (max amin bmin))}))
              (concat (axes ap) (axes bp)))]
    (when (every? #(pos? (:depth %)) candidates)
      (let [{:keys [normal] :as contact} (apply min-key :depth candidates)
            normal (if (neg? (m/dot (m/v- (:position b) (:position a)) normal)) (m/v* normal -1) normal)
            tangent (perpendicular normal)
            [at-min at-max] (projection ap tangent) [bt-min bt-max] (projection bp tangent)
            tangent-coordinate (/ (+ (max at-min bt-min) (min at-max bt-max)) 2)
            normal-coordinate (/ (+ (m/dot (support ap normal) normal)
                                    (m/dot (support bp (m/v* normal -1)) normal)) 2)
            point (m/v+ (m/v* tangent tangent-coordinate) (m/v* normal normal-coordinate))]
        (assoc contact :normal normal :point point)))))

(defn collision [a b]
  (when (and (not= false (:collision? a)) (not= false (:collision? b))
             (not (and (:static? a) (:static? b)))
             (< (m/distance (:position a) (:position b)) (+ (:radius a) (:radius b))))
    (let [contacts (for [as (:shapes a) bs (:shapes b)
                         :let [contact (sat-contact a b (shape-points a as) (shape-points b bs))]
                         :when contact] contact)]
      (when (seq contacts) (apply max-key :depth contacts)))))

(defn point-velocity [body offset]
  (m/v+ (:velocity body) (m/v* (perpendicular offset) (:angular-velocity body))))

(defn apply-impulse [body impulse offset sign]
  (if (:static? body) body
      (-> body (update :velocity m/v+ (m/v* impulse (* sign (:inv-mass body))))
          (update :angular-velocity + (* sign (:inv-inertia body) (cross offset impulse))))))

(defn resolve-contact [a b {:keys [normal depth point]} restitution]
  (let [sum (+ (:inv-mass a) (:inv-mass b))
        correction (m/v* normal (/ (* 0.75 (max 0 (- depth 0.2))) sum))
        a (if (:static? a) a (update a :position m/v- (m/v* correction (:inv-mass a))))
        b (if (:static? b) b (update b :position m/v+ (m/v* correction (:inv-mass b))))
        ra (m/v- point (:position a)) rb (m/v- point (:position b))
        relative (m/v- (point-velocity b rb) (point-velocity a ra))
        normal-speed (m/dot relative normal)
        denominator (+ sum (* (:inv-inertia a) (m/pow (cross ra normal) 2))
                       (* (:inv-inertia b) (m/pow (cross rb normal) 2)))]
    (if (pos? normal-speed) [a b]
        (let [j (/ (* (- (+ 1 restitution)) normal-speed) denominator)
              impulse (m/v* normal j)
              a (apply-impulse a impulse ra -1) b (apply-impulse b impulse rb 1)
              tangent (m/normalize (m/v- relative (m/v* normal normal-speed)))
              tangent-denom (+ sum (* (:inv-inertia a) (m/pow (cross ra tangent) 2))
                               (* (:inv-inertia b) (m/pow (cross rb tangent) 2)))
              jt (m/clamp (/ (- (m/dot relative tangent)) (max 1.0e-9 tangent-denom)) (* -0.2 j) (* 0.2 j))
              friction (m/v* tangent jt)]
          [(apply-impulse a friction ra -1) (apply-impulse b friction rb 1)]))))

(defn index-of [items id]
  (if (and (integer? id) (<= 0 id) (< id (count items)) (= id (:id (nth items id))))
    id (first (keep-indexed #(when (= id (:id %2)) %1) items))))

(defn solve-rigid-constraint [bodies {:keys [a b length stiffness] :or {length 0 stiffness 0.8}}]
  (let [ai (index-of bodies a) bi (index-of bodies b)]
    (if (and (some? ai) (some? bi))
      (let [body-a (nth bodies ai) body-b (nth bodies bi)
            delta (m/v- (:position body-b) (:position body-a)) distance (m/mag delta)
            sum (+ (:inv-mass body-a) (:inv-mass body-b))
            correction (if (and (pos? sum) (pos? distance))
                         (m/v* delta (/ (* (- distance length) stiffness) distance sum)) [0 0])]
        (cond-> bodies
          (not (:static? body-a)) (update-in [ai :position] m/v+ (m/v* correction (:inv-mass body-a)))
          (not (:static? body-b)) (update-in [bi :position] m/v- (m/v* correction (:inv-mass body-b))))) bodies)))

(defn rigid-step
  "Advance an impulse-based rigid world. Supports compound convex shapes and distance constraints."
  [{:keys [bodies constraints mode drag] :as state} {:keys [gravity restitution mouse pressed? attraction iterations] :or {gravity 0.2 restitution 0.35 attraction 16 iterations 8}}]
  (let [drag (when (and (= mode :mouse) pressed?)
               (or drag (some #(when (and (not (:static? %)) (< (m/distance (:position %) mouse) (+ (:radius %) 8))) (:id %)) (reverse bodies))))
        old-positions (into {} (map (juxt :id :position) bodies))
        center (some #(when (= :attractor (:role %)) (:position %)) bodies)
        moved
        (mapv (fn [body]
                (if (:static? body) body
                    (let [delta (when center (m/v- center (:position body)))
                          attract (if delta (m/v* (m/normalize delta) (/ attraction (m/pow (m/clamp (m/mag delta) 12 150) 2))) [0 0])
                          acceleration (m/v+ [0 (if center 0 gravity)] attract)
                          velocity (m/v* (m/v+ (:velocity body) acceleration) 0.999)
                          angular (if-let [motor (:motor body)] motor (* 0.995 (:angular-velocity body)))
                          position (m/v+ (:position body) velocity)]
                      (assoc body :velocity velocity :position (if (= drag (:id body)) (m/v+ (:position body) (m/v* (m/v- mouse (:position body)) 0.25)) position)
                             :angle (+ (:angle body) angular) :angular-velocity angular)))) bodies)
        result
        (reduce (fn [{:keys [bodies pairs]} [i j]]
                  (let [a (nth bodies i) b (nth bodies j)]
                    (if-let [contact (collision a b)]
                      (let [[a b] (resolve-contact a b contact restitution)]
                        {:bodies (assoc bodies i a j b) :pairs (conj pairs [(min (:id a) (:id b)) (max (:id a) (:id b))])})
                      {:bodies bodies :pairs pairs})))
                {:bodies moved :pairs #{}}
                (for [i (range (count moved)) j (range (inc i) (count moved))] [i j]))
        constrained (reduce (fn [bs _] (reduce solve-rigid-constraint bs constraints)) (:bodies result) (range (long iterations)))
        constrained-ids (set (mapcat (juxt :a :b) constraints))
        constrained
        (if (seq constraints)
          (mapv (fn [body] (if (or (:static? body) (not (constrained-ids (:id body)))) body
                               (assoc body :velocity (m/v- (:position body) (get old-positions (:id body)))))) constrained)
          constrained)
        new-pairs (remove (:contacts state #{}) (:pairs result))
        dynamic-ids (set (map :id (remove :static? constrained)))
        particle-pairs (filter #(every? dynamic-ids %) new-pairs)
        hit-ids (set (mapcat identity particle-pairs))
        colored (if (= mode :events) (mapv #(if (hit-ids (:id %)) (assoc % :color d/accent) %) constrained) constrained)]
    (assoc state :bodies colored :drag drag :contacts (:pairs result) :events (vec new-pairs)
           :collision-count (+ (:collision-count state 0) (count new-pairs)))))

(defn particle [id position locked?]
  {:id id :position position :previous position :locked? locked? :radius 5})
(defn spring [a b length stiffness] {:a a :b b :length length :stiffness stiffness})

(defn relax-spring [particles {:keys [a b length stiffness minimum?]}]
  (let [ai (index-of particles a) bi (index-of particles b)
        pa (nth particles ai) pb (nth particles bi)
        delta (m/v- (:position pb) (:position pa)) distance (m/mag delta)
        wa (if (:locked? pa) 0 1) wb (if (:locked? pb) 0 1) sum (+ wa wb)]
    (if (and (pos? sum) (pos? distance) (or (not minimum?) (< distance length)))
      (let [correction (m/v* delta (/ (* stiffness (- distance length)) distance sum))]
        (cond-> particles
          (pos? wa) (update-in [ai :position] m/v+ (m/v* correction wa))
          (pos? wb) (update-in [bi :position] m/v- (m/v* correction wb)))) particles)))

(defn verlet-step
  "Verlet integration, distance-spring relaxation, fixed nodes, local attractors and mouse dragging."
  [{:keys [particles springs attractors drag] :as state} {:keys [width height mouse pressed? gravity iterations] :or {gravity 0.2 iterations 8}}]
  (let [drag (when pressed? (or drag (:drag-id state)
                                (some #(when (< (m/distance (:position %) mouse) 18) (:id %)) particles)))
        moved (mapv (fn [{:keys [position previous locked? id] :as p}]
                      (cond
                        locked? p
                        (= id drag) (assoc p :position mouse :previous position)
                        :else
                        (let [force (reduce (fn [sum {:keys [position radius strength]}]
                                              (let [delta (m/v- position (:position p)) distance (m/mag delta)]
                                                (if (< 0 distance radius)
                                                  (m/v+ sum (m/v* (m/normalize delta) (* strength (- 1 (/ distance radius))))) sum)))
                                            [0 gravity] attractors)
                              next-position (m/v+ position (m/v* (m/v- position previous) 0.99) force)]
                          (assoc p :previous position :position (mapv (fn [value bound] (m/clamp value 2 (- bound 2))) next-position [width height]))))) particles)
        relaxed (reduce (fn [ps _]
                          (let [result (reduce relax-spring ps springs)]
                            (if drag (mapv #(if (= drag (:id %)) (assoc % :position mouse) %) result) result)))
                        moved (range (long iterations)))]
    (assoc state :particles relaxed :drag drag)))

(defn body-scene [body]
  (mapcat (fn [shape]
            (concat [(into [:stroke] d/ink) (into [:fill] (if (:static? body) [175 181 190] (:color body)))]
                    (if (= :circle (:type shape))
                      (let [[x y] (m/v+ (:position body) (m/rotate (:offset shape [0 0]) (:angle body)))]
                        [[:circle x y (* 2 (:radius shape))]])
                      [[:polyline (shape-points body shape) true]]))) (:shapes body)))

(defn world-scene [{:keys [bodies constraints mode collision-count]} _]
  (let [positions (into {} (map (juxt :id :position) bodies))]
    (vec (concat (d/background)
                 (mapcat #(d/line (get positions (:a %)) (get positions (:b %))) constraints)
                 (mapcat body-scene bodies)
                 (d/label (if (= mode :events) (str "collisionStart 事件: " collision-count) "凸形碰撞 / 冲量 / 距离约束"))))))

(defn spring-scene [{:keys [particles springs]} _]
  (let [positions (into {} (map (juxt :id :position) particles))]
    (vec (concat (d/background)
                 (mapcat #(d/line (get positions (:a %)) (get positions (:b %)) [90 116 132]) springs)
                 (mapcat #(d/circle (:position %) (* 2 (:radius % 5)) (if (:locked? %) d/accent d/teal)) particles)
                 (d/label "Verlet 积分 / 弹簧长度松弛; 拖动节点")))))

(defn boundaries [width height]
  [(static-body (box-body 10000 [(/ width 2) (+ height 12)] (+ width 80) 30 1))
   (static-body (box-body 10001 [-12 (/ height 2)] 30 height 1))
   (static-body (box-body 10002 [(+ width 12) (/ height 2)] 30 height 1))])

(defn random-polygon [sides radius]
  (mapv #(m/v* (m/from-angle (+ (/ m/pi 2) (* m/tau (/ % sides)))) radius) (range sides)))

(defn new-body [mode id position]
  (case mode
    :polygons (let [n (+ 3 (m/random-int 4))] (assoc (polygon-body id position (random-polygon n (m/random 12 22)) 1) :angle (m/random m/tau)))
    :compound (compound-body id position [(rectangle 10 38) {:type :circle :radius 16 :offset [0 -21]}] 1)
    :events (circle-body id position 13 1)
    :windmill (circle-body id position (m/random 8 14) 1)
    :attraction (assoc (circle-body id position (m/random 6 11) 1) :velocity (m/v* (m/normalize (perpendicular (m/v- position [320 180]))) 1.1))
    (assoc (box-body id position (m/random 16 30) (m/random 16 30) 1) :angle (m/random -0.3 0.3))))

(defn rigid-setup [mode {:keys [width height]}]
  (let [base {:mode mode :constraints [] :collision-count 0 :contacts #{} :next-id 8}
        walls (cond
                (= mode :boxes) []
                (#{:boundaries :polygons :compound} mode)
                [(static-body (box-body 10000 [(/ width 4) (- height 5)] (- (/ width 2) 50) 10 1))
                 (static-body (box-body 10001 [(* width 0.75) (- height 50)] (- (/ width 2) 50) 10 1))]
                :else (boundaries width height))
        bodies (mapv #(new-body mode % [(m/random 60 (- width 60)) (m/random 25 140)]) (range 8))]
    (case mode
      :pendulum
      (assoc base :bodies [(assoc (static-body (circle-body 0 [(/ width 2) 55] 5 1)) :collision? false)
                           (box-body 1 [(+ (/ width 2) 120) 150] 36 36 1)]
             :constraints [{:a 0 :b 1 :length 160 :stiffness 1}])
      :windmill
      (assoc base :bodies (into walls (concat (vector (assoc (static-body (circle-body 10003 [(/ width 2) (* height 0.55)] 4 1)) :collision? false)
                                                     (compound-body 10004 [(/ width 2) (* height 0.55)] [(rectangle 175 15) (rectangle 15 100)] 5)) bodies))
             :constraints [{:a 10003 :b 10004 :length 0 :stiffness 1}])
      :attraction
      (assoc base :bodies (into [(assoc (static-body (circle-body 10003 [(/ width 2) (/ height 2)] 22 1)) :role :attractor)] bodies))
      :default
      (assoc base :bodies (into walls (vector (box-body 0 [(* width 0.4) 80] 70 70 1)
                                            (assoc (box-body 1 [(* width 0.56) 25] 55 65 1) :angle 0.2)
                                            (circle-body 2 [(* width 0.5) 150] 25 1))))
      (assoc base :bodies (into walls bodies)))))

(defn spring-setup [mode {:keys [width height]}]
  (case mode
    :simple-spring {:mode mode :drag-id 1
                    :particles [(particle 0 [(/ width 2) 70] true) (assoc (particle 1 [(+ (/ width 2) 100) 190] false) :radius 12)]
                    :springs [(spring 0 1 130 0.15)]}
    :soft-pendulum
    (let [ps (mapv #(particle % [(+ (/ width 2) (* % 5)) (+ 40 (* % 10))] (zero? %)) (range 24))]
      {:mode mode :particles (update ps 23 assoc :radius 12) :springs (mapv #(spring % (inc %) 10 0.55) (range 23)) :drag-id 23})
    :soft-character
    (let [positions [[200 35] [400 35] [350 185] [400 335] [200 335] [250 185]]
          links [[0 1] [1 2] [2 3] [3 4] [4 5] [5 0] [5 2] [0 3] [1 4]]]
      {:mode mode :particles (mapv #(particle %1 %2 false) (range) positions)
       :springs (mapv (fn [[a b]] (spring a b (m/distance (nth positions a) (nth positions b)) 0.45)) links)})
    :cluster
    (let [n (+ 2 (m/random-int 18)) length (m/random 20 110)
          ps (mapv #(particle % [(m/random 319 321) (m/random 179 181)] false) (range n))]
      {:mode mode :particles ps :springs (vec (for [a (range n) b (range (inc a) n)] (spring a b length 0.01)))
       :show-springs? true :show-particles? true :last-key nil})
    :behaviors
    {:mode mode :particles (mapv #(particle % [(m/random width) (m/random height)] false) (range 50)) :springs []
     :attractor-position [(/ width 2) (/ height 2)]
     :attractors [{:position [(/ width 2) (/ height 2)] :radius width :strength 0.25}]}))

(defn step [state {:keys [frame clicked? mouse pressed? key width height motor-speed] :as env}]
  (let [mode (:mode state)]
    (if (contains? state :particles)
      (let [key-edge? (and key (not= key (:last-key state)))
            state (if (and (= mode :cluster) (or (and key-edge? (= key "n"))
                                                 (and (pos? frame) (zero? (mod frame 120)))))
                    (spring-setup :cluster env) state)
            state (cond-> state
                    (and (= mode :cluster) key-edge? (= key "c")) (update :show-springs? not)
                    (and (= mode :cluster) key-edge? (= key "p")) (update :show-particles? not))
            state (if (= mode :behaviors)
                    (let [position (if pressed? mouse (:attractor-position state))]
                      (assoc state :attractor-position position
                             :attractors (into [{:position position :radius width :strength 0.1}
                                                {:position position :radius 20 :strength -5}]
                                               (map (fn [p] {:position (:position p) :radius 16 :strength -1}) (:particles state))))) state)
            state (assoc state :last-key key)]
        (verlet-step state (if (#{:cluster :behaviors} mode) (assoc env :gravity 0) env)))
      (let [spawn? (and (not (#{:default :pendulum :attraction} mode)) (or clicked? (zero? (mod (or frame 0) 20))))
            state (if spawn? (-> state (update :bodies conj (new-body mode (:next-id state) (if clicked? mouse [(m/random 80 (- width 80)) 10]))) (update :next-id inc)) state)
            state (if (= mode :windmill) (update state :bodies #(mapv (fn [b] (if (= (:id b) 10004)
                                                                             (if (zero? motor-speed) (dissoc b :motor) (assoc b :motor motor-speed)) b)) %)) state)
            state (rigid-step state env)]
        (update state :bodies #(vec (filter (fn [b] (or (:static? b) (< (second (:position b)) (+ height 150)))) %)))))))

(defn scene [state env]
  (if (contains? state :particles)
    (case (:mode state)
      :soft-character (vec (concat (d/background)
                                   [[:stroke 39 52 69] [:fill 24 153 145 180] [:polyline (mapv :position (:particles state)) true]]
                                   (d/label "拖动六个顶点之一; 内部弹簧维持角色形状")))
      :cluster (spring-scene (assoc state :springs (if (:show-springs? state) (:springs state) [])
                                   :particles (mapv #(assoc % :radius (if (:show-particles? state) 5 0)) (:particles state))) env)
      (vec (concat (spring-scene state env)
                   (when (= :behaviors (:mode state))
                     (let [[x y] (:attractor-position state)]
                       (concat [[:no-fill] [:stroke 218 57 108] [:circle x y 40]]
                               (d/circle [x y] 32 d/accent)))))))
    (world-scene state env)))

(def titles
  [["6.1" "默认刚体世界" "Matter.js Default Render and Runner" :default]
   ["6.2" "Quil 与刚体世界" "p5.js with Matter.js" :boxes]
   ["6.3" "下落方块与边界碰撞" "Boxes and Boundaries" :boundaries]
   ["6.4" "多边形形状" "Polygon Shapes" :polygons]
   ["6.5" "复合刚体" "Compound Bodies" :compound]
   ["6.6" "刚体摆" "Matter.js Pendulum" :pendulum]
   ["6.7" "旋转风车" "Windmill" :windmill]
   ["6.8" "鼠标约束" "MouseConstraint" :mouse]
   ["6.9" "刚体吸引力" "Attraction" :attraction]
   ["6.10" "碰撞事件" "Collision Events" :events]
   ["6.11" "简单弹簧" "Toxiclibs.js Simple Spring" :simple-spring]
   ["6.12" "柔软摆" "Soft Pendulum" :soft-pendulum]
   ["6.13" "软体角色" "Soft-body Character" :soft-character]
   ["6.14" "集群" "Cluster" :cluster]
   ["6.15" "吸引与排斥行为" "Attraction and Repulsion Behaviors" :behaviors]])

(def examples
  (mapv (fn [[id title original-title mode]]
          {:id id :title title :original-title original-title :chapter 6 :width 640 :height 360
           :params {:gravity {:label "重力" :default 0.2 :min 0 :max 0.7 :step 0.01}
                    :restitution {:label "弹性" :default 0.35 :min 0 :max 0.9 :step 0.05}
                    :iterations {:label "弹簧求解次数" :default 8 :min 1 :max 20 :step 1}
                    :attraction {:label "刚体吸引力" :default 100 :min 0 :max 500 :step 5}
                    :motor-speed {:label "风车马达速度" :default 0 :min -0.08 :max 0.08 :step 0.005}}
           :setup (partial (if (#{:simple-spring :soft-pendulum :soft-character :cluster :behaviors} mode) spring-setup rigid-setup) mode)
           :step step :scene scene :source "noc/chapters/ch06.cljc"
           :explanation
           (str "本例用可在 JVM 和浏览器执行的 Clojure 求解器替换原书的 "
                (if (#{:simple-spring :soft-pendulum :soft-character :cluster :behaviors} mode) "Toxiclibs.js" "Matter.js")
                ". "
                (case mode
                  :default "矩形、圆和地面构成刚体世界. SAT 检测凸形接触, 冲量同时改变线速度和角速度."
                  :boxes "点击生成新方块; 刚体世界更新和 Quil 绘制分别负责物理和显示. 下落出画布的刚体自动移除."
                  :boundaries "静态地面和墙壁不随碰撞移动. 下落方块会与边界及其他方块相碰."
                  :polygons "三至六边形用局部顶点定义. 碰撞前把顶点旋转到世界坐标, 再沿边法向做分离轴检测."
                  :compound "长柄矩形和偏心圆共享同一位置、角度及角速度, 构成棒棒糖形状的复合刚体."
                  :pendulum "静态锚点和动态摆锤之间的固定距离约束限制运动, 重力产生沿圆弧的摆动."
                  :windmill "风车与静态中心以零长度约束连接. 落球冲量推动叶片; 马达参数可以施加规定角速度."
                  :mouse "按下鼠标选择刚体, 弹性约束逐步把它拉向鼠标位置, 同时保留与其他刚体的碰撞."
                  :attraction "静态中心产生平方反比吸引, 给运动刚体切向初速以观察轨道运动."
                  :events "保存上一帧的接触集合, 新接触触发 collisionStart 对应事件, 相碰粒子改变颜色并累加事件数."
                  :simple-spring "Verlet 积分使用当前位置与上一位置隐式保存速度, 弹簧通过长度松弛连接固定点和可拖动端点."
                  :soft-pendulum "连续24个粒子与相邻弹簧形成柔软绳摆, 固定首端并拖动末端观察传播和下垂."
                  :soft-character "六个顶点构成柔软角色轮廓, 沿轮廓及对角连接的弹簧维持形态. 可拖动任意顶点观察形变."
                  :cluster "完全连接的弹簧网络在中心附近的随机初始状态中展开. 每120帧生成新集群; n生成新图, c切换连接, p切换节点."
                  :behaviors "同一吸引子在远处吸引、近处排斥. 粒子之间也有短程排斥, 按住鼠标移动吸引子."))}) titles))

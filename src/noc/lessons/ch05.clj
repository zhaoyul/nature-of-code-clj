(ns noc.lessons.ch05
  "第 5 章正文片段的有限输入与不可变载具操作。"
  (:refer-clojure :exclude [future])
  (:require [noc.math :as m]
            [noc.draw :as d]
            [noc.chapters.ch05 :as ch]))

(def env
  {:width 640 :height 360 :mouse [400 180]
   :max-speed 8 :max-force 0.2 :path-radius 20
   :neighborhood 50 :separation-weight 1.5
   :alignment-weight 1 :cohesion-weight 1})

(defn make-vehicle
  ([x y] (make-vehicle 0 x y))
  ([id x y]
   {:id id :position [x y] :velocity [0 0] :acceleration [0 0]
    :radius 6 :max-speed 8 :max-force 0.2}))

(def vehicle
  (assoc (make-vehicle 0 160 180) :velocity [2 -1]))

(def vehicles
  [vehicle
   (assoc (make-vehicle 1 168 185) :velocity [-1 2])
   (assoc (make-vehicle 2 180 180) :velocity [1 1])
   (assoc (make-vehicle 3 205 195) :velocity [2 0])
   (assoc (make-vehicle 4 310 220) :velocity [-2 1])])

(defn apply-force [vehicle force]
  (update vehicle :acceleration m/v+ force))

(defn advance [vehicle]
  (let [velocity (m/limit (m/v+ (:velocity vehicle) (:acceleration vehicle))
                          (:max-speed vehicle))]
    (assoc vehicle :velocity velocity
           :position (m/v+ (:position vehicle) velocity)
           :acceleration [0 0])))

(defn steer [vehicle desired]
  (m/limit (m/v- desired (:velocity vehicle)) (:max-force vehicle)))

(defn seek-force [vehicle target]
  (ch/seek vehicle target (:max-speed vehicle) (:max-force vehicle)))

(defn seek [vehicle target]
  (apply-force vehicle (seek-force vehicle target)))

(defn separate-force
  ([vehicle neighbors]
   (separate-force vehicle neighbors (* 2 (:radius vehicle))))
  ([vehicle neighbors radius]
   (ch/separation vehicle neighbors (:max-speed vehicle) (:max-force vehicle) radius)))

(defn nearby [vehicle neighbors radius]
  (filterv #(ch/neighbor? vehicle % radius) neighbors))

(defn average-vector [vectors]
  (if (seq vectors)
    (m/vdiv (reduce m/v+ [0 0] vectors) (count vectors))
    [0 0]))

(defn angle-between [a b]
  (let [denominator (* (m/mag a) (m/mag b))]
    (if (zero? denominator) 0
        (Math/acos (double (m/clamp (/ (m/dot a b) denominator) -1 1))))))

(defn flow-field [resolution direction]
  (let [cols (long (m/floor (/ (:width env) resolution)))
        rows (long (m/floor (/ (:height env) resolution)))]
    {:resolution resolution :cols cols :rows rows
     :field (mapv (fn [column]
                    (mapv #(direction column %) (range rows)))
                  (range cols))}))

(defn lookup [{:keys [resolution cols rows field]} [x y]]
  (get-in field [(long (m/clamp (m/floor (/ x resolution)) 0 (dec cols)))
                (long (m/clamp (m/floor (/ y resolution)) 0 (dec rows)))]))

(def flow
  (flow-field 20 (fn [column row]
                   (m/from-angle (* m/tau (m/noise (* column 0.1) (* row 0.1)))))))

(def path
  {:radius 20 :start [0 120] :end [640 240]})

(def poly-path
  {:radius 20 :points [[0 120] [160 70] [320 280] [480 100] [640 240]]})

(defn future-position [vehicle distance]
  (m/v+ (:position vehicle) (m/set-mag (:velocity vehicle) distance)))

(defn normal-point
  "投影到无限延长线；线段范围检查由调用者执行。"
  [position a b]
  (let [from-start (m/v- position a)
        direction (m/normalize (m/v- b a))]
    (m/v+ a (m/v* direction (m/dot from-start direction)))))

(def future (future-position vehicle 25))
(def normal (normal-point future (:start path) (:end path)))

(defn triangle-scene [{:keys [position velocity radius]}]
  (let [angle (m/heading velocity)
        points (mapv #(m/v+ position (m/rotate % angle))
                     [[(* radius 2) 0] [(* radius -2) (- radius)]
                      [(* radius -2) radius]])]
    [[:fill 127] [:stroke 0] [:polyline points true]]))

(defn flock-force [vehicle neighbors]
  (ch/flock-force vehicle neighbors
                  (assoc env :max-speed (:max-speed vehicle)
                         :max-force (:max-force vehicle))))

(defn flock-step [vehicles]
  ;; 全部载具读取同一帧快照，避免更新顺序改变结果。
  (mapv #(advance (apply-force % (flock-force % vehicles))) vehicles))

(defn scene [vehicles]
  (apply d/scene (d/background) (map triangle-scene vehicles)))

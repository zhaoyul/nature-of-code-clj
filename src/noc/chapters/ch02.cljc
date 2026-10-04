(ns noc.chapters.ch02
  (:require [noc.math :as m] [noc.draw :as d]))

(defn body [p mass] {:position p :velocity [0 0] :mass mass :radius (* mass 8)})
(defn apply-force [b force]
  (update b :acceleration #(m/v+ (or % [0 0]) (m/vdiv force (:mass b)))))
(defn integrate [b]
  (let [v (m/v+ (:velocity b) (:acceleration b [0 0]))]
    (assoc b :velocity v :position (m/v+ (:position b) v) :acceleration [0 0])))
(defn edges [b {:keys [width height]}]
  (merge b (m/bounce (:position b) (:velocity b) width height (:radius b))))

(defn forces-setup [mode {:keys [width height]}]
  {:bodies (case mode
             :one [(body [(/ width 2) 30] 1)]
             :friction [(assoc (body [(/ width 2) (- height 24)] 3) :velocity [5 0])]
             :fluid (mapv #(body [(* (+ % 0.5) (/ width 5)) 30] (m/random 0.5 3)) (range 5))
             [(body [(* width 0.31) 30] 10) (body [(* width 0.69) 30] 2)])})

(defn external-forces [mode b {:keys [gravity wind friction drag pressed? height]}]
  (let [gravity-force [0 (if (#{:one :unscaled} mode) gravity (* gravity (:mass b))) ]
        contact? (>= (second (:position b)) (- height (:radius b) 1))
        friction-force (if (and (= mode :friction) contact?)
                         (m/set-mag (:velocity b)
                                    (- (min (* friction (:mass b)) (* (:mass b) (m/mag (:velocity b))))))
                         [0 0])
        drag-force (if (and (= mode :fluid) (> (second (:position b)) (/ height 2)))
                     (m/set-mag (:velocity b) (- (* drag (m/mag-sq (:velocity b))))) [0 0])]
    (-> b (apply-force gravity-force)
        (apply-force (if pressed? [wind 0] [0 0]))
        (apply-force friction-force) (apply-force drag-force))))

(defn forces-step [mode state env]
  (if (and (= mode :fluid) (:clicked? env))
    (merge state (forces-setup mode env))
    (update state :bodies #(mapv (fn [b] (edges (integrate (external-forces mode b env)) env)) %))))
(defn forces-scene [mode {:keys [bodies]} {:keys [width height]}]
  (d/scene (d/background)
           (when (= mode :fluid) [[:no-stroke] [:fill 24 153 145 70] [:rect 0 (/ height 2) width (/ height 2)]])
           (mapcat (fn [b] (d/circle (:position b) (* 2 (:radius b)) [218 57 108 120])) bodies)
           (d/label (if (= mode :fluid) "点击重置；下半区有流体阻力" "按住鼠标施加风力"))))

(defn attraction-force [source target g]
  (let [delta (m/v- (:position source) (:position target))
        distance (m/clamp (m/mag delta) 5 25)]
    (m/set-mag delta (/ (* g (:mass source) (:mass target)) (* distance distance)))))

(defn attraction-setup [mode {:keys [width height count]}]
  (let [center [(/ width 2) (/ height 2)]]
    {:attractor {:position center :mass 20 :dragging? false}
     :bodies (case mode
               :single [(assoc (body [(+ (first center) 90) (second center)] 1) :velocity [0 1.5])]
               :two [(assoc (body [(/ width 2) 40] 10) :velocity [1 0])
                     (assoc (body [(/ width 2) (- height 40)] 10) :velocity [-1 0])]
               (mapv (fn [_] (assoc (body [(m/random width) (m/random height)] (m/random 0.5 2))
                                   :velocity [(m/random -1 1) (m/random -1 1)]))
                     (range (long count))))}))

(defn attraction-step [mode state {:keys [mouse pressed? clicked? G]}]
  (let [a (:attractor state)
        drag? (and pressed? (or (:dragging? a) (and clicked? (< (m/distance mouse (:position a)) 30))))
        a (assoc a :position (if drag? mouse (:position a)) :dragging? drag?)
        bodies (:bodies state)
        next-bodies
        (mapv (fn [i b]
                (let [force (if (#{:two :many-body} mode)
                              (reduce m/v+ [0 0]
                                      (keep-indexed (fn [j other] (when (not= i j) (attraction-force other b G))) bodies))
                              (attraction-force a b G))]
                  (integrate (apply-force b force))))
              (range) bodies)]
    (assoc state :attractor a :bodies next-bodies)))
(defn attraction-scene [mode {:keys [attractor bodies]} _]
  (d/scene (d/background)
           (when-not (#{:two :many-body} mode) (d/circle (:position attractor) 40 d/teal))
           (mapcat (fn [b] (d/circle (:position b) (max 6 (* 16 (:mass b))) [218 57 108 140])) bodies)))

(def force-params
  {:gravity {:label "重力" :default 0.1 :min 0 :max 0.5 :step 0.01}
   :wind {:label "风力" :default 0.1 :min 0 :max 2 :step 0.1}})
(defn force-example [id title mode]
  {:id id :chapter 2 :title title :width 640 :height 240
   :params (merge force-params
                  (when (= mode :friction) {:friction {:label "摩擦系数" :default 0.05 :min 0 :max 0.5 :step 0.01}})
                  (when (= mode :fluid) {:drag {:label "阻力系数" :default 0.1 :min 0 :max 0.5 :step 0.01}}))
   :setup (partial forces-setup mode) :step (partial forces-step mode) :scene (partial forces-scene mode)
   :explanation "每个力除以质量得到加速度；积分后清空加速度，下一帧重新计算合力。"})
(defn attraction-example [id title mode n]
  {:id id :chapter 2 :title title :width 640 :height 360
   :params {:G {:label "引力常数" :default 1 :min 0 :max 4 :step 0.1}
            :count {:label "物体数量（重置生效）" :default n :min 2 :max 40 :step 1}}
   :setup (partial attraction-setup mode) :step (partial attraction-step mode) :scene (partial attraction-scene mode)
   :explanation "按 F = Gm₁m₂/r² 计算引力，用受力前的共同状态同时更新所有物体，避免遍历次序造成偏差。"})
(def examples
  [(force-example "2.1" "力" :one)
   (force-example "2.2" "作用于两个物体的力" :unscaled)
   (force-example "2.3" "按质量缩放重力" :scaled)
   (force-example "2.4" "加入摩擦" :friction)
   (force-example "2.5" "流体阻力" :fluid)
   (attraction-example "2.6" "引力吸引" :single 1)
   (attraction-example "2.7" "多个运动者受到吸引" :many 20)
   (attraction-example "2.8" "双体相互吸引" :two 2)
   (attraction-example "2.9" "N 体相互吸引" :many-body 10)])

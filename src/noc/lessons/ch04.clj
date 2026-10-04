(ns noc.lessons.ch04
  "粒子、发射器和按数据类型分派的动物示例。"
  (:require [noc.math :as m] [noc.draw :as d]))

(defn particle [position]
  {:position position :velocity [(m/random -1 1) (m/random -2 0)]
   :acceleration [0 0] :lifespan 255 :mass 1 :kind :circle :angle 0})
(defn apply-force [p force]
  (update p :acceleration m/v+ (m/vdiv force (:mass p))))
(defn update-particle [p]
  (let [velocity (m/v+ (:velocity p) (:acceleration p))]
    (-> p (assoc :velocity velocity :position (m/v+ (:position p) velocity) :acceleration [0 0])
        (update :lifespan - 2))))
(defn dead? [p] (neg? (:lifespan p)))
(defn emitter [origin] {:origin origin :particles []})
(defn add-particle [system]
  (update system :particles conj (particle (:origin system))))
(defn update-emitter [system]
  (update system :particles #(filterv (complement dead?) (mapv update-particle %))))
(defn force-emitter [system force]
  (update system :particles #(mapv (fn [p] (apply-force p force)) %)))
(defn repel [repeller p]
  (let [direction (m/v- (:position p) (:position repeller))
        distance (m/clamp (m/mag direction) 5 50)]
    (m/set-mag direction (/ (:power repeller) (* distance distance)))))
(defn repel-emitter [system repeller]
  (update system :particles #(mapv (fn [p] (apply-force p (repel repeller p))) %)))
(defn particle-scene [{:keys [position lifespan kind angle]}]
  (let [[x y] position]
    (if (= kind :square)
      [[:stroke 0 lifespan] [:fill 0 lifespan] [:push] [:translate x y]
       [:rotate angle] [:rect -6 -6 12 12] [:pop]]
      [[:stroke 0 lifespan] [:fill 0 lifespan] [:circle x y 8]])))
(defn emitter-scene [system]
  (d/scene (d/background) (mapcat particle-scene (:particles system))))
(def samples
  [{:position [200 80] :velocity [1 -1] :acceleration [0 0.1] :lifespan 255 :mass 1 :kind :circle :angle 0}
   {:position [240 100] :velocity [-1 1] :acceleration [0 0.1] :lifespan -1 :mass 2 :kind :square :angle 0}])

(defn animal [kind] {:kind kind :age 0})
(defn eat [a] (case (:kind a) :dog "Woof! Woof! Slurp." "Yum!"))
(defn sleep [_] "Zzzzzz")
(defn voice [a] (case (:kind a) :dog "WOOF!" :cat "MEOW!" :turtle "..." :kiwi "Kee-wee!"))
(defn kingdom []
  (vec (mapcat (fn [[kind n]] (repeat n (animal kind))) [[:dog 10] [:cat 15] [:turtle 6] [:kiwi 98]])))

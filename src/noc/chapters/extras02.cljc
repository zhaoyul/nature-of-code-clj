(ns noc.chapters.extras02
  "Helium and an interacting planetary system: companion force exercises."
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.extras00 :as extras]))

(defn spec [i directory title setup step scene params]
  {:id (str "extra-2-" i) :chapter 2 :width 640 :height 240 :title title
   :directory (str "examples/02_forces/" directory) :params params
   :source "noc/chapters/extras02.cljc"
   :setup setup :step step :scene scene})
(defn balloon-setup [{:keys [width height]}]
  {:position [(/ width 2) (/ height 2)] :velocity [0 0] :noise-x 1000})
(defn balloon-step [{[x y] :position [vx vy] :velocity :keys [noise-x] :as state}
                    {:keys [width height helium wind]}]
  (let [radius (/ height 10) hit? (< (- y radius 1) 0)
        y (if hit? (+ radius 1) y)
        vy (if hit? (* -0.75 vy) vy)
        v (m/limit [(+ vx (* wind (m/map-range (m/noise noise-x) 0 1 -0.01 0.01)))
                    (- vy helium)] 5)
        [px py] (m/v+ [x y] v)]
    (assoc state :velocity v :position [(mod px width) py] :noise-x (+ noise-x 0.01))))
(defn balloon-scene [{[x y] :position} {:keys [height]}]
  (let [diameter (/ height 5) bottom (+ y (/ diameter 2))]
    (d/scene (d/background) (d/circle [x y] diameter [240 155 180])
             [[:triangle x bottom (+ x 5) (+ bottom 5) (- x 5) (+ bottom 5)]
              [:line x (+ bottom 5) x (+ bottom 100)]])))

(defn bodies-setup [{:keys [count]}]
  {:bodies (mapv (fn [_]
                  (let [unit (m/random-vector)]
                    {:position (m/v* unit (m/random 100 150))
                     :velocity (m/rotate (m/v* unit (m/random 10 15)) (/ m/pi 2))
                     :mass (m/random 10 15)})) (range (long count)))})
(defn acceleration [body attractor g]
  (let [direction (m/v- (:position attractor) (:position body))
        distance-sq (m/clamp (m/mag-sq direction) 100 1000)]
    (m/set-mag direction (/ (* g (:mass attractor)) distance-sq))))
(defn bodies-step [{:keys [bodies] :as state} {:keys [gravity sun-mass]}]
  (assoc state :bodies
         (mapv (fn [i body]
                 (let [a (reduce m/v+ (acceleration body {:position [0 0] :mass sun-mass} gravity)
                                 (for [j (range (count bodies)) :when (not= i j)]
                                   (acceleration body (nth bodies j) gravity)))
                       v (m/v+ (:velocity body) a)]
                   (assoc body :velocity v :position (m/v+ (:position body) v))))
               (range) bodies)))
(defn bodies-scene [{:keys [bodies]} {:keys [width height]}]
  (d/scene (d/background) [[:push] [:translate (/ width 2) (/ height 2)] [:stroke 39 52 69] [:fill 39 52 69 100]]
           (mapv (fn [{[x y] :position :keys [mass]}] [:circle x y (* 2 (m/sqrt mass))]) bodies)
           [[:pop]] (d/label "中心恒星与行星之间、所有行星之间都存在引力" 12 22)))

(def examples
  [(spec 1 "nature_of_code_exercise_2_1_solution" "练习 2.1：氦气球与噪声风力" balloon-setup balloon-step balloon-scene
         {:helium (extras/parameter "向上浮力" 0.02 0 0.1 0.002) :wind (extras/parameter "风力倍率" 1 0 10 0.1)})
   (spec 2 "exercise_2_16" "练习 2.16：恒星与相互吸引的行星" bodies-setup bodies-step bodies-scene
         {:count (extras/parameter "行星数（重置生效）" 100 2 120 1)
          :gravity (extras/parameter "引力常数" 1 0 3 0.05) :sun-mass (extras/parameter "恒星质量" 500 0 1000 10)})])

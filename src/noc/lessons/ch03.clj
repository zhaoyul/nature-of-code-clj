(ns noc.lessons.ch03
  "角运动、弹簧和单摆教学单元的显式样本数据。"
  (:require [noc.math :as m] [noc.draw :as d]))

(def env {:width 640 :height 240 :mouse [320 120] :pressed? false :clicked? false
          :angular-acceleration 0.0001 :angular-speed 0.02 :radius 108
          :amplitude 200 :period 120 :frame 30 :count 4 :wavelength 750
          :rest-length 100 :stiffness 0.2 :damping 0.99 :mass 1 :gravity 0.4
          :length 175})
(def mover {:position [320 120] :velocity [2 1] :acceleration [0.01 0.05]
            :mass 1 :radius 16 :angle 0 :angular-velocity 0 :angular-acceleration 0})
(def spring {:anchor [320 10] :rest-length 100 :k 0.2})
(def bob {:position [320 130] :velocity [0 0] :acceleration [0 0] :mass 1})

(defn apply-force [body force]
  (update body :acceleration m/v+ (m/vdiv force (:mass body))))

(defn update-mover [body]
  (let [velocity (m/v+ (:velocity body) (:acceleration body))
        angular-velocity (+ (:angular-velocity body) (:angular-acceleration body))]
    (assoc body :velocity velocity :position (m/v+ (:position body) velocity)
           :angular-velocity angular-velocity :angle (+ (:angle body) angular-velocity)
           :acceleration [0 0])))

(defn spring-force [{:keys [anchor rest-length k]} {:keys [position]}]
  (let [direction (m/v- position anchor)
        stretch (- (m/mag direction) rest-length)]
    (m/set-mag direction (* (- k) stretch))))

(defn constrain-length [body anchor minimum maximum]
  (let [direction (m/v- (:position body) anchor)
        length (m/mag direction)]
    (if (<= minimum length maximum)
      body
      (assoc body :position (m/v+ anchor (m/set-mag direction (m/clamp length minimum maximum)))
             :velocity [0 0]))))

(defn move-bob [body]
  (let [velocity (m/v+ (:velocity body) (:acceleration body))]
    (assoc body :velocity velocity :position (m/v+ (:position body) velocity) :acceleration [0 0])))

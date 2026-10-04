(ns noc.physics-test
  (:require [clojure.test :refer [deftest is testing]]
            [noc.chapters.ch06 :as p] [noc.math :as m] [noc.draw :as d]))

(def env {:width 640 :height 360 :mouse [320 180] :pressed? false :gravity 0.3
          :restitution 0 :iterations 8 :attraction 100 :motor-speed 0})
(defn advance [step state config frames]
  (reduce (fn [s frame] (step s (assoc config :frame frame))) state (range frames)))

(deftest elastic-collision-conserves-momentum
  (let [a (assoc (p/box-body 0 [90 90] 20 20 1) :velocity [2 0])
        b (assoc (p/box-body 1 [109 90] 20 20 1) :velocity [-2 0])
        [a b] (p/resolve-contact a b (p/collision a b) 1)]
    (is (= [-2.0 0.0] (:velocity a)))
    (is (= [2.0 0.0] (:velocity b)))
    (is (= [0.0 0.0] (m/v+ (:velocity a) (:velocity b))))
    (is (zero? (:angular-velocity a)))))

(deftest falling-body-rests-on-static-floor
  (let [floor (p/static-body (p/box-body 0 [320 350] 640 20 1))
        body (p/box-body 1 [200 20] 20 20 1)
        states (take 400 (iterate #(p/rigid-step % env) {:bodies [floor body] :constraints []}))
        final (last states)]
    (is (< (apply max (map #(get-in % [:bodies 1 :position 1]) states)) 333))
    (is (< (m/abs (get-in final [:bodies 1 :velocity 1])) 0.5))
    (is (= (:position floor) (get-in final [:bodies 0 :position])))))

(deftest pendulum-distance-constraint
  (let [state (advance p/step (p/rigid-setup :pendulum env) env 300)
        [a b] (map :position (:bodies state))]
    (is (< (m/abs (- 160 (m/distance a b))) 1e-8))))

(deftest collision-start-is-not-repeated-for-persistent-contact
  (let [state (advance p/rigid-step
                       {:mode :events :bodies [(p/box-body 0 [100 100] 20 20 1)
                                              (p/box-body 1 [115 100] 20 20 1)] :constraints []}
                       (assoc env :gravity 0) 10)]
    (is (= 1 (:collision-count state)))
    (is (= [d/accent d/accent] (mapv :color (:bodies state))))))

(deftest verlet-keeps-fixed-anchors-and-length
  (let [state (advance p/verlet-step
                       {:particles [(p/particle 0 [320 60] true) (p/particle 1 [430 190] false)]
                        :springs [(p/spring 0 1 130 1)]} env 120)]
    (is (= [320 60] (get-in state [:particles 0 :position])))
    (is (< (m/abs (- 130 (apply m/distance (map :position (:particles state))))) 1e-8))))

(deftest minimum-springs-only-repel-near-nodes
  (let [nodes [(p/particle 0 [0 0] false) (p/particle 1 [100 0] false)]
        minimum (assoc (p/spring 0 1 50 1) :minimum? true)]
    (is (= nodes (p/relax-spring nodes minimum)))
    (is (= 50.0 (apply m/distance (map :position (p/relax-spring (assoc-in nodes [1 :position] [10 0]) minimum)))))))

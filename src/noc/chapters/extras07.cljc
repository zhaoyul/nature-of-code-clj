(ns noc.chapters.extras07
  "第 7 章官网随附练习。"
  (:require [noc.math :as m] [noc.draw :as d]))
(defn hexagon [[x y] radius]
  (mapv (fn [i] (m/v+ [x y] (m/v* (m/from-angle (* i (/ m/pi 3))) radius))) (range 6)))
(defn hex-setup [_]
  {:cells (vec (for [y (range 22) x (range 12)]
                 {:position [(+ (* x 60) (if (even? y) 30 0)) (* y 17.320508)] :state (m/random-int 2)}))})
(def examples
  [{:id "exercise-7.8" :chapter 7 :title "练习 7.8：六边形网格" :original-title "Exercise 7.8: Hexagonal Grid"
    :directory "examples/07_ca/exercise_7_8_hexagon_ca" :width 640 :height 360 :params {}
    :setup hex-setup
    :step (fn [state {:keys [clicked? mouse]}]
            (if clicked?
              (update state :cells (fn [cells] (let [nearest (:position (apply min-key #(m/distance mouse (:position %)) cells))]
                                                 (mapv #(if (= nearest (:position %)) (update % :state (fn [n] (- 1 n))) %) cells)))) state))
    :scene (fn [{:keys [cells]} _]
             (d/scene (d/background) [[:stroke 39 52 69]]
                      (mapcat (fn [{:keys [position state]}] [(into [:fill] (if (= state 1) d/ink d/paper)) [:polyline (hexagon position 20) true]]) cells)
                      (d/label "官网练习的随机六边形布局 · 点击翻转状态")))
    :explanation "用每隔 π/3 的六个顶点绘制正六边形，并使相邻行错开。官网附带解答只绘制初始随机网格，没有定义下一代规则；这里保留这一行为，并增加点击翻转元胞，供进一步设计六邻域规则。"}])

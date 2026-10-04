(ns noc.lessons.ch07
  "同步元胞自动机的小型棋盘和可观察的错误更新。"
  (:require [noc.math :as m] [noc.draw :as d] [noc.chapters.ch07 :as ch]))

(def cells [1 0 1 0 0 0 0 1 0 1 1 1 0 0 0 1 1 1 0 0])
(def ruleset [0 1 0 1 1 0 1 0])
(def board [[0 0 0 0 0] [0 0 1 0 0] [0 0 1 0 0] [0 0 1 0 0] [0 0 0 0 0]])
(defn rules [a b c] (nth ruleset (- 7 (+ (* 4 a) (* 2 b) c))))
(defn sequential-next [cells]
  ;; 故意读取刚改过的左邻：用于和同步更新比较，展示错误来自哪里。
  (reduce (fn [row i] (assoc row i (rules (nth row (dec i)) (nth row i) (nth row (inc i)))))
          cells (range 1 (dec (count cells)))))
(defn synchronous-next [cells]
  (reduce (fn [next i] (assoc next i (rules (nth cells (dec i)) (nth cells i) (nth cells (inc i)))))
          cells (range 1 (dec (count cells)))))
(defn neighbor-sum [board x y]
  (reduce + (for [dy [-1 0 1] dx [-1 0 1] :when (not= [dx dy] [0 0])]
              (get-in board [(+ y dy) (+ x dx)] 0))))
(defn cell [state x y width]
  {:state state :previous state :x x :y y :width width})
(defn cell-scene [{:keys [state previous x y width]}]
  (let [color (cond (and (= previous 0) (= state 1)) [0 0 255]
                    (= state 1) [0 0 0]
                    (and (= previous 1) (= state 0)) [255 0 0]
                    :else [255 255 255])]
    [[:stroke 0] (into [:fill] color) [:rect x y width width]]))

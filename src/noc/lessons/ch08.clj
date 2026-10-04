(ns noc.lessons.ch08
  "有限递归、Cantor 集和显式海龟状态。"
  (:require [noc.math :as m] [noc.draw :as d]))

(defn factorial-loop [n]
  (reduce *' 1 (range 1 (inc n))))
(defn factorial-recursive [n]
  (if (<= n 1) 1 (*' n (factorial-recursive (dec n)))))
(defn cantor-length [x y length]
  (if (<= length 1)
    []
    (vec (concat [[:line x y (+ x length) y]]
                 (cantor-length x (+ y 20) (/ length 3))
                 (cantor-length (+ x (/ (* 2 length) 3)) (+ y 20) (/ length 3))))))
(defn branch-length [length angle]
  (let [next-length (* length 0.67)]
    (vec (concat [[:line 0 0 0 (- length)] [:translate 0 (- length)]]
                 (when (> next-length 2)
                   (concat [[:push] [:rotate angle]] (branch-length next-length angle) [[:pop]]
                           [[:push] [:rotate (- angle)]] (branch-length next-length angle) [[:pop]]))))))
(defn turtle [length angle]
  {:length length :angle angle :position [320 240] :direction (- (/ m/pi 2)) :stack [] :lines []})
(defn turtle-step [{:keys [length angle position direction stack] :as state} symbol]
  (case symbol
    \F (let [end (m/v+ position (m/v* (m/from-angle direction) length))]
         (-> state (assoc :position end) (update :lines conj [position end])))
    \G (assoc state :position (m/v+ position (m/v* (m/from-angle direction) length)))
    \+ (update state :direction + angle)
    \- (update state :direction - angle)
    \[ (update state :stack conj [position direction])
    \] (if (seq stack) (-> state (assoc :position (first (peek stack)) :direction (second (peek stack)))
                            (update :stack pop)) state)
    state))

(ns noc.lessons.ch00
  "第 0 章课堂代码使用的有限画布与可复现随机环境。"
  (:require [noc.math :as m] [noc.draw :as d]))

(def env {:width 640 :height 240 :step-size 1 :mouse [400 160]})

(defn walker
  ([] (walker env))
  ([{:keys [width height]}] {:x (/ width 2) :y (/ height 2)}))

(defn cardinal-step [walker]
  (let [[dx dy] (m/choose [[1 0] [-1 0] [0 1] [0 -1]])]
    (-> walker (update :x + dx) (update :y + dy))))

(defn show [{:keys [x y]}]
  [[:stroke 0] [:point x y]])

(defn histogram [counts {:keys [width height]}]
  (let [bar-width (/ width (count counts))]
    (d/scene [[:background 255] [:stroke 0] [:fill 127]]
             (mapv (fn [index n]
                     [:rect (* index bar-width) (- height n) (dec bar-width) n])
                   (range) counts))))

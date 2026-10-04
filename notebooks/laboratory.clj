;; # Clojure / REPL 实验台

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.laboratory
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 普通 Clojure 数据与函数构成模型。这里的计算与画布使用同一份 `.cljc` 文件。

(engine/defaults (registry/example "2.9"))

;; ## 使用固定随机种子复现实验

(let [result (engine/run "2.9" 240 {:seed 2024 :count 8 :G 0.5})]
  (mapv #(select-keys % [:position :velocity :mass]) (get-in result [:state :bodies])))

;; ## 编程模拟鼠标输入

(:position (:state
 (engine/run "1.10" 240
             {:input-fn (fn [frame]
                          {:mouse [(+ 320 (* 100 (Math/cos (* frame 0.02))))
                                   (+ 120 (* 70 (Math/sin (* frame 0.02))))]})})))

;; ## 比较参数

(clerk/table
 (for [standard-deviation [15 30 60 120]
       :let [state (:state (engine/run "0.4" 1000 {:sd standard-deviation}))
             xs (:samples state) mean (/ (reduce + xs) (count xs))]]
   {:标准差 standard-deviation :样本均值 mean
    :样本方差 (/ (reduce + (map #(let [d (- % mean)] (* d d)) xs)) (count xs))}))

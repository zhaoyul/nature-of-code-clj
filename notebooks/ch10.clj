;; # 第 10 章: 神经网络

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch10
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch10 :as ch]
            [noc.lessons.ch10 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 0))

^{:nextjournal.clerk/no-cache true}
(do
(let [rule #(if (pos? %) :positive :non-positive)]
  (mapv rule [-2 0 3]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 1))

^{:nextjournal.clerk/no-cache true}
(do
(let [inputs [0.5 0.2 1] desired 1 weights [0.1 -0.3 0.0]]
  {:before weights :learned (ch/perceptron-train weights inputs desired 0.01)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 2))

^{:nextjournal.clerk/no-cache true}
(do
(let [inputs [12 4] weights [0.5 -1]
      weighted-sum (reduce + (map * inputs weights))]
  {:weighted-sum weighted-sum :output (ch/sign weighted-sum)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 3))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(perceptron [n rate]
          {:weights (vec (repeatedly n #(m/random -1 1))) :rate rate})
        (feedforward [model inputs]
          (ch/sign (reduce + (map * (:weights model) inputs))))]
  (let [model (perceptron 3 0.01)]
    {:model model :prediction (feedforward model [50 -12 1])}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [weights [0.2 -0.3 0.4] inputs [50 -12 1]]
  (ch/perceptron-guess weights inputs))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [inputs [0.5 0.2 1] weights [0.2 -0.3 0.4] desired -1
      prediction (ch/perceptron-guess weights inputs)
      error (- desired prediction)
      rate 0.01]
  (mapv (fn [weight input] (+ weight (* rate error input))) weights inputs))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 6))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(perceptron [n rate]
          {:weights (vec (repeatedly n #(m/random -1 1))) :rate rate})
        (guess [model inputs] (ch/perceptron-guess (:weights model) inputs))
        (train [model inputs desired]
          (update model :weights ch/perceptron-train inputs desired (:rate model)))]
  (let [model (perceptron 3 0.01)]
    {:before (guess model [0.5 0.2 1])
     :trained (train model [0.5 0.2 1] -1)}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 7))

^{:nextjournal.clerk/no-cache true}
(do
(let [f (fn [x] (- (* 0.5 x) 1))]
  (mapv (fn [x] [x (f x)]) [-2 -1 0 1 2]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 8))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene
 [[:background 255] [:translate 320 180] [:scale 1 -1]
  [:stroke 39 52 69] [:line -200 -100 200 100]])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 9))

^{:nextjournal.clerk/no-cache true}
(do
(let [x (m/random -100 100) y (m/random -100 100)
      y-line (- (* 0.5 x) 1)]
  {:inputs [x y 1] :line-y y-line :label (if (> y y-line) 1 -1)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 10))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 11))

^{:nextjournal.clerk/no-cache true}
(do
(let [initial (ch/perceptron-setup {})
      trained (nth (iterate #(ch/perceptron-step % {:rate 0.01}) initial) 800)
      correct (count (filter #(= (:label %) (ch/perceptron-guess (:weights trained) (:inputs %)))
                             (:points trained)))]
  {:weights (:weights trained) :accuracy (/ correct (double (count (:points trained))))
   :drawing (notebook/scene (ch/perceptron-scene trained {}))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 12))

^{:nextjournal.clerk/no-cache true}
(do
(require '[noc.learning :as learning])
(select-keys (learning/brain 2 8 4) [:inputs :hidden :outputs])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 13))

^{:nextjournal.clerk/no-cache true}
(do
(let [network (learning/brain 4 8 4)]
  {:architecture (select-keys network [:inputs :hidden :outputs])
   :labels ["left" "right" "up" "down"]
   :outputs (learning/predict network [0.2 0.4 0.6 0.8])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [network (learning/brain 3 8 1)]
  {:architecture (select-keys network [:inputs :hidden :outputs])
   :prediction (learning/predict network [0.2 0.4 0.6])})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 15))

^{:nextjournal.clerk/no-cache true}
(do
(mapv (fn [{:keys [inputs label]}]
        {:inputs inputs :label (nth ch/gesture-labels label)}) ch/gesture-data)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 16))

^{:nextjournal.clerk/no-cache true}
(do
(let [classifier (learning/brain 2 8 4)]
  {:inputs (:inputs classifier) :outputs (:outputs classifier)
   :hidden (:hidden classifier) :labels ch/gesture-labels})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 17))

^{:nextjournal.clerk/no-cache true}
(do
(mapv (fn [{:keys [inputs label]}]
        {:inputs (ch/normalized-input inputs) :target (ch/one-hot label)})
      ch/gesture-data)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [initial (learning/brain 2 8 4)
      trained (nth (iterate #(ch/gesture-epoch % 0.6) initial) 200)]
  {:epochs 200 :initial-loss (ch/gesture-loss initial)
   :trained-loss (ch/gesture-loss trained)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 19))

^{:nextjournal.clerk/no-cache true}
(do
(let [net (nth (iterate #(ch/gesture-epoch % 0.6) (learning/brain 2 8 4)) 200)
      direction [1 0]
      probabilities (learning/predict net (ch/normalized-input direction))]
  {:probabilities probabilities :label (nth ch/gesture-labels (learning/best-index probabilities))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 20))

^{:nextjournal.clerk/no-cache true}
(do
(let [probabilities [0.9324 0.0612 0.0054 0.0010]]
  (vec (sort-by :confidence >
                (mapv (fn [label confidence] {:label label :confidence confidence})
                      ["right" "left" "down" "up"] probabilities))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [status :training]
  (notebook/scene
   (d/scene (d/background) (d/label (if (= status :training) "正在训练" "绘制一个方向") 20 40))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 22))

;; ## Clojure 实验 10.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "10.1")

(notebook/sketch "10.1")

(select-keys (:state (engine/run "10.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 23))

^{:nextjournal.clerk/no-cache true}
(do
(let [env {:rate 0.6 :epochs 200 :mouse [100 100] :pressed? false}
      initial (ch/gesture-setup {})
      trained (nth (iterate #(ch/gesture-step % env) initial) 100)
      pressed (ch/gesture-step trained (assoc env :pressed? true))
      dragged (ch/gesture-step pressed (assoc env :pressed? true :mouse [220 100]))
      classified (ch/gesture-step dragged (assoc env :mouse [220 100]))]
  {:status (:status classified) :loss (:loss classified)
   :drawing (notebook/scene (ch/gesture-scene classified env))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 24))

^{:nextjournal.clerk/no-cache true}
(do
(let [results [{:label :right :confidence 0.93} {:label :left :confidence 0.06}]
      state {:status :classifying}]
  (assoc state :status (:label (first (sort-by :confidence > results)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch10" 25))

;; ## Clojure 实验 10.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "10.2")

(notebook/sketch "10.2")

(select-keys (:state (engine/run "10.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "10.1")

;; ## 本章随附练习与补充演示

;; # 第 9 章: 进化计算

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch09
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch09 :as ch]
            [noc.lessons.ch09 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 0))

^{:nextjournal.clerk/no-cache true}
(do
(let [target "to be or not to be that is the question"]
  {:target target :length (count target)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 1))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(random-character [] (str (char (+ 32 (m/random-int 95)))))
        (dna [n] (vec (repeatedly n random-character)))]
  {:genes (dna 17) :alphabet-size 95})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 2))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(fitness [genes target]
          (/ (count (filter true? (map = genes (map str target))))
             (double (count target))))]
  (fitness (mapv str "to be or not to be") "to be or not to be"))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 3))

^{:nextjournal.clerk/no-cache true}
(do
(let [population [{:name :a :fitness 0.8} {:name :b :fitness 0.2}]
      mating-pool (vec (mapcat #(repeat (long (* 100 (:fitness %))) %) population))]
  {:pool-size (count mating-pool) :copies (frequencies (map :name mating-pool))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [population [{:name :a :fitness 0.8} {:name :b :fitness 0.2}]
      total (reduce + (map :fitness population))]
  (loop [index 0 remainder (m/random total)]
    (let [candidate (nth population index)]
      (if (or (= index (dec (count population))) (<= remainder (:fitness candidate)))
        candidate
        (recur (inc index) (- remainder (:fitness candidate)))))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [a (mapv str "aaaaaaaa") b (mapv str "bbbbbbbb")
      midpoint (m/random-int (count a))
      child (into (subvec a 0 midpoint) (subvec b midpoint))
      mutated (learning/mutate-genes child 0.1 ch/random-character)]
  {:midpoint midpoint :child child :mutated mutated})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 6))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 7))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 极少数情况下全部适应度为 0, 使用原种群作为回退交配池.
(let [population-size 60 mutation-rate 0.01
      initial (ch/phrase-setup {:population population-size})
      generation #(ch/phrase-generation % {:mutation mutation-rate})
      history (vec (take 21 (iterate generation initial)))]
  (clerk/table
   (mapv (fn [state]
           (let [best (apply max-key :fitness (:population state))]
             {:generation (:generation state) :phrase (apply str (:genes best))
              :fitness (:fitness best)})) history)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 8))

^{:nextjournal.clerk/no-cache true}
(do
{:mutation 0.01 :population 150}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 9))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(dna [n] (vec (repeatedly n #(m/v* (m/random-vector) 0.1))))]
  (dna 8))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 10))

^{:nextjournal.clerk/no-cache true}
(do
(let [genes (vec (repeat 8 [0.1 -0.1]))
      rocket (assoc (ch/rocket genes) :position [320 300])
      next-rocket (ch/rocket-update rocket 0 [320 40] false)]
  (select-keys next-rocket [:genes :position :velocity :record-distance]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 11))

;; ## Clojure 实验 9.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "9.1")

(notebook/sketch "9.1")

(select-keys (:state (engine/run "9.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 12))

^{:nextjournal.clerk/no-cache true}
(do
(let [env (assoc lesson/env :population 20 :lifespan 40 :mutation 0.01)
      initial (ch/rocket-setup env)
      final-state (nth (iterate #(ch/rockets-step % env true) initial) 85)]
  {:generation (:generation final-state)
   :best-distance (:best-distance final-state)
   :drawing (notebook/scene (ch/rockets-scene final-state true))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 13))

;; ## Clojure 实验 9.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "9.2")

(notebook/sketch "9.2")

(select-keys (:state (engine/run "9.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [initial (ch/flower-setup {})
      rated (nth (iterate #(ch/flower-step % {:mouse [40 140] :clicked? false
                                            :mutation 0.05}) initial) 20)
      offspring (ch/flower-step rated {:mouse [30 20] :clicked? true :mutation 0.05})]
  {:generation (:generation offspring)
   :parents-fitness (mapv :fitness (:population rated))
   :flowers (notebook/scene (ch/flower-scene offspring {}))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 15))

^{:nextjournal.clerk/no-cache true}
(do
(let [creature (ch/bloop [320 180] 0.5)]
  {:after-update (update creature :health - 0.2)
   :dead? (neg? (:health (assoc creature :health -1)))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 16))

^{:nextjournal.clerk/no-cache true}
(do
(let [parent [0.35]
      copied (vec parent)
      mutated (learning/mutate-genes copied 1.0 m/random)]
  {:parent parent :copy copied :mutated mutated})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 17))

^{:nextjournal.clerk/no-cache true}
(do
(let [gene 0.35]
  {:max-speed (m/map-range gene 0 1 15 0)
   :radius (m/map-range gene 0 1 2 25)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [parent (ch/bloop [320 180] 0.4)
      reproduce (fn [creature]
                  (when (< (m/random) 0.0005)
                    (ch/bloop (:position creature)
                              (if (< (m/random) 0.01) (m/random) (:gene creature)))))]
  {:parent parent :child (reproduce parent)
   :copy-example (ch/bloop (:position parent) (:gene parent))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 19))

;; ## Clojure 实验 9.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "9.4")

(notebook/sketch "9.4")

(select-keys (:state (engine/run "9.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 20))

^{:nextjournal.clerk/no-cache true}
(do
(let [env (assoc lesson/env :population 8 :reproduction 0.001 :mutation 0.01)
      initial (ch/ecosystem-setup env)
      world (nth (iterate #(ch/ecosystem-step % env) initial) 30)]
  {:births (:births world) :deaths (:deaths world)
   :creatures (count (:creatures world))
   :ecosystem (notebook/scene (ch/ecosystem-scene world env))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch09" 21))

;; ## Clojure 实验 9.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "9.5")

(notebook/sketch "9.5")

(select-keys (:state (engine/run "9.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## Clojure 实验 9.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "9.3")

(notebook/sketch "9.3")

(select-keys (:state (engine/run "9.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "9.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 9.6 · examples/09_ga/exercise_9_6_annotated_ga_shakespeare

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/09_ga/exercise_9_6_annotated_ga_shakespeare"))

(notebook/sketch (notebook/companion-id "examples/09_ga/exercise_9_6_annotated_ga_shakespeare"))

(:_frame (:state (engine/run (notebook/companion-id "examples/09_ga/exercise_9_6_annotated_ga_shakespeare") 12)))

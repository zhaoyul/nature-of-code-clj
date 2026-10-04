(ns noc.chapters.extras09
  "第 9 章：带说明的遗传短句练习。"
  (:require [noc.draw :as d] [noc.chapters.ch09 :as ga]))
(def examples
  [(assoc (first ga/examples)
          :id "exercise-9.6" :title "练习 9.6：完整标注的莎士比亚遗传算法"
          :original-title "Exercise 9.6: Annotated Shakespeare Genetic Algorithm"
          :directory "examples/09_ga/exercise_9_6_annotated_ga_shakespeare"
          :scene (fn [state env]
                   (d/scene (ga/phrase-scene state env)
                            (d/label "循环：评分 → 轮盘选择 → 单点交叉 → 随机突变" 12 352)))
          :explanation "官网提供的是完整标注的短句演化程序，与例 9.1 使用相同的遗传算法。本练习直接复用同一实现，并把四步循环显示在画布底部；数值实验、交叉和突变仍可在 notebook 中执行。")])

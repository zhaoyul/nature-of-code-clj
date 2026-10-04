(ns noc.registry
  (:require [noc.catalog :as catalog]
            [noc.chapters.ch00 :as ch00] [noc.chapters.ch01 :as ch01]
            [noc.chapters.ch02 :as ch02] [noc.chapters.ch03 :as ch03]
            [noc.chapters.ch04 :as ch04] [noc.chapters.ch05 :as ch05]
            [noc.chapters.ch06 :as ch06] [noc.chapters.ch07 :as ch07]
            [noc.chapters.ch08 :as ch08] [noc.chapters.ch09 :as ch09]
            [noc.chapters.ch10 :as ch10] [noc.chapters.ch11 :as ch11]
            [noc.chapters.extras00 :as ex00] [noc.chapters.extras01 :as ex01]
            [noc.chapters.extras02 :as ex02] [noc.chapters.extras03 :as ex03]
            [noc.chapters.extras04 :as ex04] [noc.chapters.extras05 :as ex05]
            [noc.chapters.extras06 :as ex06] [noc.chapters.extras07 :as ex07]
            [noc.chapters.extras08 :as ex08] [noc.chapters.extras09 :as ex09]))

(def chapters
  [{:chapter 0 :title "随机性"} {:chapter 1 :title "向量"}
   {:chapter 2 :title "力"} {:chapter 3 :title "振荡"}
   {:chapter 4 :title "粒子系统"} {:chapter 5 :title "自主智能体"}
   {:chapter 6 :title "物理库"} {:chapter 7 :title "元胞自动机"}
   {:chapter 8 :title "分形"} {:chapter 9 :title "进化计算"}
   {:chapter 10 :title "神经网络"} {:chapter 11 :title "神经进化"}])

(def original-examples (into {} (map (juxt :id identity) (:examples catalog/book))))
(def original-companions (into {} (map (juxt :directory identity) (:companions catalog/book))))
(defn enrich [spec]
  (let [ch (:chapter spec)
        chapter-source (str "noc/chapters/" (if (contains? original-examples (:id spec)) "ch" "extras")
                            (when (< ch 10) "0") ch ".cljc")]
    (merge (get original-examples (:id spec)) (get original-companions (:directory spec))
           {:source chapter-source} spec)))
(def numbered-examples
  (mapv enrich (concat ch00/examples ch01/examples ch02/examples ch03/examples
                       ch04/examples ch05/examples ch06/examples ch07/examples
                       ch08/examples ch09/examples ch10/examples ch11/examples)))
(def companion-examples
  (mapv enrich (concat ex00/examples ex01/examples ex02/examples ex03/examples
                       ex04/examples ex05/examples ex06/examples ex07/examples ex08/examples ex09/examples)))
(def examples (into numbered-examples companion-examples))
(def by-id (into {} (map (juxt :id identity) examples)))
(defn example [id]
  (or (get by-id (if (keyword? id) (name id) (str id)))
      (throw (ex-info "未知示例编号" {:id id :available (keys by-id)}))))

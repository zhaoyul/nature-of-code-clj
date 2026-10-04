;; # 全书示例目录

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.examples
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

(clerk/table
 (mapv (fn [example]
         {:编号 (:id example) :标题 (:title example)
          :章节 (:chapter example) :原始示例 (:original-title example)
          :源代码 (:source example)})
       registry/examples))

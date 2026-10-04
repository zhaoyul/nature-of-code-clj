;; # 代码本色 · Clojure / Clerk / Quil 中文实验书

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.index
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]))

;; 这是你已经完成的 2024 年版《The Nature of Code》中文译文与 Clojure 实验工程。
;; 译文覆盖 PDF 第 1–643 页，19 个完整部分和 385 张原配图。
;; 正文的 538 段程序均改写为可求值的 Clojure 单元；99 个编号示例与 67 个配套实验使用共享 `.cljc` 模型。
;; 原始 Org 与 Markdown 译稿另存为完整存档，中文正文、原术语、公式、配图及图注保留。
;; 每个实验都在当前 notebook 内启动独立 Quil 画布，并能在 JVM 上有限步求值。

;; ## 阅读目录

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/navigation [["front_matter.clj" "书前内容"] ["acknowledgments.clj" "致谢"] ["introduction.clj" "引言"] ["ch00.clj" "第 0 章: 随机性"] ["ch01.clj" "第 1 章: 向量"] ["ch02.clj" "第 2 章: 力"] ["ch03.clj" "第 3 章: 振荡"] ["ch04.clj" "第 4 章: 粒子系统"] ["ch05.clj" "第 5 章: 自主智能体"] ["ch06.clj" "第 6 章: 物理库"] ["ch07.clj" "第 7 章: 元胞自动机"] ["ch08.clj" "第 8 章: 分形"] ["ch09.clj" "第 9 章: 进化计算"] ["ch10.clj" "第 10 章: 神经网络"] ["ch11.clj" "第 11 章: 神经进化"] ["appendix.clj" "附录: 生物设计"] ["image_credits.clj" "图片来源"] ["book_index.clj" "索引"] ["back_cover.clj" "封底"]])


;; ## 操作与实验

;; 先尝试下面的随机游走。画布支持暂停、继续、单步和重置，离开屏幕时自动暂停。
;; 修改 `.cljc` 后在 nREPL 执行 `(user/reset)`，重编译浏览器程序并刷新 notebook。
^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["examples.clj" "完整示例目录"] ["laboratory.clj" "参数与 REPL 实验"]])

(notebook/sketch "0.1")

;; ## 内容完整性

{:sections (count book/sections)
 :original-sha256 (book/source-sha256)
 :numbered-examples (count registry/numbered-examples)
 :companion-examples (count registry/companion-examples)}

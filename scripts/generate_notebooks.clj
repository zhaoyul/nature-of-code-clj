(ns scripts.generate-notebooks
  (:require [clojure.edn :as edn] [clojure.java.io :as io] [clojure.string :as str]))

(defn write! [path text]
  (io/make-parents path)
  (spit path (str (str/trimr text) "\n")))
(def metadata-source (edn/read-string (slurp "resources/examples.edn")))
(write! "src/noc/catalog.cljc" (str "(ns noc.catalog)\n\n(def book\n  " (pr-str metadata-source) ")\n"))

(def sections (edn/read-string (slurp "resources/translations/the-nature-of-code/sections.edn")))
(defn notebook-id [id] (if (= id "index") "book-index" id))
(defn notebook-file [id] (str (str/replace (notebook-id id) "-" "_") ".clj"))
(defn header [id title & [chapter ported?]]
  (str ";; # " title "\n\n"
       "^{:nextjournal.clerk/visibility {:code :hide :result :hide}}\n"
       "(ns notebooks." id "\n"
       "  {:nextjournal.clerk/toc true}\n"
       "  (:require [nextjournal.clerk :as clerk]\n"
       "            [noc.book :as book] [noc.notebook :as notebook]\n"
       "            [noc.engine :as engine] [noc.registry :as registry]\n"
       "            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]"
       (when (some? chapter) (str "\n            [noc.chapters.ch" (format "%02d" chapter) " :as ch]"))
       (when ported? (str "\n            [noc.lessons." id " :as lesson]"))
       "))\n\n"))
(defn cell [code] (str "^{:nextjournal.clerk/visibility {:code :hide}}\n" code "\n\n"))
(defn navigation-cell [code]
  (str "^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}\n"
       code "\n\n"))
(defn experiment [id]
  (str ";; ## Clojure 实验 " id "\n\n"
       (cell (str "(notebook/explanation " (pr-str id) ")"))
       "(notebook/sketch " (pr-str id) ")\n\n"
       "(select-keys (:state (engine/run " (pr-str id) " 120))\n"
       "             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])\n\n"))

(defn ported-reading [id part-ids valid-ids]
  (let [layout (edn/read-string (slurp (str "resources/clojure-book/layout/" id ".edn")))
        snippets (edn/read-string (slurp (str "resources/clojure-book/" id ".edn")))]
    (assert (= (mapv :index snippets) (vec (range (:code-blocks layout)))) id)
    (apply str
           (for [group (partition-by :part (:segments layout))
                 :let [part (:part (first group)) example-id (nth part-ids part)]]
             (str
              (apply str
                     (for [{:keys [kind index comments]} group]
                       (if (= kind :markdown)
                         (navigation-cell (str "(clerk/md (book/prose-part " (pr-str id) " " index "))"))
                         (let [{:keys [source note]} (nth snippets index)]
                           (str (when note (cell (str "(clerk/md " (pr-str (str "Clojure 适配：" note)) ")")))
                                "^{:nextjournal.clerk/no-cache true}\n(do\n"
                                (apply str (map #(str "  ;; " % "\n") comments))
                                source "\n)\n\n")))))
              (when (valid-ids example-id) (experiment example-id)))))))

(doseq [{:keys [id title chapter]} sections]
  (let [numbered (filter #(= chapter (:chapter %)) (:examples metadata-source))
        numbered (if (some? chapter) numbered [])
        raw-markdown (slurp (str "resources/translations/the-nature-of-code/sections/" id ".md"))
        parts (str/split raw-markdown #"(?m)(?=^(?:#{1,6}\s+|\d+\.\s+)示例\s+\d+\.\d+)")
        part-ids (mapv #(second (re-find #"(?m)^(?:#{1,6}\s+|\d+\.\s+)示例\s+(\d+\.\d+)" %)) parts)
        valid-ids (set (map :id numbered))
        inserted-ids (set (filter valid-ids part-ids))
        ported? (.exists (io/file "resources/clojure-book" (str id ".edn")))
        body (str (header (notebook-id id) title chapter ported?)
                  (navigation-cell "(notebook/links [[\"index.clj\" \"返回目录\"]])")
                  ";; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。\n"
                  ";; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。\n\n"
                  (if ported?
                    (ported-reading id part-ids valid-ids)
                    (apply str
                           (for [[i example-id] (map-indexed vector part-ids)]
                             (str (navigation-cell (str "(clerk/md (book/part " (pr-str id) " " i "))"))
                                  (when (valid-ids example-id) (experiment example-id))))))
                  (when (seq numbered)
                    (str ";; # 本章 Clojure + Quil 可执行示例\n\n"
                         ";; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。\n"
                         ";; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。\n\n"
                         (apply str (for [{:keys [id]} numbered :when (not (inserted-ids id))]
                                      (experiment id)))
                         ";; ## 共享 Clojure 实现源码\n\n"
                         (cell (str "(notebook/source " (pr-str (:id (first numbered))) ")"))
                         ";; ## 本章随附练习与补充演示\n\n"
                         (apply str
                                (map-indexed
                                 (fn [i item]
                                   (let [extra-id (str "(notebook/companion-id " (pr-str (:directory item)) ")")]
                                     (str ";; ### " (:title item) " · " (:directory item) "\n\n"
                                          (cell (str "(notebook/explanation " extra-id ")"))
                                          "(notebook/sketch " extra-id ")\n\n"
                                          "(:_frame (:state (engine/run " extra-id " 12)))\n\n")))
                                 (filter #(= chapter (:chapter %)) (:companions metadata-source)))))))]
    (write! (str "notebooks/" (notebook-file id)) body)))

(write! "notebooks/index.clj"
        (str (header "index" "代码本色 · Clojure / Clerk / Quil 中文实验书")
             ";; 这是你已经完成的 2024 年版《The Nature of Code》中文译文与 Clojure 实验工程。\n"
             ";; 译文覆盖 PDF 第 1–643 页，19 个完整部分和 385 张原配图。\n"
             ";; 正文的 538 段程序均改写为可求值的 Clojure 单元；99 个编号示例与 67 个配套实验使用共享 `.cljc` 模型。\n"
             ";; 原始 Org 与 Markdown 译稿另存为完整存档，中文正文、原术语、公式、配图及图注保留。\n"
             ";; 每个实验都在当前 notebook 内启动独立 Quil 画布，并能在 JVM 上有限步求值。\n\n"
             ";; ## 阅读目录\n\n"
             (navigation-cell (str "(notebook/navigation "
                                   (pr-str (mapv (fn [{:keys [id title]}] [(notebook-file id) title]) sections))
                                   ")"))
             "\n;; ## 操作与实验\n\n"
             ";; 先尝试下面的随机游走。画布支持暂停、继续、单步和重置，离开屏幕时自动暂停。\n"
             ";; 修改 `.cljc` 后在 nREPL 执行 `(user/reset)`，重编译浏览器程序并刷新 notebook。\n"
             (navigation-cell "(notebook/links [[\"examples.clj\" \"完整示例目录\"] [\"laboratory.clj\" \"参数与 REPL 实验\"]])")
             "(notebook/sketch \"0.1\")\n\n"
             ";; ## 内容完整性\n\n"
             "{:sections (count book/sections)\n :original-sha256 (book/source-sha256)\n"
             " :numbered-examples (count registry/numbered-examples)\n"
             " :companion-examples (count registry/companion-examples)}\n"))

(write! "notebooks/examples.clj"
        (str (header "examples" "全书示例目录")
             (navigation-cell "(notebook/links [[\"index.clj\" \"返回目录\"]])")
             "(clerk/table\n (mapv (fn [example]\n         {:编号 (:id example) :标题 (:title example)\n          :章节 (:chapter example) :原始示例 (:original-title example)\n          :源代码 (:source example)})\n       registry/examples))\n"))

(write! "notebooks/laboratory.clj"
        (str (header "laboratory" "Clojure / REPL 实验台")
             (navigation-cell "(notebook/links [[\"index.clj\" \"返回目录\"]])")
             ";; 普通 Clojure 数据与函数构成模型。这里的计算与画布使用同一份 `.cljc` 文件。\n\n"
             "(engine/defaults (registry/example \"2.9\"))\n\n"
             ";; ## 使用固定随机种子复现实验\n\n"
             "(let [result (engine/run \"2.9\" 240 {:seed 2024 :count 8 :G 0.5})]\n"
             "  (mapv #(select-keys % [:position :velocity :mass]) (get-in result [:state :bodies])))\n\n"
             ";; ## 编程模拟鼠标输入\n\n"
             "(:position (:state\n (engine/run \"1.10\" 240\n             {:input-fn (fn [frame]\n                          {:mouse [(+ 320 (* 100 (Math/cos (* frame 0.02))))\n                                   (+ 120 (* 70 (Math/sin (* frame 0.02))))]})})))\n\n"
             ";; ## 比较参数\n\n"
             "(clerk/table\n (for [standard-deviation [15 30 60 120]\n       :let [state (:state (engine/run \"0.4\" 1000 {:sd standard-deviation}))\n             xs (:samples state) mean (/ (reduce + xs) (count xs))]]\n"
             "   {:标准差 standard-deviation :样本均值 mean\n"
             "    :样本方差 (/ (reduce + (map #(let [d (- % mean)] (* d d)) xs)) (count xs))}))\n"))
(println "Generated" (+ 3 (count sections)) "Clerk notebooks and browser catalogue.")

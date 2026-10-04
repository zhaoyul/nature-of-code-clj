(ns noc.book-rendering-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [nextjournal.markdown :as markdown]
            [noc.book :as book]))

(defn section-nodes [id]
  ;; Canonical chapter document, including the rewritten source blocks.
  (mapcat #(tree-seq (comp seq :content) :content
                     (markdown/parse (:markdown %)))
          (book/markdown-parts id)))

(defn prose-nodes [id]
  ;; The actual isolated Markdown cells evaluated by the generated notebook.
  (if (book/clojure-section? id)
    (mapcat (fn [{:keys [index]}]
              (tree-seq (comp seq :content) :content
                        (markdown/parse (book/prose-part id index))))
            (filter #(= :markdown (:kind %)) (:segments (book/clojure-layout id))))
    (section-nodes id)))

(defn node-text [node]
  (apply str (keep :text (tree-seq (comp seq :content) :content node))))

(deftest mathematical-blocks-preserve-book-structure
  (let [sections (mapv (fn [{:keys [id]}]
                         {:id id :nodes (vec (section-nodes id))})
                       book/sections)
        counts (frequencies (map :type (mapcat :nodes sections)))]
    (testing "Original prose formulas, code and images remain separate AST nodes"
      (is (= {:block-formula 89 :formula 244 :code 550 :image 387}
             (select-keys counts [:block-formula :formula :code :image]))))
    (doseq [{:keys [id nodes]} sections]
      (let [formulas (filter #(#{:formula :block-formula} (:type %)) nodes)]
        (is (every? #(and (not (str/blank? (:text %)))
                          (not (re-find #"let |return |function |!\[|data:image/|\n#{1,6} "
                                        (:text %))))
                    formulas)
            (str id ": no code, image URLs or headings swallowed by math"))))))

(deftest dot-product-and-projection-render-independently
  (let [nodes (vec (section-nodes "ch05"))
        code (map node-text (filter #(= :code (:type %)) nodes))
        paragraphs (map node-text (filter #(= :paragraph (:type %)) nodes))]
    (is (= 8 (count (filter #(= :block-formula (:type %)) nodes))))
    (is (= 59 (count (filter #(= :image (:type %)) nodes))))
    (is (some #(and (str/includes? % "[-3 5]") (str/includes? % "(m/dot a b)")) code))
    (is (some #(str/includes? % "normal-point (m/v+") code))
    (is (some #(str/includes? % "则点积定义为") paragraphs))
    (is (some #(str/includes? % "点积的输入是两个向量, 输出却是标量") paragraphs))))

(deftest inline-code-and-degree-formulas-retain-their-meaning
  (doseq [[id expected] [["ch00" ["3 === '3'" "3 == '3'"]]
                         ["ch01" ["x" "y" "z" "xspeed" "yspeed" "zspeed"]]
                         ["ch02" ["position1" "position2" "mass1" "mass2" "i === j"]]
                         ["ch03" ["PI" "TWO_PI" "this.angle" "this.angleVelocity"]]
                         ["ch05" ["this === other" "this !== other"]]
                         ["ch11" ["frameCount % 100 === 0"]]]]
    (let [inline-code (set (map node-text (filter #(= :monospace (:type %))
                                                  (section-nodes id))))]
      (is (every? inline-code expected) (str id ": original inline code retained"))))
  (let [formulas (set (map :text (filter #(= :formula (:type %))
                                         (section-nodes "ch03"))))]
    (is (every? formulas ["180^\\circ" "0" "\\pi" "2\\pi"]))))

(deftest original-chinese-captions-are-visible-prose
  (let [captions (map second (re-seq #"(?m)^#\+CAPTION:\s*(.+)$" (book/original-org)))
        ;; Ignore markup punctuation; retain every Chinese/Latin word, number
        ;; and TeX command. Image alt text does not count as a visible caption.
        readable #(str/replace % #"[^\p{L}\p{N}]" "")
        expected (frequencies (map readable captions))
        paragraphs (mapcat (fn [{:keys [id]}]
                             (map node-text (filter #(= :paragraph (:type %))
                                                    (prose-nodes id))))
                           book/sections)
        actual (frequencies (map readable paragraphs))]
    (is (= 387 (count captions)))
    (is (every? (fn [[text n]] (>= (get actual text 0) n)) expected)
        "All original captions appear as visible paragraphs, including repeated image uses")))

(deftest isolated-notebook-prose-preserves-formulas-and-images
  (let [counts (frequencies (map :type (mapcat (comp prose-nodes :id) book/sections)))]
    (is (= {:block-formula 89 :formula 244 :image 387}
           (select-keys counts [:block-formula :formula :image]))))
  (doseq [{:keys [id]} book/sections :when (book/clojure-section? id)]
    (is (zero? (count (filter #(= :code (:type %)) (prose-nodes id))))
        (str id ": a list continuation must remain readable prose after a code cell"))))

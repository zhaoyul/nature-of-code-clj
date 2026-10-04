(ns noc.lesson-test
  "全文课堂代码的覆盖、独立求值和 Clerk 正文边界回归。"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [nextjournal.clerk :as clerk]
            [nextjournal.markdown :as markdown]
            [noc.book :as book]
            [noc.draw :as d]
            [noc.engine :as engine]
            [noc.learning :as learning]
            [noc.math :as m]
            [noc.notebook :as notebook]
            [noc.registry :as registry]))

(def expected-sections
  [["introduction" 10]
   ["ch00" 32] ["ch01" 57] ["ch02" 55] ["ch03" 47]
   ["ch04" 56] ["ch05" 82] ["ch06" 65] ["ch07" 21]
   ["ch08" 35] ["ch09" 17] ["ch10" 23] ["ch11" 38]])

(defn entries [id]
  (let [resource (io/resource (str "clojure-book/" id ".edn"))]
    (when-not resource
      (throw (ex-info "课堂代码资源缺失" {:section id})))
    (edn/read-string (slurp resource))))

(def shared-aliases
  {'m 'noc.math 'd 'noc.draw 'learning 'noc.learning
   'engine 'noc.engine 'registry 'noc.registry
   'notebook 'noc.notebook 'clerk 'nextjournal.clerk})

(defn evaluate-entry
  "Evaluate with real project namespaces in a fresh, disposable notebook namespace.
  A preceding code cell cannot accidentally supply a missing definition."
  [id {:keys [index source]}]
  (let [name (symbol (str "noc.lesson-evaluation." id "-" index "-" (gensym)))
        temporary-ns (create-ns name)
        chapter-id (if (= id "introduction") "ch00" id)
        aliases (assoc shared-aliases
                       'ch (symbol (str "noc.chapters." chapter-id))
                       'lesson (symbol (str "noc.lessons." id)))]
    (try
      (binding [*ns* temporary-ns
                m/*rng* (m/seeded 42)]
        (clojure.core/refer 'clojure.core)
        (doseq [[short-name namespace-name] aliases]
          (require namespace-name)
          (alias short-name namespace-name))
        (eval (read-string (str "(do\n" source "\n)"))))
      (finally (remove-ns name)))))

(defn result-properties
  "Bound traversal of lazy collections; viewer functions are ordinary leaves.
  Detect an accidental infinite result and nonfinite simulation coordinates."
  [value]
  (let [budget 100000
        nodes (vec (take (inc budget) (tree-seq coll? seq value)))]
    {:bounded? (<= (count nodes) budget)
     :finite-numbers? (every? #(or (not (number? %)) (Double/isFinite (double %)))
                              nodes)}))

(deftest every-original-code-block-has-a-physical-clojure-cell
  (is (= 538 (reduce + (map second expected-sections))))
  (doseq [[id expected-count] expected-sections]
    (testing id
      (let [snippets (entries id)
            layout (book/clojure-layout id)
            notebook-source (slurp (io/resource (str id ".clj")))
            indices (vec (range expected-count))]
        (is (= expected-count (count snippets)))
        (is (= indices (mapv :index snippets)))
        (is (= expected-count (:code-blocks layout)))
        (is (= indices (mapv :index (filter #(= :clojure (:kind %))
                                            (:segments layout)))))
        (doseq [{:keys [index source]} snippets]
          (is (and (string? source) (not (str/blank? source)))
              (str id "/" index ": nonempty Clojure source"))
          (is (str/includes? notebook-source source)
              (str id "/" index ": source is editable code physically embedded in the notebook")))))))

(deftest all-538-cells-evaluate-independently-with-real-clerk-viewers
  (doseq [[id _] expected-sections
          entry (entries id)]
    (testing (str id "/" (:index entry))
      (try
        (let [{:keys [bounded? finite-numbers?]} (result-properties (evaluate-entry id entry))]
          (is bounded? "The cell returns a finite, inspectable value")
          (is finite-numbers? "Simulation results contain no NaN or infinite numbers"))
        (catch Throwable error
          (is false (str id "/" (:index entry) ": "
                         (.getName (class error)) ": " (.getMessage error))))))))

(defn node-text [node]
  (apply str (keep :text (tree-seq (comp seq :content) :content node))))

(deftest markdown-preserves-each-clojure-block-as-code
  (doseq [[id expected-count] expected-sections]
    (testing id
      (let [document (markdown/parse (book/markdown id))
            nodes (tree-seq (comp seq :content) :content document)
            code-nodes (filter #(= :code (:type %)) nodes)
            code-texts (set (map (comp str/trimr node-text) code-nodes))]
        (is (= expected-count (count code-nodes))
            "Math and prose cannot swallow a Clojure code cell")
        (is (every? #(= "clojure" (:info %)) code-nodes)
            "Every translated teaching block is explicitly Clojure code")
        (doseq [{:keys [index]} (entries id)]
          (is (contains? code-texts (str/trimr (book/snippet-source id index)))
              (str id "/" index ": full source and original Chinese comments remain code")))))))

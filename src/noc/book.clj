(ns noc.book
  "The user's complete, previously translated Chinese book, preserved verbatim."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.util Base64]
           [java.security MessageDigest]))

(def resource-root "translations/the-nature-of-code/")
(def sections (edn/read-string (slurp (io/resource (str resource-root "sections.edn")))))
(def numbered-examples (edn/read-string (slurp (io/resource (str resource-root "examples.edn")))))

(defn original-org [] (slurp (io/resource (str resource-root "book.zh.org"))))
(defn section [id] (some #(when (= id (:id %)) %) sections))
(defn example-section [id] (some #(when (= id (:id %)) %) numbered-examples))

(defn clojure-section? [id]
  (boolean (io/resource (str "clojure-book/" id ".edn"))))

(def clojure-layout
  (memoize
   (fn [id]
     (edn/read-string (slurp (io/resource (str "clojure-book/layout/" id ".edn")))))))

(def clojure-snippets
  (memoize
   (fn [id]
     (let [snippets (edn/read-string (slurp (io/resource (str "clojure-book/" id ".edn"))))]
       (when-not (= (mapv :index snippets) (vec (range (:code-blocks (clojure-layout id)))))
         (throw (ex-info "Clojure 正文片段覆盖不完整" {:section id})))
       snippets))))

(defn standalone-prose
  "A cell beginning inside an original list has no preceding list container.
  Dedent only that initial continuation, stopping at the next outer block."
  [text]
  (let [lines (str/split text #"\n" -1)
        first-line (first (remove str/blank? lines))
        indentation (count (or (re-find #"^ +" (or first-line "")) ""))]
    (if (< indentation 4)
      text
      (let [[continuation rest-lines]
            (split-with #(or (str/blank? %)
                             (str/starts-with? % (apply str (repeat indentation \space))))
                        lines)]
        (str/join "\n" (concat (map #(if (str/blank? %) "" (subs % indentation))
                                     continuation)
                              rest-lines))))))

(defn prose [id index]
  (standalone-prose
   (:text (some #(when (and (= :markdown (:kind %)) (= index (:index %))) %)
                (:segments (clojure-layout id))))))

(defn snippet-source [id index]
  (let [snippet (nth (clojure-snippets id) index)
        original (some #(when (and (= :clojure (:kind %)) (= index (:index %))) %)
                       (:segments (clojure-layout id)))]
    (str (apply str (map #(str ";; " % "\n") (:comments original)))
         (:source snippet))))

(defn snippet-note [id index] (:note (nth (clojure-snippets id) index)))

(def image-data-url
  (memoize
   (fn [path]
     (let [resource (io/resource (str resource-root "images/" (last (str/split path #"/"))))
           mime (cond (str/ends-with? path ".png") "image/png"
                      (or (str/ends-with? path ".jpg") (str/ends-with? path ".jpeg")) "image/jpeg"
                      (str/ends-with? path ".svg") "image/svg+xml"
                      :else "application/octet-stream")]
       (when-not resource (throw (ex-info "原译稿图片缺失" {:path path})))
       (with-open [in (io/input-stream resource)]
         (str "data:" mime ";base64," (.encodeToString (Base64/getEncoder) (.readAllBytes in))))))))

(defn markdown
  "Complete GFM section text, including pipe tables and embedded local images for Clerk."
  [id]
  (let [record (section id)]
    (when-not record (throw (ex-info "未知书籍章节" {:id id})))
    (-> (if (clojure-section? id)
          (apply str
                 (map (fn [{:keys [kind text index]}]
                        (if (= kind :markdown) (standalone-prose text)
                            (str "\n\n```clojure\n" (snippet-source id index) "\n```\n\n")))
                      (:segments (clojure-layout id))))
          (slurp (io/resource (str resource-root (:path record)))))
        (str/replace #"\.\./images/[A-Za-z0-9_.-]+" image-data-url))))

(defn prose-part [id index]
  (str/replace (prose id index) #"\.\./images/[A-Za-z0-9_.-]+" image-data-url))

(def text markdown)

(def markdown-parts
  "Split only at original example headings; joining the parts preserves all prose."
  (memoize
   (fn [id]
     (let [text (markdown id)
           parts (str/split text #"(?m)(?=^(?:#{1,6}\s+|\d+\.\s+)示例\s+\d+\.\d+)")]
       (mapv (fn [part]
               {:example-id (second (re-find #"(?m)^(?:#{1,6}\s+|\d+\.\s+)示例\s+(\d+\.\d+)" part))
                :markdown part}) parts)))))

(defn part [id index] (:markdown (nth (markdown-parts id) index)))

(defn source-sha256 []
  (with-open [in (io/input-stream (io/resource (str resource-root "book.zh.org")))]
    (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.readAllBytes in))]
      (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

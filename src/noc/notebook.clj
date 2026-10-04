(ns noc.notebook
  "Clerk presentation helpers; all simulations remain ordinary JVM functions."
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.repl :as repl]
            [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.paths :as paths]
            [nextjournal.clerk.viewer :as viewer]
            [noc.engine :as engine]
            [noc.registry :as registry]))

(def ^:dynamic *asset-base* "/js/")

(defn doc-url
  "Resolve an internal notebook link for both live Clerk and static packages."
  [filename]
  (let [path (str "notebooks/" filename)
        opts paths/*build-opts*
        route (if (:package opts)
                (-> (viewer/map-index opts path)
                    (str/replace #"(^|.*/)(index\.(clj|cljc|md))$" "$1")
                    (str/replace #"\.(clj|cljc|md)$" ""))
                path)]
    (clerk/doc-url route)))

(defn link [filename label]
  (str "[" label "](" (doc-url filename) ")"))

(defn links [entries]
  (clerk/md (str/join " · " (map #(apply link %) entries))))

(defn navigation [entries]
  (clerk/md (str/join "\n" (map #(str "- " (apply link %)) entries))))

(def sketch-viewer
  {:name ::sketch
   :transform-fn viewer/mark-presented
   :render-fn
   '(fn [{:keys [id title width height asset-base config-json]}]
      [:iframe {:title title
                :src (str asset-base "sketch.html?id=" (js/encodeURIComponent id)
                          "&config=" (js/encodeURIComponent config-json))
                :loading "lazy"
                :style {:width "100%" :max-width (str (+ width 26) "px")
                        :height (str (+ height 196) "px") :border "1px solid #d8ded9"
                        :border-radius "12px" :background "#fcfdf9"}
                :allow "fullscreen"}])})

(defn sketch
  "Embed an independent Quil canvas with pause, single-step, reset and parameters."
  ([id] (sketch id {}))
  ([id overrides]
  (let [{:keys [id title width height] :as spec} (registry/example id)]
    (when-not spec (throw (ex-info "未知示例" {:id id})))
    (clerk/with-viewer sketch-viewer
      {:nextjournal.clerk/width :wide}
      {:id id :title title :width (or width 640) :height (or height 360)
       :asset-base *asset-base* :config-json (json/write-str overrides)}))))

(def scene-viewer
  {:name ::scene
   :transform-fn viewer/mark-presented
   :render-fn
   '(fn [{:keys [commands-json width height asset-base]}]
      [:iframe {:title "Clojure 表达式的 Quil 绘图"
                :src (str asset-base "sketch.html#scene="
                          (js/encodeURIComponent commands-json)
                          "&width=" width "&height=" height)
                :loading "lazy"
                :style {:width "100%" :max-width (str (+ width 26) "px")
                        :height (str (+ height 70) "px") :border "1px solid #d8ded9"
                        :border-radius "8px"}}])})

(defn scene
  "Render a finite sequence of Clojure drawing commands with real Quil."
  ([commands] (scene commands {}))
  ([commands {:keys [width height] :or {width 640 height 360}}]
   (let [commands (vec commands)]
     (when-not (every? #(and (vector? %) (keyword? (first %))) commands)
       (throw (ex-info "绘图单元需要带关键字操作符的命令向量" {})))
     (clerk/with-viewer scene-viewer
       {:commands-json (json/write-str commands) :width width :height height
        :asset-base *asset-base*}))))

(defn defaults [spec]
  (engine/defaults spec))

(defn run
  "Evaluate a finite simulation on the JVM, with exactly the browser's model.
  Returns state, drawing commands and configuration for inspection."
  ([id frames] (run id frames {}))
  ([id frames overrides]
   (engine/run id frames overrides)))

(defn explanation [id]
  (let [spec (registry/example id)]
    (clerk/md (or (:explanation spec) (:title spec)))))

(defn companion-id
  "Resolve an official companion directory to its registered executable example."
  [directory]
  (or (some #(when (= directory (:directory %)) (:id %)) registry/companion-examples)
      (throw (ex-info "未知随附示例目录" {:directory directory}))))

(defn source
  "Show the authoritative implementation used on both the JVM and in the canvas."
  [id]
  (let [spec (registry/example id)
        source-file (:source spec)]
    (cond
      (string? source-file) (clerk/code (slurp (or (io/resource source-file) source-file)))
      (:source-code spec) (clerk/code (:source-code spec))
      :else (clerk/code
             (with-out-str
               (doseq [f [:setup :step :scene]
                       :let [sym (get (:source-vars spec) f)]
                       :when sym]
                 (println (repl/source-fn sym))))))))

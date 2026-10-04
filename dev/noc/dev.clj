(ns noc.dev
  "Development server and portable HTML export."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cljs.build.api :as cljs]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.webserver :as webserver]))

(defonce server (atom nil))

(defn compile-browser! []
  (println "正在编译 Quil 浏览器程序……")
  (cljs/build "src" (read-string (slurp "cljs.edn")))
  (println "浏览器程序已生成：public/js/noc.js"))

(defn notebook-paths []
  (->> (file-seq (io/file "notebooks"))
       (filter #(.isFile %))
       (map str)
       (filter #(str/ends-with? % ".clj"))
       sort vec))

(defn start!
  ([] (start! {}))
  ([{:keys [port nrepl-port compile? browse?]
     :or {port 7779 nrepl-port 7878 compile? true browse? false}}]
   (when-not @server
     (require 'nrepl.server)
     (reset! server ((resolve 'nrepl.server/start-server)
                     :bind "127.0.0.1" :port nrepl-port))
     (spit ".nrepl-port" nrepl-port))
   (println (str "nREPL 已启动：" nrepl-port))
   (when (or compile? (not (.exists (io/file "public/js/noc.js"))))
     (compile-browser!))
   (clerk/serve! {:host "localhost" :port port :browse browse?
                 :watch-paths ["notebooks" "src"]
                 :show-filter-fn #(str/starts-with? % "notebooks")})
   (clerk/show! "notebooks/index.clj")
   (println (str "中文 notebook：http://localhost:" port))
   (println (str "nREPL：clj-nrepl-eval -p " nrepl-port " \"(user/reset)\""))
   {:clerk-port port :nrepl-port nrepl-port}))

(defn reload!
  "Reload the pure models and rebuild the browser bundle after an edit."
  []
  (require 'noc.registry :reload-all)
  (compile-browser!)
  (clerk/show! "notebooks/index.clj"))

(defn copy-tree! [from to]
  (let [root (.toPath (io/file from))]
    (doseq [file (file-seq (io/file from)) :when (.isFile file)]
      (let [relative (.toString (.relativize root (.toPath file)))
            target (io/file to relative)]
        (io/make-parents target)
        (io/copy file target)))))

(defn build!
  ([] (build! {}))
  ([{:keys [out-path compile?] :or {out-path "public/build" compile? true}}]
   (when (or compile? (not (.exists (io/file "public/js/noc.js"))))
     (compile-browser!))
   (require 'noc.notebook)
   (with-bindings {(resolve 'noc.notebook/*asset-base*) "./js/"}
     (clerk/build! {:paths (notebook-paths) :index "notebooks/index.clj"
                   :package :single-file :out-path out-path}))
   (copy-tree! "public/js" (str out-path "/js"))
   (println (str "已导出 " out-path "/index.html；请用 HTTP 静态服务器打开。"))))

(defn -main [& args]
  (try
    (start! {:compile? (not (some #{"--skip-compile"} args))
             :browse? (boolean (some #{"--browse"} args))})
    (catch Throwable e
      (binding [*out* *err*]
        (println "启动未完成，nREPL 保持运行；修正错误后执行 (noc.dev/start!)。")
        (.printStackTrace e))))
  @(promise))

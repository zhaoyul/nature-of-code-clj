(ns noc.browser
  "Actual Quil/p5 rendering. Each notebook iframe owns exactly one sketch."
  (:require [clojure.string :as str]
            [noc.engine :as engine]
            [noc.registry :as registry]
            [quil.core :as q :include-macros true]))

(defonce runtime (atom nil))
(defonce images (atom {}))
(defonce asset-status (atom {}))
(defonce audio-context (atom nil))
(defonce played-tones (atom #{}))

(defn element [id] (.getElementById js/document id))
(defn text! [id text] (set! (.-textContent (element id)) (str text)))

(defn defaults [spec]
  (engine/defaults spec))

(defn visible? []
  (and (not (.-hidden js/document))
       (if-let [frame (.-frameElement js/window)]
         (let [rect (.getBoundingClientRect frame)
               h (.-innerHeight (.-parent js/window))]
           (and (pos? (.-bottom rect)) (< (.-top rect) h)))
         true)))

(defn sound! [frequency duration id]
  (when (and @audio-context (not (contains? @played-tones id)))
    (swap! played-tones conj id)
    (let [context @audio-context
          oscillator (.createOscillator context)
          gain (.createGain context)
          now (.-currentTime context)
          duration (max 0.02 (or duration 0.2))]
      (set! (.-value (.-frequency oscillator)) frequency)
      (.setValueAtTime (.-gain gain) 0.025 now)
      (.exponentialRampToValueAtTime (.-gain gain) 0.0001 (+ now duration))
      (.connect oscillator gain)
      (.connect gain (.-destination context))
      (.start oscillator now)
      (.stop oscillator (+ now duration)))))

(defn primitive! [[op & args]]
  (case op
    :background (apply q/background args)
    :fill (apply q/fill args)
    :stroke (apply q/stroke args)
    :no-fill (q/no-fill)
    :no-stroke (q/no-stroke)
    :stroke-weight (apply q/stroke-weight args)
    :ellipse (apply q/ellipse args)
    :circle (let [[x y d] args] (q/ellipse x y d d))
    :line (apply q/line args)
    :rect (apply q/rect args)
    :point (apply q/point args)
    :triangle (apply q/triangle args)
    :quad (apply q/quad args)
    :arc (apply q/arc args)
    :bezier (apply q/bezier args)
    :text (apply q/text args)
    :text-size (apply q/text-size args)
    :text-align (apply q/text-align args)
    :rect-mode (apply q/rect-mode args)
    :ellipse-mode (apply q/ellipse-mode args)
    :blend-mode (apply q/blend-mode args)
    :image-mode (apply q/image-mode args)
    :tint (apply q/tint args)
    :no-tint (q/no-tint)
    :image (let [[path & coordinates] args
                 image (get @images path)]
             (when (and image (= :loaded (get @asset-status path)) (q/loaded? image))
               (apply q/image image coordinates)))
    :tone (apply sound! args)
    :push (do (q/push-matrix) (q/push-style))
    :pop (do (q/pop-style) (q/pop-matrix))
    :translate (apply q/translate args)
    :rotate (apply q/rotate args)
    :scale (apply q/scale args)
    :polyline (let [[points closed?] args]
                (q/begin-shape)
                (doseq [[x y] points] (q/vertex x y))
                (if closed? (q/end-shape :close) (q/end-shape)))
    :polygon (let [[points] args]
               (q/begin-shape)
               (doseq [[x y] points] (q/vertex x y))
               (q/end-shape :close))
    (throw (ex-info "未知绘图命令" {:command op :args args}))))

(defn reset-model! []
  (let [{:keys [spec config state frame clicked? running?]} @runtime]
    (reset! state (engine/setup spec @config))
    (reset! frame 0)
    (reset! clicked? false)
    (reset! played-tones #{})
    (text! "status" (str "第 0 步 · " (if @running? "运行中" "已暂停")))
    (text! "error" "")))

(defn mount-parameters! [spec config]
  (let [container (element "parameters")]
    (set! (.-textContent container) "")
    (doseq [[k {:keys [label default min max step]}] (sort-by (comp str key) (:params spec))]
      (let [wrapper (.createElement js/document "label")
            input (.createElement js/document "input")
            output (.createElement js/document "output")]
        (.appendChild wrapper (.createTextNode js/document (or label (name k))))
        (set! (.-type input) "range")
        (set! (.-min input) (str (or min 0)))
        (set! (.-max input) (str (or max 10)))
        (set! (.-step input) (str (or step 0.1)))
        (set! (.-value input) (str (get @config k default)))
        (set! (.-textContent output) (str (get @config k default)))
        (set! (.-oninput input)
              (fn [_]
                (let [value (js/parseFloat (.-value input))]
                  (swap! config assoc k value)
                  (set! (.-textContent output) (str value)))))
        (.appendChild wrapper input)
        (.appendChild wrapper output)
        (.appendChild container wrapper)))))

(defn stop! []
  (when-let [{:keys [sketch timer]} @runtime]
    (when timer (js/clearInterval timer))
    (when sketch (.remove sketch))
    (when @audio-context (.close @audio-context) (reset! audio-context nil))
    (reset! runtime nil)))

(defn mount! [id overrides]
  (stop!)
  (text! "pause" "暂停")
  (text! "error" "")
  (doseq [id ["toolbar" "parameters" "help"]]
    (set! (.. (element id) -style -display) ""))
  (reset! images {})
  (reset! asset-status {})
  (let [spec (registry/example id)]
    (when-not spec (throw (ex-info "示例不存在" {:id id})))
    (let [config (atom (merge (defaults spec) overrides))
          state (atom (engine/setup spec @config))
          frame (atom 0)
          running? (atom true)
          step? (atom false)
          clicked? (atom false)
          drawn-frame (atom -1)
          key (atom nil)
          draw (fn []
                 (when (visible?)
                   (try
                     (let [env (merge @config {:frame @frame
                                              :mouse [(q/mouse-x) (q/mouse-y)]
                                              :pressed? (q/mouse-pressed?)
                                              :clicked? @clicked? :key @key})]
                       (when (or @running? @step?)
                         (swap! state #(engine/step spec % env))
                         (swap! frame inc)
                         (reset! step? false)
                         (reset! clicked? false))
                       (q/reset-matrix)
                       (q/no-fill)
                       (q/stroke 42 64 49)
                       (q/stroke-weight 1)
                       (q/text-size 13)
                       (doseq [command (engine/scene spec @state env)]
                         (primitive! command))
                       (reset! drawn-frame @frame)
                       (when (or (not @running?) (zero? (mod @frame 15)))
                         (text! "status" (str "第 " @frame " 步 · " (if @running? "运行中" "已暂停")))))
                     (catch :default e
                       (reset! running? false)
                       (text! "error" (str (.-message e)))
                       (.error js/console e)))))
          sketch (q/sketch :host "canvas"
                           :size [(:width @config) (:height @config)]
                           :setup #(do (q/frame-rate 30)
                                       (q/background 247 249 239)
                                       (reset! asset-status (zipmap (:assets spec) (repeat :loading)))
                                       (reset! images (into {} (map (fn [path]
                                                                     [path (.loadImage (q/current-graphics) path
                                                                                      (fn [_] (swap! asset-status assoc path :loaded))
                                                                                      (fn [_] (swap! asset-status assoc path :error)))])
                                                                    (:assets spec)))))
                           :draw draw
                           :mouse-pressed #(do (reset! clicked? true)
                                               (when-let [canvas (.querySelector (element "canvas") "canvas")]
                                                 (.focus canvas))
                                               (when (and (nil? @audio-context) (.-AudioContext js/window))
                                                 (reset! audio-context (js/AudioContext.)))
                                               (when @audio-context (.resume @audio-context)))
                           :key-pressed #(reset! key (q/key-as-keyword))
                           :key-released #(reset! key nil))
          timer (js/setInterval #(if (visible?) (.loop sketch) (.noLoop sketch)) 350)]
      (reset! runtime {:spec spec :config config :state state :frame frame
                       :running? running? :clicked? clicked? :drawn-frame drawn-frame :sketch sketch :timer timer})
      (when-let [canvas (.querySelector (element "canvas") "canvas")]
        (set! (.-tabIndex canvas) 0)
        (.addEventListener canvas "keydown" (fn [event]
                                             (when (contains? #{"ArrowLeft" "ArrowRight" "ArrowUp" "ArrowDown" " "} (.-key event))
                                               (.preventDefault event)))))
      (text! "title" (str (:id spec) " · " (:title spec)))
      (text! "status" "第 0 步 · 运行中")
      (set! (.-onclick (element "pause"))
            (fn [_]
              (swap! running? not)
              (text! "pause" (if @running? "暂停" "继续"))
              (text! "status" (str "第 " @frame " 步 · " (if @running? "运行中" "已暂停")))))
      (set! (.-onclick (element "step"))
            (fn [_]
              (reset! running? false)
              (reset! step? true)
              (text! "pause" "继续")))
      (set! (.-onclick (element "reset")) (fn [_] (reset-model!)))
      (mount-parameters! spec config)
      (set! (.-nocState js/window) #(clj->js {:id id :frame @frame :state @state :config @config}))
      (set! (.-nocStep js/window)
            (fn [n]
              (reset! running? false)
              (dotimes [_ n]
                (swap! state #(engine/step spec % (merge @config {:frame @frame :mouse [320 180]
                                                                 :pressed? false :clicked? false :key nil})))
                (swap! frame inc))
              (clj->js {:id id :frame @frame :state @state}))))))

(defn mount-scene! [commands width height]
  (stop!)
  (text! "error" "")
  (doseq [id ["toolbar" "parameters" "help"]]
    (set! (.. (element id) -style -display) "none"))
  (text! "title" "Clojure 表达式 · Quil 绘图")
  (let [commands (mapv (fn [[operation & arguments]]
                         (let [operation (keyword operation)]
                           (into [operation]
                                 (if (#{:rect-mode :ellipse-mode :blend-mode :image-mode :text-align} operation)
                                   (map #(if (string? %) (keyword %) %) arguments)
                                   arguments)))) commands)
        assets (vec (set (keep #(when (= :image (first %)) (second %)) commands)))
        sketch (q/sketch :host "canvas" :size [width height]
                         :setup #(do
                                   (q/background 250 249 246)
                                   (reset! asset-status (zipmap assets (repeat :loading)))
                                   (reset! images
                                           (into {} (map (fn [path]
                                                           [path (.loadImage
                                                                  (q/current-graphics) path
                                                                  (fn [_] (swap! asset-status assoc path :loaded))
                                                                  (fn [_]
                                                                    (swap! asset-status assoc path :error)
                                                                    (text! "error" (str "纹理加载失败：" path))))])
                                                         assets))))
                         :draw #(try
                                  (when-not (some #{:loading} (vals @asset-status))
                                    (doseq [command commands] (primitive! command))
                                    (q/no-loop))
                                  (catch :default error
                                    (text! "error" (.-message error))
                                    (q/no-loop))))]
    (reset! runtime {:sketch sketch})))

(defn ^:export init! []
  (try
    (let [params (js/URLSearchParams. (.-search js/location))
          scene-params (js/URLSearchParams. (subs (.-hash js/location) 1))
          id (or (.get params "id") (:id (first registry/examples)))
          overrides (if-let [encoded (.get params "config")]
                      (js->clj (js/JSON.parse encoded) :keywordize-keys true)
                      {})]
      (if-let [scene (.get scene-params "scene")]
        (mount-scene! (js->clj (js/JSON.parse scene))
                      (js/parseInt (or (.get scene-params "width") "640"))
                      (js/parseInt (or (.get scene-params "height") "360")))
        (mount! id overrides)))
    (catch :default e
      (text! "title" "示例加载失败")
      (text! "error" (str (.-message e)))
      (.error js/console e))))

(defn ^:export verify-all!
  "Browser integration check: run every model, then draw it with real Quil.
  Invoked by the dedicated local check page's visible button."
  []
  (let [input-cases
        [(assoc (registry/example "3.10") :check-label "spring-drag"
                :check-input (fn [frame]
                               (cond (zero? frame) {:mouse [320 110] :clicked? true :pressed? true}
                                     (< frame 10) {:mouse [400 240] :pressed? true}
                                     :else {})))
         (assoc (registry/example "4.4") :check-label "click-emitter"
                :check-input (fn [frame] (if (= frame 1) {:mouse [100 140] :clicked? true :pressed? true} {})))
         (assoc (registry/example "9.2") :check-label "click-target"
                :check-input (fn [frame] (if (= frame 1) {:mouse [200 100] :clicked? true :pressed? true} {})))
         (assoc (registry/example "9.4") :check-label "click-selection"
                :check-input (fn [frame] (if (= frame 19) {:mouse [80 30] :clicked? true :pressed? true} {})))]
        all-checks (concat registry/examples input-cases)
        report (element "check-results")
        summary (element "check-summary")
        queue (atom (seq all-checks))
        passed (atom 0)
        failures (atom [])
        finite? (fn finite? [x]
                  (cond (number? x) (js/Number.isFinite x)
                        (map? x) (every? finite? (vals x))
                        (coll? x) (every? finite? x)
                        :else true))]
    (set! (.-textContent report) "")
    (.setAttribute summary "data-status" "running")
    (letfn [(record! [spec error]
              (let [row (.createElement js/document "div")]
                (.setAttribute row "data-id" (:id spec))
                (.setAttribute row "data-case" (or (:check-label spec) "default"))
                (.setAttribute row "data-status" (if error "fail" "pass"))
                (if error
                  (do (swap! failures conj {:id (:id spec) :error error})
                      (set! (.-textContent row) (str "FAIL " (:id spec) " " error)))
                  (do (swap! passed inc)
                      (set! (.-textContent row) (str "PASS " (:id spec) " " (or (:check-label spec) (:title spec))))))
                (.appendChild report row)))
            (after-render! [spec attempts]
              (let [error (.-textContent (element "error"))
                    texture-error (some (fn [[path status]] (when (= :error status) path)) @asset-status)
                    ready? (and (>= @(:drawn-frame @runtime) 20)
                                (every? #(= :loaded (get @asset-status %)) (:assets spec)))]
                (cond
                  (not (str/blank? error)) (do (record! spec error) (next!))
                  texture-error (do (record! spec (str "纹理加载失败 " texture-error)) (next!))
                  ready? (do (record! spec nil) (next!))
                  (> attempts 60) (do (record! spec "画布绘制或纹理加载超时") (next!))
                  :else (js/setTimeout #(after-render! spec (inc attempts)) 50))))
            (next! []
              (if-let [spec (first @queue)]
                (do
                  (swap! queue next)
                  (set! (.-textContent summary) (str "正在检查 " (:id spec) "；已通过 " @passed " 个"))
                  (try
                    (mount! (:id spec) {})
                    (let [result (engine/run (:id spec) 20
                                             (if (:check-input spec) {:input-fn (:check-input spec)} {}))
                          {:keys [state frame running?]} @runtime]
                      (when-not (finite? result) (throw (ex-info "状态或绘图坐标包含非有限数值" {:id (:id spec)})))
                      (when (and (:check-input spec) (= (:state result) (:state (engine/run (:id spec) 20))))
                        (throw (ex-info "交互输入没有改变状态" {:id (:id spec)})))
                      (reset! running? false)
                      (reset! state (:state result))
                      (reset! frame 20))
                    (js/setTimeout #(after-render! spec 0) 50)
                    (catch :default e
                      (record! spec (.-message e))
                      (js/setTimeout next! 0))))
                (do
                  (stop!)
                  (.setAttribute summary "data-status" (if (empty? @failures) "pass" "fail"))
                  (set! (.-textContent summary)
                        (str "检查完成：" @passed " 项通过，" (count @failures) " 项失败；"
                             (count registry/examples) " 个示例与 " (count input-cases) " 项交互检查")))))]
      (next!))))

(.addEventListener js/window "beforeunload" stop!)
(if (= "loading" (.-readyState js/document))
  (.addEventListener js/document "DOMContentLoaded" init!)
  (init!))

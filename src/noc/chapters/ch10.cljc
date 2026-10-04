(ns noc.chapters.ch10
  "第 10 章：感知机与实际反向传播训练的手势分类器。"
  (:require [noc.math :as m] [noc.draw :as d] [noc.learning :as l]))

(defn sign [n] (if (neg? n) -1 1))
(defn target-y [x] (- (* 0.3 x) 0.2))
(defn training-point []
  (let [x (m/random -1 1) y (m/random -1 1)] {:inputs [x y 1] :label (if (> y (target-y x)) 1 -1)}))
(defn perceptron-setup [_]
  {:weights (vec (repeatedly 3 #(m/random -1 1))) :points (vec (repeatedly 160 training-point)) :index 0 :iterations 0})
(defn perceptron-guess [weights inputs] (sign (reduce + (map * weights inputs))))
(defn perceptron-train [weights inputs label rate]
  (let [error (- label (perceptron-guess weights inputs))]
    (mapv (fn [w x] (+ w (* rate error x))) weights inputs)))
(defn perceptron-step [{:keys [weights points index] :as state} {:keys [rate]}]
  (let [{:keys [inputs label]} (nth points index)]
    (assoc state :weights (perceptron-train weights inputs label rate) :index (mod (inc index) (count points)) :iterations (inc (:iterations state)))))
(defn to-canvas [[x y]] [(m/map-range x -1 1 0 640) (m/map-range y -1 1 360 0)])
(defn perceptron-scene [{:keys [weights points iterations]} _]
  (let [[wx wy bias] weights learned-y (fn [x] (/ (- (+ (* wx x) bias)) (if (< (m/abs wy) 1.0e-6) 1.0e-6 wy)))
        correct (count (filter #(= (:label %) (perceptron-guess weights (:inputs %))) points))]
    (d/scene (d/background)
             (d/line (to-canvas [-1 (target-y -1)]) (to-canvas [1 (target-y 1)]) d/ink)
             (d/line (to-canvas [-1 (learned-y -1)]) (to-canvas [1 (learned-y 1)]) d/accent)
             (mapcat (fn [{:keys [inputs label]}]
                       (d/circle (to-canvas (take 2 inputs)) 7 (if (= label (perceptron-guess weights inputs)) d/teal d/accent))) points)
             (d/label (str "训练次数 " iterations " · 正确率 " (long (* 100 (/ correct (count points)))) "% · 黑：真实线，红：学习线")))))

(def gesture-data
  [{:inputs [0.99 0.02] :label 0} {:inputs [0.76 -0.1] :label 0}
   {:inputs [-1.0 0.12] :label 1} {:inputs [-0.9 -0.1] :label 1}
   {:inputs [0.02 0.98] :label 2} {:inputs [-0.2 0.75] :label 2}
   {:inputs [0.01 -0.9] :label 3} {:inputs [-0.1 -0.8] :label 3}])
(def gesture-labels ["向右" "向左" "向下" "向上"])
(defn normalized-input [[x y]] [(/ (+ x 1) 2) (/ (+ y 1) 2)])
(defn one-hot [label] (assoc [0.0 0.0 0.0 0.0] label 1.0))
(defn gesture-epoch [net rate]
  (reduce (fn [brain {:keys [inputs label]}] (l/train brain (normalized-input inputs) (one-hot label) rate)) net gesture-data))
(defn gesture-loss [net]
  (/ (reduce + (for [{:keys [inputs label]} gesture-data
                     [prediction actual] (map vector (l/predict net (normalized-input inputs)) (one-hot label))]
                 (m/pow (- prediction actual) 2))) (* 4 (count gesture-data))))
(defn gesture-setup [_]
  {:brain (l/brain 2 8 4) :epoch 0 :loss 1 :status "正在训练" :was-pressed? false :start nil :end nil :probabilities nil})
(defn gesture-step [{:keys [brain epoch was-pressed? start end] :as state} {:keys [rate epochs mouse pressed?]}]
  (let [train? (< epoch epochs)
        brain (if train? (nth (iterate #(gesture-epoch % rate) brain) 2) brain)
        epoch (if train? (+ epoch 2) epoch)
        state (assoc state :brain brain :epoch epoch :loss (gesture-loss brain) :was-pressed? pressed?)
        state (if (and (not train?) (= "正在训练" (:status state))) (assoc state :status "拖动鼠标画一个方向") state)
        state (cond (and pressed? (not was-pressed?)) (assoc state :start mouse :end mouse)
                    pressed? (assoc state :end mouse)
                    :else state)]
    (if (and was-pressed? (not pressed?) start end (> (m/distance start end) 2))
      (let [input (normalized-input (m/normalize (m/v- end start))) result (l/predict brain input)]
        (assoc state :probabilities result :status (nth gesture-labels (l/best-index result)))) state)))
(defn gesture-scene [{:keys [status epoch loss start end probabilities]} _]
  (d/scene (d/background)
           (d/label (str "实际 MLP：2→8→4 · 训练轮次 " epoch " · 均方误差 " (/ (long (* 10000 loss)) 10000.0)))
           (d/label status 245 160)
           (when (and start end) (d/scene [[:stroke-weight 5]] (d/line start end d/accent)))
           (when probabilities
             (mapcat (fn [index p]
                       (d/scene [[:no-stroke] [:fill 24 153 145] [:rect 150 (+ 230 (* index 26)) (* 330 p) 14]]
                                (d/label (str (nth gesture-labels index) " " (long (* 100 p)) "%") 25 (+ 244 (* index 26)))))
                     (range 4) probabilities))))

(def examples
  [{:id "10.1" :chapter 10 :title "感知机" :original-title "The Perceptron" :width 640 :height 360
    :params {:rate {:label "学习率" :default 0.01 :min 0.001 :max 0.1 :step 0.001}}
    :setup perceptron-setup :step perceptron-step :scene perceptron-scene
    :explanation "输入是归一化的 x、y 和常数偏置 1。加权和经过符号函数得到 ±1 分类。每个权重按 学习率×误差×输入 更新。红线来自学到的权重，正确样本为绿色，错误样本为红色。感知机适合线性可分问题。"}
   {:id "10.2" :chapter 10 :title "手势分类器" :original-title "Gesture Classifier" :width 640 :height 360
    :params {:rate {:label "学习率" :default 0.6 :min 0.05 :max 1 :step 0.05} :epochs {:label "训练轮次" :default 400 :min 100 :max 1000 :step 50}}
    :setup gesture-setup :step gesture-step :scene gesture-scene
    :explanation "以原例的八组方向数据训练四类分类器。此版本直接用 Clojure 实现 2→8→4 前馈网络、sigmoid、均方误差和反向传播，不依赖远程机器学习服务。画线后把方向向量归一化，网络预测向左、向右、向上、向下，并展示四个输出。"}])

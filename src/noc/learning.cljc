(ns noc.learning
  "小型前馈神经网络与遗传操作：用于学习和神经进化示例。"
  (:require [noc.math :as m]))

(defn weighted-choice
  "轮盘赌选择；全体适应度为零时均匀选择，避免除零和越界。"
  [population fitness]
  (let [weights (mapv #(max 0 (double (fitness %))) population)
        total (reduce + weights)]
    (if (pos? total)
      (loop [index 0 remaining (m/random total)]
        (if (or (= index (dec (count population))) (<= remaining (nth weights index)))
          (nth population index)
          (recur (inc index) (- remaining (nth weights index)))))
      (m/choose population))))

(defn crossover [a b]
  (let [midpoint (m/random-int (count a))]
    (mapv (fn [i] (nth (if (< i midpoint) a b) i)) (range (count a)))))
(defn mutate-genes [genes rate random-gene]
  (mapv #(if (< (m/random) rate) (random-gene) %) genes))
(defn sigmoid [x] (/ 1.0 (+ 1 (m/exp (- (m/clamp x -40 40))))))
(defn matrix [rows columns]
  (mapv (fn [_] (mapv (fn [_] (m/gaussian 0 (/ 1 (m/sqrt columns)))) (range columns))) (range rows)))
(defn brain [inputs hidden outputs]
  {:inputs inputs :hidden hidden :outputs outputs
   :w1 (matrix hidden (inc inputs)) :w2 (matrix outputs (inc hidden))})
(defn layer [weights inputs]
  (let [with-bias (conj (vec inputs) 1.0)]
    (mapv #(sigmoid (reduce + (map * % with-bias))) weights)))
(defn activations [{:keys [w1 w2]} inputs]
  (let [hidden (layer w1 inputs)] {:hidden hidden :output (layer w2 hidden)}))
(defn predict [{:keys [w1 w2]} inputs] (layer w2 (layer w1 inputs)))
(defn train
  "一次样本的反向传播，平方误差和 sigmoid 导数。"
  [{:keys [w1 w2] :as net} inputs target rate]
  (let [hidden (layer w1 inputs) output (layer w2 hidden)
        delta-out (mapv (fn [expected actual] (* (- expected actual) actual (- 1 actual))) target output)
        delta-hidden (mapv (fn [j h]
                             (* h (- 1 h) (reduce + (map-indexed (fn [i delta] (* delta (get-in w2 [i j]))) delta-out))))
                           (range (count hidden)) hidden)
        update-weights (fn [weights deltas values]
                         (mapv (fn [row delta] (mapv (fn [weight value] (+ weight (* rate delta value))) row (conj (vec values) 1.0))) weights deltas))]
    (assoc net :w1 (update-weights w1 delta-hidden inputs) :w2 (update-weights w2 delta-out hidden))))
(defn brain-genes [{:keys [w1 w2]}] (vec (concat (mapcat identity w1) (mapcat identity w2))))
(defn with-genes [{:keys [inputs hidden outputs] :as net} genes]
  (let [split (* hidden (inc inputs))]
    (assoc net :w1 (mapv vec (partition (inc inputs) (take split genes)))
           :w2 (mapv vec (partition (inc hidden) (drop split genes))))))
(defn mutate-brain [net rate]
  (with-genes net (mapv #(if (< (m/random) rate) (+ % (m/gaussian 0 0.25)) %) (brain-genes net))))
(defn crossover-brain [a b] (with-genes a (crossover (brain-genes a) (brain-genes b))))
(defn best-index [values]
  (first (apply max-key second (map-indexed vector values))))

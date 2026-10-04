(ns noc.chapters.ch00
  (:require [noc.math :as m] [noc.draw :as d]))

(defn walker-setup [{:keys [width height]}]
  {:position [(/ width 2) (/ height 2)] :trail [] :t 0})

(defn keep-trail [state p]
  (assoc state :position p :trail (vec (take-last 4000 (conj (:trail state) p)))))

(defn walk [state {:keys [width height step-size]} biased?]
  (let [r (m/random)
        delta (if biased?
                (cond (< r 0.4) [1 0] (< r 0.6) [-1 0] (< r 0.8) [0 1] :else [0 -1])
                (m/choose [[1 0] [-1 0] [0 1] [0 -1]]))
        [x y] (m/v+ (:position state) (m/v* delta step-size))]
    (keep-trail state [(m/clamp x 0 (dec width)) (m/clamp y 0 (dec height))])))

(defn walker-scene [{:keys [position trail]} _]
  (d/scene (d/background) [[:stroke 39 52 69] [:stroke-weight 1]]
           (mapv (fn [[x y]] [:point x y]) trail) (d/circle position 8)))

(defn accept-reject []
  (loop []
    (let [candidate (m/random) threshold (m/random)]
      (if (< threshold candidate) candidate (recur)))))

(defn counts-setup [{:keys [bins]}] {:counts (vec (repeat (long bins) 0)) :samples 0})
(defn count-step [state _ sampler]
  (let [i (min (dec (count (:counts state)))
               (long (m/floor (* (count (:counts state)) (sampler)))))]
    (-> state (update-in [:counts i] inc) (update :samples inc))))
(defn histogram-scene [{:keys [counts samples]} {:keys [width height]}]
  (let [bar-width (/ width (count counts)) scale (/ (- height 45) (max 10 (apply max counts)))]
    (d/scene (d/background) [[:stroke 39 52 69] [:fill 218 57 108]]
             (mapv (fn [i n] [:rect (* i bar-width) (- height (* n scale)) (dec bar-width) (* n scale)])
                   (range) counts)
             (d/label (str "样本数 " samples)))))

(defn gaussian-step [state {:keys [width sd]}]
  (update state :samples #(vec (take-last 2500 (conj % (m/gaussian (/ width 2) sd))))))
(defn gaussian-scene [{:keys [samples]} {:keys [height]}]
  (d/scene (d/background) [[:no-stroke] [:fill 39 52 69 12]]
           (mapv (fn [x] [:circle x (/ height 2) 16]) samples)))
(defn noise-step [state {:keys [width height noise-speed]}]
  (let [t (:t state)]
    (-> state
        (assoc :position [(* width (m/noise t)) (* height (m/noise (+ 10000 t)))])
        (update :t + noise-speed))))

(def examples
  [{:id "0.1" :chapter 0 :title "传统随机游走" :width 640 :height 240
    :params {:step-size {:label "步长" :default 1 :min 1 :max 10 :step 1}}
    :setup walker-setup :step #(walk %1 %2 false) :scene walker-scene
    :explanation "每帧等概率选择上、下、左、右一步；状态是包含坐标和轨迹的不可变映射。"}
   {:id "0.2" :chapter 0 :title "随机数分布" :width 640 :height 240
    :params {:bins {:label "分箱数（重置生效）" :default 20 :min 5 :max 40 :step 1}}
    :setup counts-setup :step #(count-step %1 %2 m/random) :scene histogram-scene
    :explanation "均匀抽样并累加直方图；样本越多，各个分箱的相对频率越接近。"}
   {:id "0.3" :chapter 0 :title "偏向右侧的随机游走" :width 640 :height 240
    :params {:step-size {:label "步长" :default 1 :min 1 :max 10 :step 1}}
    :setup walker-setup :step #(walk %1 %2 true) :scene walker-scene
    :explanation "向右的概率为 40%，其余三个方向各为 20%，长期轨迹因而向右偏移。"}
   {:id "0.4" :chapter 0 :title "高斯分布" :width 640 :height 240
    :params {:sd {:label "标准差" :default 60 :min 5 :max 180 :step 1}}
    :setup (fn [_] {:samples []}) :step gaussian-step :scene gaussian-scene
    :explanation "Box–Muller 变换把均匀随机数变为正态样本，均值在画布中央，透明圆点显示密度。"}
   {:id "0.5" :chapter 0 :title "接受—拒绝分布" :width 640 :height 240
    :params {:bins {:label "分箱数（重置生效）" :default 20 :min 5 :max 40 :step 1}}
    :setup counts-setup :step #(count-step %1 %2 accept-reject) :scene histogram-scene
    :explanation "候选值 x 以概率 x 被接受，得到概率密度随 x 线性增长的分布。"}
   {:id "0.6" :chapter 0 :title "Perlin 噪声游走者" :width 640 :height 240
    :params {:noise-speed {:label "噪声时间步长" :default 0.01 :min 0.001 :max 0.1 :step 0.001}}
    :setup walker-setup :step noise-step
    :scene (fn [s _] (d/scene (d/background) (d/circle (:position s) 48)))
    :explanation "两个相距较远的噪声时间坐标分别控制 x 与 y，连续梯度噪声产生平滑运动。"}])

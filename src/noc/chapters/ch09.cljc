(ns noc.chapters.ch09
  "第 9 章：遗传算法、交互选择与生态演化。"
  (:require [noc.math :as m] [noc.draw :as d] [noc.learning :as l]))

(def target-phrase "to be or not to be")
(def alphabet (mapv #(str #?(:clj (char %) :cljs (js/String.fromCharCode %))) (range 32 127)))
(defn random-character [] (m/choose alphabet))
(defn phrase-fitness [genes]
  (/ (count (filter true? (map = genes (map str target-phrase)))) (double (count target-phrase))))
(defn phrase-individual [genes] {:genes genes :fitness (phrase-fitness genes)})
(defn phrase-setup [{:keys [population]}]
  {:population (mapv (fn [_] (phrase-individual (vec (repeatedly (count target-phrase) random-character)))) (range (long population))) :generation 0})
(defn phrase-generation [{:keys [population] :as state} {:keys [mutation]}]
  (if (some #(= 1.0 (:fitness %)) population) state
      (assoc state :generation (inc (:generation state))
             :population (mapv (fn [_]
                                 (let [a (l/weighted-choice population :fitness) b (l/weighted-choice population :fitness)]
                                   (phrase-individual (l/mutate-genes (l/crossover (:genes a) (:genes b)) mutation random-character)))) population))))
(defn phrase-scene [{:keys [population generation]} _]
  (let [sorted (sort-by :fitness > population) best (first sorted)]
    (d/scene (d/background)
             (d/label (str "目标：" target-phrase) 16 32)
             (d/label (str "最佳：" (apply str (:genes best))) 16 70)
             (d/label (str "世代 " generation " · 匹配 " (long (* 100 (:fitness best))) "%") 16 104)
             (d/label (str "平均适应度 " (/ (long (* 10000 (/ (reduce + (map :fitness population)) (count population)))) 100.0) "%") 16 132)
             (map-indexed (fn [i individual] [:text (apply str (:genes individual)) (+ 16 (* 210 (quot i 9))) (+ 170 (* 20 (mod i 9)))]) (take 27 sorted)))))

(defn force-gene [] (m/v* (m/random-vector) (m/random 0.25)))
(defn rocket [genes] {:genes genes :position [320 340] :velocity [0 0] :record-distance 1000 :finish 1 :hit? false :crashed? false})
(defn rocket-setup [{:keys [population lifespan]}]
  {:population (mapv (fn [_] (rocket (vec (repeatedly (long lifespan) force-gene)))) (range (long population)))
   :generation 0 :age 0 :target [320 40] :best-distance 1000})
(defn in-obstacle? [[x y]] (and (< 245 x 395) (< 175 y 190)))
(defn rocket-update [individual age target advanced?]
  (if (or (:hit? individual) (:crashed? individual)) individual
      (let [velocity (m/limit (m/v+ (:velocity individual) (nth (:genes individual) age [0 0])) 5)
            position (m/v+ (:position individual) velocity)
            distance (m/distance position target)
            [x y] position]
        (assoc individual :position position :velocity velocity
               :record-distance (min distance (:record-distance individual))
               :finish (inc (:finish individual)) :hit? (and advanced? (< distance 12))
               :crashed? (and advanced? (or (in-obstacle? position) (< x 0) (> x 640) (< y 0) (> y 360)))))))
(defn rocket-fitness [r target advanced?]
  (if advanced?
    (* (m/pow (/ 1.0 (* (:finish r) (max 1 (:record-distance r)))) 4) (if (:crashed? r) 0.1 1) (if (:hit? r) 2 1))
    (/ 1.0 (max 1 (m/mag-sq (m/v- (:position r) target))))))
(defn rockets-step [{:keys [population age target] :as state} {:keys [lifespan mutation clicked? mouse] :as env} advanced?]
  (let [target (if clicked? mouse target)]
    (if (< age lifespan)
      (let [updated (mapv #(rocket-update % age target advanced?) population)]
        (assoc state :population updated :age (inc age) :target target
               :best-distance (min (:best-distance state) (apply min (map :record-distance updated)))))
      (let [fitness #(rocket-fitness % target advanced?)]
        (assoc state :age 0 :generation (inc (:generation state)) :target target
               :population (mapv (fn [_]
                                   (let [a (l/weighted-choice population fitness) b (l/weighted-choice population fitness)]
                                     (rocket (l/mutate-genes (l/crossover (:genes a) (:genes b)) mutation force-gene)))) population))))))
(defn rocket-drawing [{:keys [position velocity hit? crashed?]}]
  (let [[x y] position angle (+ (/ m/pi 2) (m/heading velocity))]
    [[:push] [:translate x y] [:rotate angle] [:stroke 39 52 69]
     (into [:fill] (cond hit? d/teal crashed? [170 170 170] :else d/accent))
     [:triangle 0 -9 -4 8 4 8] [:pop]]))
(defn rockets-scene [{:keys [population target age generation best-distance]} advanced?]
  (d/scene (d/background) (d/circle target 24 d/teal)
           (when advanced? [[:stroke 39 52 69] [:fill 39 52 69] [:rect 245 175 150 15]])
           (mapcat rocket-drawing population)
           (d/label (str "世代 " generation " · 时间 " age " · 最近距离 " (long best-distance) " · 点击移动目标"))))
(def rocket-params {:population {:label "种群数量" :default 70 :min 20 :max 150 :step 10}
                    :lifespan {:label "每代寿命" :default 180 :min 80 :max 300 :step 20}
                    :mutation {:label "突变概率" :default 0.01 :min 0 :max 0.2 :step 0.01}})

(defn flower [genes] {:genes genes :fitness 1})
(defn flower-setup [_] {:population (mapv (fn [_] (flower (vec (repeatedly 14 m/random)))) (range 8)) :generation 0})
(defn flower-step [{:keys [population] :as state} {:keys [mouse clicked? mutation]}]
  (let [[mx my] mouse]
    (if (and clicked? (< 10 mx 180) (< 8 my 42))
      (assoc state :generation (inc (:generation state))
             :population (mapv (fn [_]
                                 (let [a (l/weighted-choice population :fitness) b (l/weighted-choice population :fitness)]
                                   (flower (l/mutate-genes (l/crossover (:genes a) (:genes b)) mutation m/random)))) population))
      (update state :population #(mapv (fn [i f] (if (and (< (m/abs (- mx (+ 40 (* i 80)))) 35) (< 95 my 275)) (update f :fitness + 0.25) f)) (range 8) %)))))
(defn flower-drawing [index {:keys [genes fitness]}]
  (let [x (+ 40 (* index 80)) stem-length (m/lerp 50 100 (nth genes 13)) y (- 270 stem-length)
        size (m/lerp 4 24 (nth genes 4)) petals (long (m/lerp 2 16 (nth genes 5)))
        color (mapv #(* 255 %) (take 3 genes)) center (mapv #(* 255 %) (subvec genes 6 9))]
    (d/scene [[:stroke 210 210 210] [:no-fill] [:rect (- x 35) 95 70 180]
              (into [:stroke] (map #(* 255 %) (subvec genes 10 13))) [:stroke-weight 3] [:line x y x 270] [:no-stroke]
              (into [:fill] (conj color (* 255 (nth genes 3))))]
             (for [i (range petals) :let [a (* m/tau (/ i petals))]]
               [:circle (+ x (* size (m/cos a))) (+ y (* size (m/sin a))) size])
             [(into [:fill] center) [:circle x y (m/lerp 24 48 (nth genes 9))]]
             (d/label (long fitness) (- x 8) 300))))
(defn flower-scene [{:keys [population generation]} _]
  (d/scene (d/background) [[:fill 24 153 145] [:no-stroke] [:rect 10 8 170 34]]
           (d/label "点击生成下一代" 18 31) (mapcat (fn [[i f]] (flower-drawing i f)) (map-indexed vector population))
           (d/label (str "世代 " generation " · 鼠标停留时间决定花朵适应度") 12 340)))

(defn bloop [position gene] {:position position :gene gene :health 200 :xoff (m/random 1000) :yoff (m/random 1000)})
(defn ecosystem-setup [{:keys [population]}]
  {:creatures (mapv (fn [_] (bloop [(m/random 640) (m/random 360)] (m/random))) (range (long population)))
   :food (vec (repeatedly 100 #(vector (m/random 640) (m/random 360)))) :births 0 :deaths 0})
(defn ecosystem-step [{:keys [creatures food births deaths] :as state} {:keys [reproduction mutation]}]
  (let [result
        (reduce (fn [{:keys [food] :as acc} c]
                  (let [speed (m/lerp 15 0 (:gene c)) radius (m/lerp 0 25 (:gene c))
                        velocity [(* speed (- (* 2 (m/noise (:xoff c))) 1)) (* speed (- (* 2 (m/noise (:yoff c))) 1))]
                        p (m/wrap (m/v+ (:position c) velocity) 640 360)
                        eaten (filter #(< (m/distance p %) (* 2 radius)) food)
                        c (-> c (assoc :position p :health (+ (- (:health c) 0.2) (* 100 (count eaten)))) (update :xoff + 0.01) (update :yoff + 0.01))]
                    (if (neg? (:health c))
                      (-> acc (update :food conj p) (update :deaths inc))
                      (cond-> (-> acc (update :creatures conj c) (assoc :food (vec (remove (set eaten) food))))
                        (and (< (+ (count creatures) (- (:births acc) births)) 180) (< (m/random) reproduction))
                        (-> (update :creatures conj (bloop p (if (< (m/random) mutation) (m/random) (:gene c)))) (update :births inc))))))
                {:creatures [] :food food :births births :deaths deaths} creatures)]
    (cond-> (merge state result) (< (m/random) 0.2) (update :food conj [(m/random 640) (m/random 360)]))))
(defn ecosystem-scene [{:keys [creatures food births deaths]} _]
  (d/scene (d/background) [[:fill 24 153 145] [:no-stroke]] (map (fn [[x y]] [:circle x y 4]) food)
           (mapcat (fn [{:keys [position gene health]}] (d/circle position (* 2 (m/lerp 0 25 gene)) [39 52 69 (m/clamp health 25 230)])) creatures)
           (d/label (str "生物 " (count creatures) " · 出生 " births " · 死亡 " deaths " · 大体型移动更慢"))))

(def examples
  [{:id "9.1" :chapter 9 :title "用遗传算法演化莎士比亚短句" :original-title "Genetic Algorithm for Evolving Shakespeare" :width 640 :height 360
    :params {:population {:label "种群数量" :default 150 :min 50 :max 300 :step 25} :mutation {:label "突变概率" :default 0.01 :min 0 :max 0.2 :step 0.01}}
    :setup phrase-setup :step (fn [state env] (nth (iterate #(phrase-generation % env) state) 3)) :scene phrase-scene
    :explanation "DNA 是字符向量，适应度是与目标短句同位置匹配的比例。轮盘赌按适应度选择两位亲本，单点交叉组合基因，再以独立概率突变字符。每帧推进三代；完美匹配后停止演化。"}
   {:id "9.2" :chapter 9 :title "智能火箭" :original-title "Smart Rockets" :width 640 :height 360 :params rocket-params
    :setup rocket-setup :step (fn [state env] (rockets-step state env false)) :scene (fn [state _] (rockets-scene state false))
    :explanation "每个基因都是某一帧要施加的推力向量。寿命结束时，用最终距离的平方倒数评估适应度，选择、交叉和突变生成下一代推力序列。点击会移动目标，种群需要重新适应。"}
   {:id "9.3" :chapter 9 :title "更聪明的火箭" :original-title "Smarter Rockets" :width 640 :height 360 :params rocket-params
    :setup rocket-setup :step (fn [state env] (rockets-step state env true)) :scene (fn [state _] (rockets-scene state true))
    :explanation "加入障碍物、到达状态、最短记录距离和完成时间。适应度同时奖励接近目标与快速完成；撞到障碍物降为十分之一，到达目标奖励加倍。碰撞后的火箭停止运动。"}
   {:id "9.4" :chapter 9 :title "交互选择" :original-title "Interactive Selection" :width 640 :height 360
    :params {:mutation {:label "突变概率" :default 0.05 :min 0 :max 0.3 :step 0.01}}
    :setup flower-setup :step flower-step :scene flower-scene
    :explanation "人代替数学公式评判美感。鼠标在花朵上停留越久，它的适应度越高。十四个基因控制花瓣颜色、数量、大小、花心和茎；点击画布左上按钮，按选择结果生成下一代。"}
   {:id "9.5" :chapter 9 :title "演化中的生态系统" :original-title "An Evolving Ecosystem" :width 640 :height 360
    :params {:population {:label "初始生物数量" :default 30 :min 10 :max 80 :step 5}
             :reproduction {:label "每帧繁殖概率" :default 0.0005 :min 0 :max 0.01 :step 0.0005}
             :mutation {:label "突变概率" :default 0.01 :min 0 :max 0.2 :step 0.01}}
    :setup ecosystem-setup :step ecosystem-step :scene ecosystem-scene
    :explanation "体型与速度由同一个基因关联：大生物慢，小生物快。Perlin 噪声驱动游走，进食补充生命值，死亡转为食物。存活时间更长的个体有更多复制和突变机会，无须统一的代际评分。为保持浏览器响应，种群上限为 180。"}])

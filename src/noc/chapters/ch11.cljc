(ns noc.chapters.ch11
  "第 11 章：神经网络权重参与选择、交叉和突变。"
  (:require [noc.math :as m] [noc.draw :as d] [noc.learning :as l] [noc.chapters.ch09 :as ga]))

(defn pipe [gap] (let [top (m/random 30 (- 330 gap))] {:x 640 :top top :bottom (+ top gap) :width 28}))
(defn pipe-collision? [{:keys [y]} {:keys [x width top bottom]}]
  (and (< x 50 (+ x width)) (or (< y top) (> y bottom))))
(defn update-pipes [pipes age gap]
  (let [updated (mapv #(update % :x - 2.5) (filter #(> (:x %) -28) pipes))]
    (cond-> updated (zero? (mod age 95)) (conj (pipe gap)))))
(defn bird [brain] {:y 160 :velocity 0 :brain brain :fitness 0 :alive? true})
(defn bird-motion [bird flap? gravity]
  (let [velocity (+ (:velocity bird) (if flap? -10 0) gravity) y (+ (:y bird) velocity)]
    (assoc bird :y y :velocity (* velocity 0.95))))
(defn pipe-drawing [{:keys [x top bottom width]}]
  [[:fill 39 52 69] [:no-stroke] [:rect x 0 width top] [:rect x bottom width (- 360 bottom)]])
(defn birds-scene [{:keys [birds pipes generation age best-fitness collision?]} manual?]
  (d/scene (d/background) (mapcat pipe-drawing pipes)
           (mapcat (fn [{:keys [y alive?]}] (when alive? (d/circle [50 y] 16 [218 57 108 (if manual? 255 95)]))) birds)
           (d/label (if manual? (str "点击拍翅 · " (if collision? "撞到水管" "飞行中"))
                        (str "世代 " generation " · 存活 " (count (filter :alive? birds)) " · 最佳寿命 " best-fitness " · 本代时间 " age)))))
(defn manual-setup [{:keys [gap]}] {:birds [(bird nil)] :pipes [(pipe gap)] :age 0 :generation 0})
(defn manual-step [{:keys [birds pipes age] :as state} {:keys [clicked? gravity gap]}]
  (let [b (bird-motion (first birds) clicked? gravity) b (if (> (:y b) 352) (assoc b :y 352 :velocity 0) b)
        pipes (update-pipes pipes age gap)]
    (assoc state :birds [b] :pipes pipes :age (inc age) :collision? (boolean (some #(pipe-collision? b %) pipes)))))
(def evolution-params {:population {:label "种群数量" :default 50 :min 10 :max 150 :step 10}
                       :mutation {:label "权重突变概率" :default 0.05 :min 0 :max 0.3 :step 0.01}})
(defn child-brain [population fitness mutation]
  (let [a (:brain (l/weighted-choice population fitness)) b (:brain (l/weighted-choice population fitness))]
    (l/mutate-brain (l/crossover-brain a b) mutation)))
(defn evolved-birds-setup [{:keys [population gap]}]
  {:birds (mapv (fn [_] (bird (l/brain 4 8 2))) (range (long population))) :pipes [(pipe gap)] :age 0 :generation 0 :best-fitness 0})
(defn evolved-bird-step [b pipes gravity]
  (if-not (:alive? b) b
          (let [next-pipe (or (first (filter #(> (+ (:x %) (:width %)) 50) pipes)) {:x 640 :top 120})
                inputs [(/ (:y b) 360) (/ (:velocity b) 360) (/ (:top next-pipe) 360) (/ (- (:x next-pipe) 50) 640)]
                output (l/predict (:brain b) inputs)
                b (bird-motion b (> (first output) (second output)) gravity)]
            (assoc b :fitness (inc (:fitness b))
                   :alive? (and (< 0 (:y b) 360) (not-any? #(pipe-collision? b %) pipes))))))
(defn evolved-birds-step [{:keys [birds pipes age] :as state} {:keys [gravity gap mutation]}]
  (let [pipes (update-pipes pipes age gap) birds (mapv #(evolved-bird-step % pipes gravity) birds)
        best (max (:best-fitness state) (apply max (map :fitness birds)))]
    (if (some :alive? birds)
      (assoc state :birds birds :pipes pipes :age (inc age) :best-fitness best)
      (assoc state :birds (mapv (fn [_] (bird (child-brain birds :fitness mutation))) birds)
             :pipes [(pipe gap)] :age 0 :generation (inc (:generation state)) :best-fitness best))))

(defn neural-rocket [brain]
  {:brain brain :position [320 340] :velocity [0 0] :record-distance 1000 :finish 1 :hit? false :crashed? false})
(defn neural-rockets-setup [{:keys [population]}]
  {:population (mapv (fn [_] (neural-rocket (l/brain 2 8 2))) (range (long population)))
   :target [320 40] :age 0 :generation 0 :best-distance 1000})
(defn neural-rocket-step [r target]
  (if (or (:hit? r) (:crashed? r)) r
      (let [[x y] (:position r) [angle magnitude] (l/predict (:brain r) [(/ x 640) (/ y 360)])
            force (m/v* (m/from-angle (* angle m/tau)) magnitude)
            velocity (m/limit (m/v+ (:velocity r) force) 4)
            position (m/v+ (:position r) velocity) distance (m/distance position target) [x y] position]
        (assoc r :position position :velocity velocity :record-distance (min distance (:record-distance r))
               :finish (inc (:finish r)) :hit? (< distance 12)
               :crashed? (or (ga/in-obstacle? position) (< x 0) (> x 640) (< y 0) (> y 360))))))
(defn neural-rockets-step [{:keys [population age target] :as state} {:keys [lifespan mutation clicked? mouse]}]
  (let [target (if clicked? mouse target)]
    (if (< age lifespan)
      (let [updated (mapv #(neural-rocket-step % target) population)]
        (assoc state :population updated :target target :age (inc age)
               :best-distance (min (:best-distance state) (apply min (map :record-distance updated)))))
      (assoc state :population (mapv (fn [_] (neural-rocket (child-brain population #(ga/rocket-fitness % target true) mutation))) population)
             :target target :age 0 :generation (inc (:generation state))))))

(defn steering-creature [brain]
  {:position [(m/random 640) (m/random 360)] :velocity [0 0] :brain brain :fitness 0})
(defn steering-setup [{:keys [population]}]
  {:creatures (mapv (fn [_] (steering-creature (l/brain 5 8 2))) (range (long population)))
   :target [320 180] :target-time 0 :age 0 :generation 0 :best-fitness 0})
(defn steering-update [{:keys [position velocity brain] :as c} target]
  (let [delta (m/v- target position) [dx dy] (m/normalize delta)
        [vx vy] (m/vdiv velocity 4)
        [angle strength] (l/predict brain [dx dy (/ (m/mag delta) 640) vx vy])
        velocity (m/limit (m/v+ velocity (m/v* (m/from-angle (* m/tau angle)) strength)) 4)
        position (m/v+ position velocity)]
    (assoc c :position position :velocity velocity
           :fitness (+ (:fitness c) (if (< (m/distance position target) 28) 1 0)))))
(defn steering-step-once [{:keys [creatures target-time age] :as state} {:keys [lifespan mutation pressed? mouse]}]
  (let [target (if pressed? mouse [(* 640 (m/noise target-time)) (* 360 (m/noise (+ 130 target-time)))])
        creatures (mapv #(steering-update % target) creatures)]
    (if (< age lifespan)
      (assoc state :creatures creatures :target target :target-time (+ target-time 0.01) :age (inc age))
      (assoc state :creatures (mapv (fn [_] (steering-creature (child-brain creatures :fitness mutation))) creatures)
             :age 0 :generation (inc (:generation state))
             :best-fitness (max (:best-fitness state) (apply max (map :fitness creatures)))))))
(defn steering-scene [{:keys [creatures target generation age best-fitness]} _]
  (d/scene (d/background) (d/circle target 48 [24 153 145 95])
           (mapcat (fn [{:keys [position velocity]}] (ga/rocket-drawing {:position position :velocity velocity})) creatures)
           (d/label (str "世代 " generation " · 时间 " age " · 最佳接触时长 " best-fitness " · 拖动光源"))))

(defn sensor-offsets [radius]
  (mapv #(m/v* (m/from-angle (* m/tau (/ % 15))) radius) (range 15)))
(defn sensor-values [position radius food]
  (mapv (fn [offset]
          (let [tip (m/v+ position offset)]
            (reduce max 0 (map (fn [{:keys [position radius]}]
                                 (max 0 (- 1 (/ (m/distance tip position) radius)))) food)))) (sensor-offsets radius)))
(defn food [] {:position [(m/random 640) (m/random 360)] :radius 50})
(defn sensor-setup [_] {:position [320 180] :food [(food)] :values (vec (repeat 15 0))})
(defn sensor-step [state {:keys [mouse pressed? clicked?]}]
  (let [position (if pressed? mouse (:position state))
        food (if (and clicked? (not pressed?)) [(assoc (food) :position mouse)] (:food state))]
    (assoc state :position position :food food :values (sensor-values position 32 food))))
(defn food-drawing [foods]
  (mapcat (fn [{:keys [position radius]}] (d/circle position (* 2 radius) [24 153 145 70])) foods))
(defn sensors-drawing [position sensor-radius values body-radius color]
  (d/scene (mapcat (fn [offset value]
                     (let [tip (m/v+ position offset)]
                       (d/scene (d/line position tip [160 168 178])
                                (when (pos? value) (d/circle tip 7 [218 57 108 (* 255 value)])))))
                   (sensor-offsets sensor-radius) values)
           (d/circle position (* 2 body-radius) color)))
(defn sensor-scene [{:keys [position food values]} _]
  (d/scene (d/background) (food-drawing food) (sensors-drawing position 32 values 16 d/ink)
           (d/label "拖动生物：传感器端点进入食物时亮起")
           (d/label (str "传感器读数 " (mapv #(/ (long (* 100 %)) 100.0) values)) 12 344)))

(defn neural-bloop [position brain]
  {:position position :velocity [0 0] :brain brain :health 100 :values (vec (repeat 15 0))})
(defn neural-eco-setup [{:keys [population]}]
  {:creatures (mapv (fn [_] (neural-bloop [(m/random 640) (m/random 360)] (l/brain 15 10 2))) (range (long population)))
   :food (vec (repeatedly 8 food)) :births 0 :deaths 0})
(defn neural-eco-step-once [{:keys [creatures food births deaths] :as state} {:keys [mutation reproduction]}]
  (let [result
        (reduce (fn [{:keys [food] :as acc} {:keys [position velocity brain health] :as c}]
                  (let [values (sensor-values position 18 food) [angle strength] (l/predict brain values)
                        velocity (m/limit (m/v+ velocity (m/v* (m/from-angle (* m/tau angle)) strength)) 2)
                        position (m/wrap (m/v+ position velocity) 640 360)
                        radius (m/clamp (m/map-range health 0 100 2 12) 2 12)
                        inside? (fn [f] (< (m/distance position (:position f)) (+ radius (:radius f))))
                        eaten (count (filter inside? food))
                        food (mapv (fn [f] (if (inside? f) (if (< (:radius f) 20.05) (noc.chapters.ch11/food) (update f :radius - 0.05)) f)) food)
                        health (+ health -0.25 (* 0.5 eaten))
                        c (assoc c :position position :velocity velocity :health health :values values)]
                    (if (neg? health)
                      (-> acc (assoc :food food) (update :deaths inc))
                      (cond-> (-> acc (assoc :food food) (update :creatures conj c))
                        (and (< (+ (count creatures) (- (:births acc) births)) 120) (< (m/random) reproduction))
                        (-> (update :creatures conj (neural-bloop position (l/mutate-brain brain mutation))) (update :births inc))))))
                {:creatures [] :food food :births births :deaths deaths} creatures)]
    (merge state result)))
(defn neural-eco-scene [{:keys [creatures food births deaths]} _]
  (d/scene (d/background) (food-drawing food)
           (mapcat (fn [{:keys [position values health]}]
                     (sensors-drawing position 18 values (m/clamp (m/map-range health 0 100 2 12) 2 12)
                                      [39 52 69 (m/clamp (* health 2) 20 240)])) creatures)
           (d/label (str "生物 " (count creatures) " · 出生 " births " · 死亡 " deaths " · 食物越多，健康越久"))))

(def examples
  [{:id "11.1" :chapter 11 :title "Flappy Bird 复刻" :original-title "Flappy Bird Clone" :width 640 :height 360
    :params {:gravity {:label "重力" :default 0.5 :min 0.1 :max 1 :step 0.1} :gap {:label "水管间隙" :default 130 :min 70 :max 220 :step 10}}
    :setup manual-setup :step manual-step :scene (fn [state _] (birds-scene state true))
    :explanation "鸟只在竖直方向运动，重力增加速度，点击施加向上的拍翅力；水管水平向左移动。先建立可玩的环境，再让神经网络接管相同的动作。撞到水管时显示提示。"}
   {:id "11.2" :chapter 11 :title "神经进化的 Flappy Bird" :original-title "Flappy Bird with Neuroevolution" :width 640 :height 360
    :params (merge evolution-params {:gravity {:label "重力" :default 0.5 :min 0.1 :max 1 :step 0.1} :gap {:label "水管间隙" :default 130 :min 70 :max 220 :step 10}})
    :setup evolved-birds-setup :step evolved-birds-step :scene (fn [state _] (birds-scene state false))
    :explanation "网络读取鸟高度、竖直速度、下一根水管的上沿和水平距离，输出拍翅或保持。存活帧数作为适应度。全体死亡后按轮盘赌选择网络，交叉真实权重并突变，开始下一代。训练发生在演化中，而非人工标签监督。"}
   {:id "11.3" :chapter 11 :title "神经进化的智能火箭" :original-title "Smart Rockets with Neuroevolution" :width 640 :height 360
    :params (merge evolution-params {:lifespan {:label "每代寿命" :default 200 :min 80 :max 400 :step 20}})
    :setup neural-rockets-setup :step neural-rockets-step :scene (fn [state _] (ga/rockets-scene state true))
    :explanation "DNA 从每帧推力序列改为网络权重。网络根据当前位置输出推力角度和强度。距离、完成时间、碰撞和到达奖励仍用于评估适应度，因此可以比较固定计划与依赖当前状态的策略。"}
   {:id "11.4" :chapter 11 :title "动态神经进化转向" :original-title "Dynamic Neuroevolutionary Steering" :width 640 :height 360
    :params (merge evolution-params {:lifespan {:label "每代寿命" :default 250 :min 100 :max 600 :step 50} :speed {:label "每帧模拟步数" :default 1 :min 1 :max 10 :step 1}})
    :setup steering-setup :step (fn [state env] (nth (iterate #(steering-step-once % env) state) (long (:speed env)))) :scene steering-scene
    :explanation "五个输入包含目标方向、目标距离和当前速度。输出控制转向力。移动光源提供变化的环境，接触光源的时间决定适应度。增加模拟速度可以更快观察世代变化；拖动画布移动目标。"}
   {:id "11.5" :chapter 11 :title "带传感器的 Bloop" :original-title "A Bloop with Sensors" :width 640 :height 360 :params {}
    :setup sensor-setup :step sensor-step :scene sensor-scene
    :explanation "十五个传感器均匀分布在生物周围。每个传感器端点进入食物圆时输出 0 到 1 的强度，越接近食物中心，读数越大。这些局部读数将成为下一例的网络输入。"}
   {:id "11.6" :chapter 11 :title "神经进化生态系统" :original-title "A Neuroevolutionary Ecosystem" :width 640 :height 360
    :params {:population {:label "初始生物数量" :default 20 :min 5 :max 50 :step 5}
             :mutation {:label "权重突变概率" :default 0.1 :min 0 :max 0.4 :step 0.01}
             :reproduction {:label "每步繁殖概率" :default 0.001 :min 0 :max 0.01 :step 0.001}
             :speed {:label "每帧模拟步数" :default 1 :min 1 :max 10 :step 1}}
    :setup neural-eco-setup :step (fn [state env] (nth (iterate #(neural-eco-step-once % env) state) (long (:speed env)))) :scene neural-eco-scene
    :explanation "网络以十五个局部食物传感器预测运动力。进食补充健康、消耗食物；每步健康衰减，死亡个体被移除。繁殖复制父代网络并突变，无须整齐的世代循环。食物过小后在随机位置再生，种群上限 120 保持浏览器响应。"}])

(ns noc.lessons.ch06
  "正文中的刚体与Verlet示例，使用本工程的Clojure求解器。"
  (:require [noc.math :as m]
            [noc.chapters.ch06 :as ch]))

(def env
  {:width 640 :height 360 :gravity 0.2 :restitution 0.35
   :iterations 8 :mouse [320 120] :pressed? false :attraction 16})

(defn make-world []
  {:mode :default :bodies [] :constraints [] :contacts #{}
   :collision-count 0})

(def box (ch/box-body 0 [160 100] 50 50 1))
(def ball (ch/circle-body 1 [180 100] 20 1))
(def ground
  (ch/static-body (ch/box-body 1000 [320 355] 640 10 1)))
(def world (assoc (make-world) :bodies [box ball ground]))

(defn add-body [world body] (update world :bodies conj body))
(defn remove-body [world body-id]
  (-> world
      (update :bodies #(filterv (fn [body] (not= body-id (:id body))) %))
      (update :constraints #(filterv (fn [constraint]
                                      (not (#{(:a constraint) (:b constraint)} body-id))) %))))

(defn force-on-body
  "单位时间步中，在指定世界坐标施力，同时改变线速度和角速度。"
  [body point force]
  (if (:static? body) body
      (-> body
          (update :velocity m/v+ (m/v* force (:inv-mass body)))
          (update :angular-velocity +
                  (* (:inv-inertia body) (ch/cross (m/v- point (:position body)) force))))))

(defn step-world
  ([world] (step-world world env))
  ([world settings]
   (let [[gx gy] (:gravity-vector settings [0 (:gravity settings 0.2)])
         world (if (zero? gx) world
                   (update world :bodies
                           #(mapv (fn [body]
                                    (if (:static? body) body
                                        (update body :velocity m/v+ [gx 0]))) %)))]
     (ch/rigid-step world (assoc settings :gravity gy)))))

(defn simulate-rigid [world settings frames]
  (nth (iterate #(step-world % settings) world) frames))

(defn body-scene [body] (vec (ch/body-scene body)))
(defn world-scene [world] (ch/world-scene world env))

(defn translated-shape [shape offset]
  (if (= :circle (:type shape))
    (update shape :offset m/v+ offset)
    (update shape :vertices #(mapv (fn [vertex] (m/v+ vertex offset)) %))))

(defn lollipop [id x y]
  (ch/compound-body id [x y]
                    [{:type :circle :radius 16 :offset [0 -20]}
                     (translated-shape (ch/rectangle 8 60) [0 10])]
                    1))

(defn pendulum [x y length]
  (let [anchor (assoc (ch/static-body (ch/circle-body 0 [x y] 12 1))
                      :collision? false)
        bob (ch/circle-body 1 [(+ x length) y] 12 1)]
    (assoc (make-world) :mode :pendulum :bodies [anchor bob]
           :constraints [{:a 0 :b 1 :length length :stiffness 1}])))

(defn hinge [body point]
  (let [anchor (assoc (ch/static-body (ch/circle-body 9999 point 1 1))
                      :collision? false)]
    (assoc (make-world) :bodies [body anchor]
           :constraints [{:a (:id body) :b 9999 :length 0 :stiffness 1}])))

(def event-world
  (assoc (make-world) :mode :events
         :bodies [(assoc (ch/circle-body 0 [300 120] 15 1) :velocity [2 0])
                  (assoc (ch/circle-body 1 [326 120] 15 1) :velocity [-2 0])]))

(defn make-verlet-world [] {:particles [] :springs [] :attractors []})
(defn particle [id x y radius]
  (assoc (ch/particle id [x y] false) :radius radius))
(def particles [(particle 0 320 70 8) (particle 1 440 90 8)])
(def spring-world
  {:particles (assoc-in particles [0 :locked?] true)
   :springs [(ch/spring 0 1 120 0.01)] :attractors []})

(defn step-springs
  ([world] (step-springs world env))
  ([world settings] (ch/verlet-step world settings)))

(defn simulate-springs [world settings frames]
  (nth (iterate #(step-springs % settings) world) frames))

(defn spring-scene [world] (ch/spring-scene world env))

(defn drag-particle [world id position]
  (update world :particles
          #(mapv (fn [particle]
                   (if (= id (:id particle))
                     (assoc particle :position position :previous position)
                     particle)) %)))

(defn chain [total spacing]
  (let [particles (mapv #(particle % (* % spacing) 10 4) (range total))]
    {:particles (assoc-in particles [0 :locked?] true)
     :springs (mapv #(ch/spring % (inc %) spacing 0.01) (range (dec total)))
     :attractors []}))

(defn soft-character []
  (let [positions [[200 25] [400 25] [350 125]
                   [400 225] [200 225] [250 125]]
        links [[0 1] [1 2] [2 3] [3 4] [4 5] [5 0]
               [5 2] [0 3] [1 4]]]
    {:particles (mapv (fn [id [x y]] (particle id x y 4)) (range) positions)
     :springs (mapv (fn [[a b]]
                      (ch/spring a b (m/distance (nth positions a) (nth positions b)) 0.01))
                    links)
     :attractors []}))

(defn cluster [n length]
  (let [particles (mapv (fn [id]
                          (particle id (m/random 319 321) (m/random 119 121) 4))
                        (range n))]
    {:particles particles
     :springs (vec (for [a (range n) b (range (inc a) n)]
                     (ch/spring a b length 0.01)))
     :attractors []}))

(defn attractor [position radius strength]
  {:position position :radius radius :strength strength})

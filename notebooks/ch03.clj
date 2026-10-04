;; # 第 3 章: 振荡

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch03
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch03 :as ch]
            [noc.lessons.ch03 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 0))

^{:nextjournal.clerk/no-cache true}
(do
(let [degrees 60
      radians (* degrees (/ m/pi 180))]
  (notebook/scene [[:push] [:translate 320 120] [:rotate radians]
                   [:line -60 0 60 0] [:pop]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 1))

^{:nextjournal.clerk/no-cache true}
(do
(let [angle 0.3]
  (notebook/scene
   (d/scene (d/background)
            [[:push] [:translate 320 120] [:rotate angle]]
            (d/line [-60 0] [60 0]) (d/circle [60 0] 16) (d/circle [-60 0] 16)
            [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 2))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 3))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 角位置
  ;; 角速度
  ;; 角加速度
(let [angular-step (fn [{:keys [angle angular-velocity]}]
                     (let [v (+ angular-velocity 0.0001)]
                       {:angle (+ angle v) :angular-velocity v}))
      state (nth (iterate angular-step {:angle 0 :angular-velocity 0}) 120)]
  (notebook/scene (ch/angle-scene state {:width 640 :height 240})))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 4))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(mover []
          {:position [0 0] :velocity [0 0] :acceleration [0 0] :mass 1
           :angle 0 :angular-velocity 0 :angular-acceleration 0})]
  (mover))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 5))

^{:nextjournal.clerk/no-cache true}
(do
(let [{:keys [position velocity acceleration angle angular-velocity angular-acceleration]
       :as body} lesson/mover
      v (m/v+ velocity acceleration)
      av (+ angular-velocity angular-acceleration)]
  (assoc body :velocity v :position (m/v+ position v)
         :angular-velocity av :angle (+ angle av) :acceleration [0 0]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 6))

^{:nextjournal.clerk/no-cache true}
(do
(let [{:keys [position radius angle]} lesson/mover]
  (notebook/scene
   (d/scene [[:push] (into [:translate] position) [:rotate angle]]
            (d/circle [0 0] (* radius 2) [175 175 175 200])
            (d/line [0 0] [radius 0]) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 7))

^{:nextjournal.clerk/no-cache true}
(do
(assoc lesson/mover :angular-acceleration 0.01)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 8))

^{:nextjournal.clerk/no-cache true}
(do
(assoc lesson/mover :angular-acceleration (first (:acceleration lesson/mover)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 9))

;; ## Clojure 实验 3.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.1")

(notebook/sketch "3.1")

(select-keys (:state (engine/run "3.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 10))

^{:nextjournal.clerk/no-cache true}
(do
(let [body lesson/mover
      acceleration (:acceleration body)
      velocity (m/v+ (:velocity body) acceleration)
      angular-acceleration (/ (first acceleration) 10.0)
      av (m/clamp (+ (:angular-velocity body) angular-acceleration) -0.1 0.1)]
  (assoc body :velocity velocity :position (m/v+ (:position body) velocity)
         :angular-acceleration angular-acceleration :angular-velocity av
         :angle (+ (:angle body) av) :acceleration [0 0]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 11))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：atan(y/x) 只有斜率信息；下一单元使用 atan2 处理象限和垂直方向。")

^{:nextjournal.clerk/no-cache true}
(do
(let [[vx vy] [2 1]
      angle (Math/atan (/ vy vx))]
  (notebook/scene [[:push] [:translate 320 120] [:rotate angle]
                   [:fill 175] [:rect -15 -5 30 10] [:pop]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 12))

;; ## Clojure 实验 3.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.2")

(notebook/sketch "3.2")

(select-keys (:state (engine/run "3.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 13))

^{:nextjournal.clerk/no-cache true}
(do
(let [[vx vy] [-2 1]
      angle (m/atan2 vy vx)]
  (notebook/scene [[:push] [:translate 320 120] [:rotate angle]
                   [:fill 175] [:rect -15 -5 30 10] [:pop]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 14))

^{:nextjournal.clerk/no-cache true}
(do
(let [velocity [-2 1]] (m/heading velocity))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 15))

^{:nextjournal.clerk/no-cache true}
(do
(let [r 75 theta (/ m/pi 4)]
  [(* r (m/cos theta)) (* r (m/sin theta))])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 16))

;; ## Clojure 实验 3.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.3")

(notebook/sketch "3.3")

(select-keys (:state (engine/run "3.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 17))

^{:nextjournal.clerk/no-cache true}
(do
(let [radius (* 240 0.45)
      theta (* 120 0.02)
      position (m/v* (m/from-angle theta) radius)]
  (notebook/scene
   (d/scene (d/background) [[:push] [:translate 320 120]]
            (d/line [0 0] position) (d/circle position 48) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 18))

^{:nextjournal.clerk/no-cache true}
(do
(let [theta (/ m/pi 4) radius 75
      position (m/v* (m/from-angle theta) radius)]
  (notebook/scene (d/circle (m/v+ [320 120] position) 48)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 19))

^{:nextjournal.clerk/no-cache true}
(do
(let [amplitude 100] {:amplitude amplitude :range [(- amplitude) amplitude]})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 20))

^{:nextjournal.clerk/no-cache true}
(do
(let [period 120] {:period period :frequency (/ 1.0 period)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [amplitude 100 period 120 frame 30]
  (* amplitude (m/sin (/ (* m/tau frame) period))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 22))

^{:nextjournal.clerk/no-cache true}
(do
(let [frame 30 period 120] (/ (* m/tau frame) period))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 23))

;; ## Clojure 实验 3.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.4")

(notebook/sketch "3.4")

(select-keys (:state (engine/run "3.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 24))

^{:nextjournal.clerk/no-cache true}
(do
(let [period 120 amplitude 200 frame 30
      x (* amplitude (m/sin (/ (* m/tau frame) period)))]
  (notebook/scene
   (d/scene (d/background) [[:push] [:translate 320 120]]
            (d/line [0 0] [x 0]) (d/circle [x 0] 48) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 25))

^{:nextjournal.clerk/no-cache true}
(do
(mapv (fn [frame] (* 100 (m/sin (/ (* m/tau frame) 120)))) [0 30 60 90 120])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 26))

^{:nextjournal.clerk/no-cache true}
(do
(mapv (fn [angle] (* 100 (m/sin angle))) (range 0.0 0.3 0.05))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 27))

^{:nextjournal.clerk/no-cache true}
(do
(let [angle 0 angular-velocity 0.05 amplitude 200
      next-angle (+ angle angular-velocity)]
  {:angle next-angle :x (* amplitude (m/sin next-angle))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 28))

;; ## Clojure 实验 3.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.5")

(notebook/sketch "3.5")

(select-keys (:state (engine/run "3.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 29))

^{:nextjournal.clerk/no-cache true}
(do
(let [step (fn [angle] (+ angle 0.05))
      angle (nth (iterate step 0) 30)
      x (* 200 (m/sin angle))]
  (notebook/scene
   (d/scene (d/background) [[:push] [:translate 320 120]]
            (d/line [0 0] [x 0]) (d/circle [x 0] 48) [[:pop]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 30))

;; ## Clojure 实验 3.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.6")

(notebook/sketch "3.6")

(select-keys (:state (engine/run "3.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 31))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(oscillator []
          {:angle [0 0] :velocity [(m/random -0.05 0.05) (m/random -0.05 0.05)]
           :amplitude [(m/random 20 320) (m/random 20 120)]})
        (advance [o] (update o :angle m/v+ (:velocity o)))]
  (let [o (nth (iterate advance (oscillator)) 60)
        [ax ay] (:angle o) [ampx ampy] (:amplitude o)
        point [(* ampx (m/sin ax)) (* ampy (m/sin ay))]]
    (notebook/scene
     (d/scene [[:push] [:translate 320 120]]
              (d/line [0 0] point) (d/circle point 32) [[:pop]]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 32))

^{:nextjournal.clerk/no-cache true}
(do
{:angle 0 :delta-angle 0.2 :amplitude 100}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 33))

;; ## Clojure 实验 3.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.7")

(notebook/sketch "3.7")

(select-keys (:state (engine/run "3.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 34))

^{:nextjournal.clerk/no-cache true}
(do
(notebook/scene
 (d/scene (d/background)
          (mapcat (fn [[i x]]
                    (d/circle [x (+ 120 (* 100 (m/sin (* i 0.2))))] 48 [127 127 127 127]))
                  (map-indexed vector (range 0 641 24)))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 35))

;; ## Clojure 实验 3.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.8")

(notebook/sketch "3.8")

(select-keys (:state (engine/run "3.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 36))

^{:nextjournal.clerk/no-cache true}
(do
(let [start-angle 0.6 delta-angle 0.2]
  (notebook/scene
   (d/scene (d/background)
            (mapcat (fn [[i x]]
                      (let [angle (+ start-angle (* i delta-angle))
                            y (m/map-range (m/sin angle) -1 1 0 240)]
                        (d/circle [x y] 48 [127 127 127 127])))
                    (map-indexed vector (range 0 641 24))))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 37))

^{:nextjournal.clerk/no-cache true}
(do
{:anchor [0 0] :bob [0 120] :rest-length 100}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 38))

^{:nextjournal.clerk/no-cache true}
(do
(let [k 0.1 stretch 20] {:stiffness k :spring-force (* (- k) stretch)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 39))

^{:nextjournal.clerk/no-cache true}
(do
(let [anchor [0 0] bob [0 120] rest-length 100
      direction (m/v- bob anchor) current-length (m/mag direction)]
  {:direction direction :length current-length :stretch (- current-length rest-length)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 40))

^{:nextjournal.clerk/no-cache true}
(do
(let [k 0.1 anchor [0 0] bob [0 120] rest-length 100
      direction (m/v- bob anchor) stretch (- (m/mag direction) rest-length)]
  (m/set-mag direction (* (- k) stretch)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 41))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 计算弹簧力
(let [gravity [0 1]
      spring-force (lesson/spring-force lesson/spring lesson/bob)
      next-bob (-> lesson/bob (lesson/apply-force gravity)
                   (lesson/apply-force spring-force) lesson/move-bob)]
  (notebook/scene (d/circle (:position next-bob) 48)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 42))

^{:nextjournal.clerk/no-cache true}
(do
(let [spring {:anchor [320 10] :rest-length 100 :k 0.2}
      bob (-> lesson/bob (lesson/apply-force [0 1])
              (lesson/apply-force (lesson/spring-force spring lesson/bob))
              lesson/move-bob)]
  (notebook/scene
   (d/scene (d/line (:anchor spring) (:position bob))
            (d/circle (:anchor spring) 10) (d/circle (:position bob) 48))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 43))

^{:nextjournal.clerk/no-cache true}
(do
(let [attractor {:position [320 120] :mass 20}
      body (assoc lesson/bob :position [100 100])
      direction (m/v- (:position attractor) (:position body))
      distance (m/clamp (m/mag direction) 5 25)
      force (m/set-mag direction (/ (* (:mass attractor) (:mass body)) (* distance distance)))]
  (lesson/apply-force body force))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 44))

^{:nextjournal.clerk/no-cache true}
(do
(let [force (lesson/spring-force lesson/spring lesson/bob)]
  (lesson/apply-force lesson/bob force))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 45))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 计算
(letfn [(connect [spring bob]
          (lesson/apply-force bob (lesson/spring-force spring bob)))]
  (connect lesson/spring lesson/bob))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 46))

;; ## Clojure 实验 3.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.9")

(notebook/sketch "3.9")

(select-keys (:state (engine/run "3.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 47))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(spring [x y length] {:anchor [x y] :rest-length length :k 0.2})
        (connect [{:keys [anchor rest-length k]} bob]
          (let [direction (m/v- (:position bob) anchor)
                stretch (- (m/mag direction) rest-length)
                force (m/set-mag direction (* (- k) stretch))]
            (lesson/apply-force bob force)))]
  (let [s (spring 320 10 100) bob (connect s lesson/bob)]
    {:spring s :bob bob :force (lesson/spring-force s lesson/bob)}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 48))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(constrain-length [bob anchor minimum maximum]
          (let [direction (m/v- (:position bob) anchor)
                length (m/mag direction)]
            (if (<= minimum length maximum)
              bob
              (assoc bob :position (m/v+ anchor (m/set-mag direction (m/clamp length minimum maximum)))
                     :velocity [0 0]))))]
  (constrain-length (assoc lesson/bob :position [320 350]) [320 10] 30 200))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 49))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 摆臂长度
  ;; 当前角度
  ;; 角速度
  ;; 角加速度
(letfn [(pendulum [length angle]
          {:length length :angle angle :angular-velocity 0 :angular-acceleration 0})]
  (pendulum 175 (/ m/pi 4)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 50))

^{:nextjournal.clerk/no-cache true}
(do
(let [state {:length 175 :angle (/ m/pi 4) :angular-velocity 0}
      angular-acceleration (/ (* -0.4 (m/sin (:angle state))) (:length state))
      velocity (+ (:angular-velocity state) angular-acceleration)]
  (assoc state :angular-acceleration angular-acceleration
         :angular-velocity velocity :angle (+ (:angle state) velocity)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 51))

^{:nextjournal.clerk/no-cache true}
(do
{:pivot [100 10] :length 125}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 52))

^{:nextjournal.clerk/no-cache true}
(do
(let [r 125 angle (/ m/pi 4)] [(* r (m/sin angle)) (* r (m/cos angle))])
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 53))

^{:nextjournal.clerk/no-cache true}
(do
(let [pivot [100 10] r 125 angle (/ m/pi 4)
      bob [(* r (m/sin angle)) (* r (m/cos angle))]]
  (m/v+ bob pivot))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 54))

^{:nextjournal.clerk/no-cache true}
(do
(let [pivot [100 10] bob (m/v+ pivot [(* 125 (m/sin 0.7)) (* 125 (m/cos 0.7))])]
  (notebook/scene (d/scene (d/line pivot bob) (d/circle bob 16 [127 127 127]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 55))

^{:nextjournal.clerk/no-cache true}
(do
(update {:angular-velocity 0.05} :angular-velocity * 0.99)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 56))

;; ## Clojure 实验 3.10

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.10")

(notebook/sketch "3.10")

(select-keys (:state (engine/run "3.10" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 57))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 支点位置
  ;; 重物位置
  ;; 摆臂长度
  ;; 摆臂角度
  ;; 角速度
  ;; 角加速度
  ;; 阻尼
  ;; 重物半径
(let [env (assoc lesson/env :width 640 :height 240 :length 175 :damping 0.99)
      initial {:pivot [320 0] :angle (/ m/pi 4) :angular-velocity 0 :dragging? false}
      state (nth (iterate #(ch/pendulum-step % env) initial) 120)]
  (notebook/scene (ch/pendulum-scene state env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch03" 58))

;; ## Clojure 实验 3.11

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "3.11")

(notebook/sketch "3.11")

(select-keys (:state (engine/run "3.11" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "3.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 3.1 · examples/03_oscillation/exercise_3_1_baton

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/exercise_3_1_baton"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/exercise_3_1_baton"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/exercise_3_1_baton") 12)))

;; ### Exercise 3.3 · examples/03_oscillation/example_1_10_accelerating_towards_the_mouse

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/example_1_10_accelerating_towards_the_mouse"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/example_1_10_accelerating_towards_the_mouse"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/example_1_10_accelerating_towards_the_mouse") 12)))

;; ### Exercise 3.5 · examples/03_oscillation/exercise_3_5_spiral

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/exercise_3_5_spiral"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/exercise_3_5_spiral"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/exercise_3_5_spiral") 12)))

;; ### Exercise 3.6 · examples/03_oscillation/exercise_3_6_asteroids

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/exercise_3_6_asteroids"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/exercise_3_6_asteroids"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/exercise_3_6_asteroids") 12)))

;; ### Exercise 3.6 · examples/03_oscillation/example_3_5_simple_harmonic_motion

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/example_3_5_simple_harmonic_motion"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/example_3_5_simple_harmonic_motion"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/example_3_5_simple_harmonic_motion") 12)))

;; ### Exercise 3.9 · examples/03_oscillation/example_3_9_the_wave

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave") 12)))

;; ### Example 3.8: Static Wave · examples/03_oscillation/example_3_9_the_wave_a

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_a"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_a"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_a") 12)))

;; ### Example 3.8: Static Wave · examples/03_oscillation/example_3_9_the_wave_b

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_b"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_b"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_b") 12)))

;; ### Example 3.8: Static Wave · examples/03_oscillation/example_3_9_the_wave_c

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_c"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_c"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/example_3_9_the_wave_c") 12)))

;; ### Exercise 3.11 · examples/03_oscillation/exercise_3_11_oop_wave

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/exercise_3_11_oop_wave"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/exercise_3_11_oop_wave"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/exercise_3_11_oop_wave") 12)))

;; ### Exercise 3.12 · examples/03_oscillation/exercise_3_12_additive_wave

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/exercise_3_12_additive_wave"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/exercise_3_12_additive_wave"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/exercise_3_12_additive_wave") 12)))

;; ### Exercise 3.15 · examples/03_oscillation/exercise_3_15_double_pendulum

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/03_oscillation/exercise_3_15_double_pendulum"))

(notebook/sketch (notebook/companion-id "examples/03_oscillation/exercise_3_15_double_pendulum"))

(:_frame (:state (engine/run (notebook/companion-id "examples/03_oscillation/exercise_3_15_double_pendulum") 12)))

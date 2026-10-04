;; # 第 4 章: 粒子系统

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns notebooks.ch04
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [noc.book :as book] [noc.notebook :as notebook]
            [noc.engine :as engine] [noc.registry :as registry]
            [noc.math :as m] [noc.draw :as d] [noc.learning :as learning]
            [noc.chapters.ch04 :as ch]
            [noc.lessons.ch04 :as lesson]))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(notebook/links [["index.clj" "返回目录"]])

;; 中文正文沿用原书术语；下列教学代码均以 Clojure 数据和函数实现对应操作。
;; 类与可变对象改为映射值与返回新状态的函数，向量运算使用 noc.math，绘图由 Quil 执行。

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 0))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这个主程序是不是既简单又漂亮?
(let [initial (lesson/emitter [320 20])
      frame (fn [system] (-> system lesson/add-particle lesson/update-emitter))
      system (nth (iterate frame initial) 60)]
  (notebook/scene (lesson/emitter-scene system)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 1))

^{:nextjournal.clerk/no-cache true}
(do
  ;; Particle 对象只是 mover 的另一个名字.
  ;; 它具有位置, 速度和加速度.
(letfn [(particle [x y]
          {:position [x y] :velocity [0 0] :acceleration [0 0]})
        (advance [p]
          (let [velocity (m/v+ (:velocity p) (:acceleration p))]
            (assoc p :velocity velocity :position (m/v+ (:position p) velocity)
                   :acceleration [0 0])))]
  (let [p (advance (assoc (particle 320 20) :acceleration [0 0.1]))]
    (notebook/scene (d/circle (:position p) 8 [175 175 175]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 2))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 记录粒子已经 "存活" 多久.
  ;; 从 255 开始并逐渐倒数到 0.
  ;; 生命周期递减.
  ;; 生命周期范围也是 255 到 0, 因而可直接用作 alpha.
(letfn [(particle [x y]
          {:position [x y] :velocity [0 0] :acceleration [0 0] :lifespan 255})
        (advance [p]
          (let [v (m/v+ (:velocity p) (:acceleration p))]
            (-> p (assoc :velocity v :position (m/v+ (:position p) v))
                (update :lifespan - 2))))]
  (let [p (nth (iterate advance (particle 320 20)) 60)
        [x y] (:position p)]
    (notebook/scene [[:stroke 0 (:lifespan p)] [:fill 175 (:lifespan p)] [:circle x y 8]])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 3))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(dead? [particle] (if (neg? (:lifespan particle)) true false))]
  (mapv dead? [{:lifespan 255} {:lifespan 0} {:lifespan -1}]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 4))

^{:nextjournal.clerk/no-cache true}
(do
(let [dead? (fn [particle] (neg? (:lifespan particle)))]
  (mapv dead? [{:lifespan 255} {:lifespan 0} {:lifespan -1}]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 5))

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 6))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 为了演示, 粒子具有随机初速度.
  ;; 沿用前面章节中的物理模型.
(let [origin [320 20]
      advance (fn [particle]
                (let [p (-> particle (lesson/apply-force [0 0.1]) lesson/update-particle)]
                  (if (lesson/dead? p) (lesson/particle origin) p)))
      particle (nth (iterate advance (lesson/particle origin)) 140)]
  (notebook/scene (lesson/particle-scene particle)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 7))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 从空数组开始.
  ;; 使用索引和方括号访问数组元素.
(let [total 10 particles (mapv (fn [_] (lesson/particle [320 120])) (range total))
      next-particles (mapv lesson/update-particle particles)]
  (notebook/scene (d/scene (mapcat lesson/particle-scene next-particles))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 8))

^{:nextjournal.clerk/no-cache true}
(do
(let [particles lesson/samples]
  (mapv (fn [particle] (lesson/update-particle particle)) particles))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 9))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每次执行 draw() 都向数组加入一个新 Particle 对象.
(let [frame (fn [particles]
              (mapv lesson/update-particle (conj particles (lesson/particle [320 20]))))
      particles (nth (iterate frame []) 30)]
  (notebook/scene (d/scene (d/background) (mapcat lesson/particle-scene particles))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 10))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：不可变向量先统一更新再筛选，删除不会跳过下一粒子。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 把数组元素赋给变量, 提高可读性.
  ;; 移除索引 i 处的粒子.
(let [updated (mapv lesson/update-particle lesson/samples)]
  (filterv (complement lesson/dead?) updated))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 11))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：只遍历帧开始时的粒子快照；新粒子从下一帧更新，避免遍历中无限增长。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; 在遍历期间向列表加入新粒子.
(let [particles lesson/samples
      updated (mapv lesson/update-particle particles)
      newborn (mapv (fn [_] (lesson/particle [320 20])) particles)]
  (into updated newborn))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 12))

^{:nextjournal.clerk/no-cache true}
(do
(reduce (fn [survivors particle]
          (let [p (lesson/update-particle particle)]
            (if (lesson/dead? p) survivors (conj survivors p))))
        [] lesson/samples)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 13))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 保留尚未死亡的粒子.
(filterv (fn [particle] (not (lesson/dead? particle))) lesson/samples)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 14))

^{:nextjournal.clerk/no-cache true}
(do
(filterv (complement lesson/dead?) lesson/samples)
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 15))

;; ## Clojure 实验 4.1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.1")

(notebook/sketch "4.1")

(select-keys (:state (engine/run "4.1" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 16))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 为了安全删除元素, 反向遍历数组.
(let [frame (fn [particles]
              (->> (conj particles (lesson/particle [320 20]))
                   (mapv lesson/update-particle)
                   (filterv (complement lesson/dead?))))
      particles (nth (iterate frame []) 140)]
  (notebook/scene (d/scene (d/background) (mapcat lesson/particle-scene particles))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 17))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 只有一个粒子发射器.
(let [emitter (lesson/emitter [320 20])
      emitter (-> emitter lesson/add-particle lesson/update-emitter)]
  (notebook/scene (lesson/emitter-scene emitter)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 18))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(emitter [] {:origin [320 20] :particles []})
        (add-particle [system]
          (update system :particles conj (lesson/particle (:origin system))))
        (run [system]
          (update system :particles
                  #(filterv (complement lesson/dead?) (mapv lesson/update-particle %))))]
  (-> (emitter) add-particle run))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 19))

;; ## Clojure 实验 4.2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.2")

(notebook/sketch "4.2")

(select-keys (:state (engine/run "4.2" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 20))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 此实现包含一个所有粒子开始运动的原点.
  ;; 原点在粒子加入数组时传递给粒子.
(letfn [(emitter [x y] {:origin [x y] :particles []})
        (add-particle [{:keys [origin] :as system}]
          (update system :particles conj (lesson/particle origin)))]
  (add-particle (emitter 320 20)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 21))

^{:nextjournal.clerk/no-cache true}
(do
(let [initial (lesson/emitter [320 20])
      frame #(-> % lesson/add-particle lesson/update-emitter)
      emitter (nth (iterate frame initial) 60)]
  (notebook/scene (lesson/emitter-scene emitter)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 22))

;; ## Clojure 实验 4.3

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.3")

(notebook/sketch "4.3")

(select-keys (:state (engine/run "4.3" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 23))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这次数组中保存的对象本身就是粒子发射器.
  ;; 每次点击鼠标, 创建新的 Emitter 并加入数组.
  ;; emitters 中没有元素被删除, 因此可以使用 for...of.
(let [clicks [[180 40] [320 60] [460 40]]
      emitters (mapv lesson/emitter clicks)
      frame #(mapv (fn [e] (-> e lesson/update-emitter lesson/add-particle)) %)
      emitters (nth (iterate frame emitters) 60)]
  (notebook/scene
   (d/scene (d/background) (mapcat #(mapcat lesson/particle-scene (:particles %)) emitters))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 24))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(confetti [kind] {:kind kind :position [320 20] :lifespan 255})]
  (mapv confetti [:happy :fun :wacky]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 25))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 随机选择一种粒子.
(let [choose-particle (fn []
                        (let [r (m/random)]
                          (assoc (lesson/particle [320 20])
                                 :kind (cond (< r 0.33) :happy (< r 0.67) :fun :else :wacky))))]
  {:particles (vec (repeatedly 10 choose-particle))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 26))

^{:nextjournal.clerk/no-cache true}
(do
{:happy-particles [] :fun-particles [] :wacky-particles []}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 27))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(dog [] {:kind :dog :age 0})
        (eat [_] "Yum!") (sleep [_] "Zzzzzz") (bark [_] "WOOF")]
  (let [d (dog)] {:animal d :actions [(eat d) (sleep d) (bark d)]}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 28))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 狗和猫共享许多相同变量, 例如 age.
  ;; 猫没有 bark(), 而有独特的 meow().
(letfn [(cat [] {:kind :cat :age 0})
        (eat [_] "Yum!") (sleep [_] "Zzzzzz") (meow [_] "MEOW!")]
  (let [c (cat)] {:animal c :actions [(eat c) (sleep c) (meow c)]}))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 29))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：共享数据构造函数和通用行为，用 :kind 分派特有行为，表达继承与多态。")

^{:nextjournal.clerk/no-cache true}
(do
  ;; Animal 是父类, 也称超类.
  ;; Dog 和 Cat 会继承 age.
  ;; Dog 和 Cat 会继承 eat() 和 sleep().
  ;; Dog 是子类, extends Animal 表示继承 Animal.
  ;; super() 执行父类构造函数中的代码.
  ;; bark() 只定义在子类中, 因为它不属于通用 Animal.
(letfn [(animal [kind] {:age 0 :kind kind})
        (eat [_] "Yum!") (sleep [_] "Zzzzzz")
        (voice [a] (case (:kind a) :dog "WOOF!" :cat "MEOW!"))]
  (mapv (fn [a] {:animal a :actions [(eat a) (sleep a) (voice a)]})
        [(animal :dog) (animal :cat)]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 30))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 子类可以引入父类中不存在的新变量.
(letfn [(dog [] (assoc (lesson/animal :dog) :hair-color [210 105 30]))]
  (dog))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 31))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 子类可以在需要时覆盖父类方法.
(letfn [(eat [animal]
          (if (= :dog (:kind animal)) "Woof! Woof! Slurp." "Yum!"))]
  (mapv eat [(lesson/animal :dog) (lesson/animal :cat)]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 32))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 调用 Animal 中的 eat().
  ;; Dog 特有的额外行为.
(letfn [(animal-eat [_] "Yum!")
        (dog-eat [dog] [(animal-eat dog) "Woof!!!"])]
  (dog-eat (lesson/animal :dog)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 33))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每种动物使用独立数组.
(let [animals (into {} (for [[kind n] [[:dog 10] [:cat 15] [:turtle 6] [:kiwi 98]]]
                        [kind (vec (repeat n (lesson/animal kind)))]))]
  (into {} (map (fn [[kind xs]] [kind (count xs)]) animals)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 34))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 每种动物分别循环.
(let [dogs (repeat 10 (lesson/animal :dog))
      cats (repeat 15 (lesson/animal :cat))
      turtles (repeat 6 (lesson/animal :turtle))
      kiwis (repeat 98 (lesson/animal :kiwi))]
  {:dogs (mapv lesson/eat dogs) :cats (mapv lesson/eat cats)
   :turtles (mapv lesson/eat turtles) :kiwis (mapv lesson/eat kiwis)})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 35))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 一个数组保存所有动物.
  ;; 一个循环处理全部动物.
(let [kingdom (vec (concat (repeat 10 (lesson/animal :dog))
                           (repeat 15 (lesson/animal :cat))
                           (repeat 6 (lesson/animal :turtle))
                           (repeat 98 (lesson/animal :kiwi))))]
  (mapv lesson/eat kingdom))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 36))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(particle [x y]
          {:position [x y] :velocity [(m/random -1 1) (m/random -2 0)]
           :acceleration [0 0] :lifespan 255 :mass 1 :kind :circle :angle 0})
        (update-particle [p]
          (let [v (m/v+ (:velocity p) (:acceleration p))]
            (-> p (assoc :velocity v :position (m/v+ (:position p) v) :acceleration [0 0])
                (update :lifespan - 2))))
        (apply-force [p force] (update p :acceleration m/v+ force))]
  (let [p (-> (particle 320 20) (apply-force [0 0.1]) update-particle)]
    (notebook/scene (lesson/particle-scene p))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 37))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 这里可以加入只属于 Confetti 的变量.
  ;; update() 等其他方法从父类继承.
  ;; 覆盖 show() 方法.
(letfn [(confetti [x y] (assoc (lesson/particle [x y]) :kind :square))]
  (let [p (lesson/update-particle (confetti 320 20))]
    (notebook/scene (lesson/particle-scene p))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 38))

^{:nextjournal.clerk/no-cache true}
(do
(let [position [160 20] width 640]
  (m/map-range (first position) 0 width 0 (* m/tau 2)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 39))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 在 p5.js 中旋转形状需要变换.
  ;; 参见 https://thecodingtrain.com/transformations.
(let [{:keys [position lifespan]} (first lesson/samples)
      [x y] position angle (m/map-range x 0 640 0 (* m/tau 2))]
  (notebook/scene [[:stroke 0 lifespan] [:fill 0 lifespan] [:push]
                   [:translate x y] [:rotate angle] [:rect -6 -6 12 12] [:pop]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 40))

;; ## Clojure 实验 4.4

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.4")

(notebook/sketch "4.4")

(select-keys (:state (engine/run "4.4" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 41))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 一个列表可以保存 Particle 或任何扩展 Particle 的对象.
  ;; 两种粒子各有 50% 概率被加入.
(let [origin [320 20]
      add-particle (fn [system]
                     (let [p (assoc (lesson/particle origin)
                                    :kind (if (< (m/random) 0.5) :circle :square))]
                       (update system :particles conj p)))
      frame #(-> % add-particle lesson/update-emitter)
      emitter (nth (iterate frame (lesson/emitter origin)) 60)]
  (notebook/scene (lesson/emitter-scene emitter)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 42))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 加入质量属性. 可以为不同粒子使用不同质量.
  ;; 按质量缩放作用力.
(let [particle (assoc (lesson/particle [320 20]) :mass 2)
      force [0 0.1]]
  (update particle :acceleration m/v+ (m/vdiv force (:mass particle))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 43))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 创建硬编码向量并作为力施加.
(let [p (-> (lesson/particle [320 20])
            (lesson/apply-force [0 0.05]) lesson/update-particle)]
  (notebook/scene (lesson/particle-scene p)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 44))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 向所有粒子施加一个力?
(let [emitter (-> (lesson/emitter [320 20]) lesson/add-particle lesson/update-emitter)]
  (notebook/scene (lesson/emitter-scene emitter)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 45))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 向发射器施加力.
(let [emitter (-> (lesson/emitter [320 20]) lesson/add-particle
                  (lesson/force-emitter [0 0.1]) lesson/update-emitter)]
  (notebook/scene (lesson/emitter-scene emitter)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 46))

^{:nextjournal.clerk/no-cache true}
(do
(let [force [0 0.1]]
  (mapv (fn [particle] (lesson/apply-force particle force)) lesson/samples))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 47))

;; ## Clojure 实验 4.5

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.5")

(notebook/sketch "4.5")

(select-keys (:state (engine/run "4.5" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 48))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 对所有粒子施加同一个力.
  ;; 因为需要检查并删除粒子, 不能使用 for...of.
(let [frame (fn [emitter]
              (-> emitter (lesson/force-emitter [0 0.1])
                  lesson/add-particle lesson/update-emitter))
      emitter (nth (iterate frame (lesson/emitter [320 20])) 90)]
  (notebook/scene (lesson/emitter-scene emitter)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 49))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 新增: 声明 Repeller 对象.
  ;; 新增: 初始化 Repeller 对象.
  ;; 新增: 应用排斥器产生的力.
  ;; 新增: 绘制 Repeller 对象.
(let [repeller {:position [300 120] :power 150}
      frame #(-> % lesson/add-particle (lesson/force-emitter [0 0.1])
                  (lesson/repel-emitter repeller) lesson/update-emitter)
      emitter (nth (iterate frame (lesson/emitter [320 50])) 100)]
  (notebook/scene
   (d/scene (lesson/emitter-scene emitter) (d/circle (:position repeller) 32 [127 127 127]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 50))

^{:nextjournal.clerk/no-cache true}
(do
  ;; Repeller 不移动, 只需要位置.
  ;; 用 power 而不是 mass 缩放排斥力.
(letfn [(repeller [x y] {:position [x y] :power 150})]
  (let [r (repeller 320 120)]
    (notebook/scene (d/circle (:position r) 32 [127 127 127]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 51))

^{:nextjournal.clerk/no-cache true}
(do
(let [repeller {:position [320 200] :power 150} force [0 0.1]]
  (mapv (fn [particle]
          (-> particle (lesson/apply-force force)
              (lesson/apply-force (lesson/repel repeller particle))))
        lesson/samples))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 52))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 步骤 1: 计算力的方向.
  ;; 步骤 2: 获取并约束距离.
  ;; 步骤 3: 使用 power 计算力的大小.
  ;; 步骤 4: 把方向与大小组合为向量.
(let [repeller {:position [320 200] :power 150}
      particle (first lesson/samples)
      direction (m/v- (:position repeller) (:position particle))
      distance (m/clamp (m/mag direction) 5 50)
      strength (/ (- (:power repeller)) (* distance distance))]
  (m/set-mag direction strength))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 53))

;; ## Clojure 实验 4.6

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.6")

(notebook/sketch "4.6")

(select-keys (:state (engine/run "4.6" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 54))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 一个粒子发射器
  ;; 一个排斥器
  ;; 直接施加一个 p5.Vector 力.
  ;; 根据排斥器为每个粒子分别计算力.
(let [repeller {:position [320 200] :power 150}
      step (fn [emitter]
             (-> emitter lesson/add-particle
                 (lesson/force-emitter [0 0.1])
                 (lesson/repel-emitter repeller) lesson/update-emitter))
      emitter (nth (iterate step (lesson/emitter [320 20])) 120)]
  (notebook/scene
   (d/scene (lesson/emitter-scene emitter) (d/circle (:position repeller) 32 [127 127 127]))))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 55))

;; ## Clojure 实验 4.7

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.7")

(notebook/sketch "4.7")

(select-keys (:state (engine/run "4.7" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 56))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：纹理作为资产路径保存在数据中，由 Quil 画布加载。")

^{:nextjournal.clerk/no-cache true}
(do
{:texture "images/texture.png"}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 57))

^{:nextjournal.clerk/no-cache true}
(do
(let [texture "images/texture.png"]
  {:path texture :available? (some? (clojure.java.io/resource "public/js/texture.png"))})
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 58))

^{:nextjournal.clerk/no-cache true}
(do
  ;; tint() 对图像的作用类似于 fill() 对形状的作用.
(let [particle {:position [320 120] :lifespan 150}
      [x y] (:position particle)]
  (notebook/scene [[:background 0] [:tint 255 (:lifespan particle)]
                   [:image "images/texture.png" (- x 16) (- y 16) 32 32] [:no-tint]]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 59))

^{:nextjournal.clerk/no-cache true}
(do
(let [vx (m/gaussian 0 0.3) vy (m/gaussian -1 0.3)]
  (assoc (lesson/particle [320 192]) :velocity [vx vy]))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 60))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 风力方向由 mouseX 决定.
(let [mouse-x 480 wind [(m/map-range mouse-x 0 640 -0.2 0.2) 0]
      env {:width 640 :height 240 :mouse [mouse-x 120] :clicked? false
           :emission 2 :gravity 0 :wind 0.2 :repulsion 150}
      state (nth (iterate #(ch/step % env) (ch/setup :texture env)) 100)]
  (notebook/scene (ch/scene state env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 61))

;; ## Clojure 实验 4.8

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.8")

(notebook/sketch "4.8")

(select-keys (:state (engine/run "4.8" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 62))

^{:nextjournal.clerk/no-cache true}
(do
  ;; 使用加法混合.
  ;; 加法混合不会像普通背景那样覆盖旧内容, 因此先 clear().
  ;; 白色或过亮背景无法表现加法混合的发光效果.
  ;; 每帧加入三个粒子, 进一步增强叠加效果.
(let [env {:width 640 :height 240 :mouse [480 120] :clicked? false
           :emission 3 :gravity 0 :wind 0.2 :repulsion 150}
      state (nth (iterate #(ch/step % env) (ch/setup :additive env)) 100)]
  (notebook/scene (ch/scene state env)))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 63))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "Clojure 适配：三维画布配置保存在数据中；本书共享示例将三维位置投影到 notebook 的二维 Quil 画布。")

^{:nextjournal.clerk/no-cache true}
(do
{:canvas {:width 640 :height 240 :renderer :webgl}}
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 64))

^{:nextjournal.clerk/no-cache true}
(do
(letfn [(add-particle [emitter]
          (update emitter :particles conj (lesson/particle (:origin emitter))))]
  (add-particle (lesson/emitter [320 20])))
)

^{:nextjournal.clerk/visibility {:code :hide} :nextjournal.clerk/no-cache true}
(clerk/md (book/prose-part "ch04" 65))

;; ## Clojure 实验 4.9

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation "4.9")

(notebook/sketch "4.9")

(select-keys (:state (engine/run "4.9" 120))
             [:_frame :generation :age :position :velocity :samples :collisions :accuracy])

;; # 本章 Clojure + Quil 可执行示例

;; 画布中的参数即时传入模型；改变种群数、网格尺寸等初始参数后点击重置。
;; JVM 单元展示普通数据，可编辑帧数、参数，或传入 `:input-fn` 重现鼠标操作。

;; ## 共享 Clojure 实现源码

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/source "4.1")

;; ## 本章随附练习与补充演示

;; ### Exercise 4.4 · examples/04_particles/exercise_4_4_asteroids

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/exercise_4_4_asteroids"))

(notebook/sketch (notebook/companion-id "examples/04_particles/exercise_4_4_asteroids"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/exercise_4_4_asteroids") 12)))

;; ### Exercise 4.4 · examples/04_particles/4_4_multiple_emitters_0

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/4_4_multiple_emitters_0"))

(notebook/sketch (notebook/companion-id "examples/04_particles/4_4_multiple_emitters_0"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/4_4_multiple_emitters_0") 12)))

;; ### Exercise 4.4 · examples/04_particles/4_4_emitters_1

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/4_4_emitters_1"))

(notebook/sketch (notebook/companion-id "examples/04_particles/4_4_emitters_1"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/4_4_emitters_1") 12)))

;; ### Exercise 4.4 · examples/04_particles/4_4_emitters_2

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/4_4_emitters_2"))

(notebook/sketch (notebook/companion-id "examples/04_particles/4_4_emitters_2"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/4_4_emitters_2") 12)))

;; ### Exercise 4.6 · examples/04_particles/exercise_4_6_particle_shatter

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/exercise_4_6_particle_shatter"))

(notebook/sketch (notebook/companion-id "examples/04_particles/exercise_4_6_particle_shatter"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/exercise_4_6_particle_shatter") 12)))

;; ### Exercise 4.10 · examples/04_particles/figure_4_8_circles

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/figure_4_8_circles"))

(notebook/sketch (notebook/companion-id "examples/04_particles/figure_4_8_circles"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/figure_4_8_circles") 12)))

;; ### Exercise 4.10 · examples/04_particles/figure_4_8_image

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/figure_4_8_image"))

(notebook/sketch (notebook/companion-id "examples/04_particles/figure_4_8_image"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/figure_4_8_image") 12)))

;; ### Exercise 4.12 · examples/04_particles/exercise_4_12_particle_textures_array

^{:nextjournal.clerk/visibility {:code :hide}}
(notebook/explanation (notebook/companion-id "examples/04_particles/exercise_4_12_particle_textures_array"))

(notebook/sketch (notebook/companion-id "examples/04_particles/exercise_4_12_particle_textures_array"))

(:_frame (:state (engine/run (notebook/companion-id "examples/04_particles/exercise_4_12_particle_textures_array") 12)))

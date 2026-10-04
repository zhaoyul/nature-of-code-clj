(ns noc.math
  "Portable vector arithmetic and reproducible random sources used throughout the book."
  (:refer-clojure :exclude [abs]))

(def pi #?(:clj Math/PI :cljs js/Math.PI))
(def tau (* 2 pi))
(defn abs [x] (#?(:clj Math/abs :cljs js/Math.abs) (double x)))
(defn sqrt [x] (#?(:clj Math/sqrt :cljs js/Math.sqrt) x))
(defn sin [x] (#?(:clj Math/sin :cljs js/Math.sin) x))
(defn cos [x] (#?(:clj Math/cos :cljs js/Math.cos) x))
(defn atan2 [y x] (#?(:clj Math/atan2 :cljs js/Math.atan2) y x))
(defn floor [x] (#?(:clj Math/floor :cljs js/Math.floor) x))
(defn exp [x] (#?(:clj Math/exp :cljs js/Math.exp) x))
(defn log [x] (#?(:clj Math/log :cljs js/Math.log) x))
(defn pow [x y] (#?(:clj Math/pow :cljs js/Math.pow) x y))
(defn clamp [x lo hi] (max lo (min hi x)))
(defn lerp [a b t] (+ a (* (- b a) t)))
(defn map-range [x a b c d] (if (= a b) c (lerp c d (/ (- x a) (- b a)))))

(defn v+ [& vs] (reduce (fn [[x y] [a b]] [(+ x a) (+ y b)]) [0.0 0.0] vs))
(defn v- [[x y] [a b]] [(- x a) (- y b)])
(defn v* [[x y] k] [(* x k) (* y k)])
(defn vdiv [v k] (if (zero? k) [0.0 0.0] (v* v (/ 1.0 k))))
(defn dot [[x y] [a b]] (+ (* x a) (* y b)))
(defn mag-sq [v] (dot v v))
(defn mag [v] (sqrt (mag-sq v)))
(defn distance [a b] (mag (v- a b)))
(defn normalize [v] (vdiv v (mag v)))
(defn set-mag [v n] (v* (normalize v) n))
(defn limit [v n] (if (> (mag-sq v) (* n n)) (set-mag v n) v))
(defn heading [[x y]] (atan2 y x))
(defn from-angle [a] [(cos a) (sin a)])
(defn rotate [[x y] a] [(- (* x (cos a)) (* y (sin a))) (+ (* x (sin a)) (* y (cos a)))])
(defn wrap [[x y] w h] [(mod x w) (mod y h)])

(def ^:dynamic *rng* (atom 42))
(defn seeded [seed] (atom (inc (mod (long seed) 2147483646))))
(defn random
  ([] (/ (double (swap! *rng* #(mod (* % 48271) 2147483647))) 2147483647.0))
  ([high] (* high (random)))
  ([low high] (+ low (random (- high low)))))
(defn random-int [n] (long (floor (random n))))
(defn choose [xs] (nth xs (random-int (count xs))))
(defn random-vector [] (from-angle (random tau)))
(defn gaussian
  ([] (* (sqrt (* -2 (log (max 1.0e-12 (random))))) (cos (* tau (random)))))
  ([mean sd] (+ mean (* sd (gaussian)))))

;; Gradient Perlin noise. All corner gradients are deterministic, including
;; negative coordinates; the quintic fade gives continuous first derivatives.
(defn- fade [t] (* t t t (+ (* t (- (* t 6) 15)) 10)))
(defn- gradient [x y]
  (let [seed (mod (+ (* x 1619) (* y 31337) 6971) 2147483647)
        h (mod (mod (* seed 48271) 2147483647) 8)]
    (nth [[1 1] [-1 1] [1 -1] [-1 -1] [1 0] [-1 0] [0 1] [0 -1]] h)))
(defn noise
  ([x] (noise x 0.37))
  ([x y]
   (let [ix (long (floor x)) iy (long (floor y))
         tx (- x ix) ty (- y iy) u (fade tx) v (fade ty)
         n00 (dot (gradient ix iy) [tx ty])
         n10 (dot (gradient (inc ix) iy) [(- tx 1) ty])
         n01 (dot (gradient ix (inc iy)) [tx (- ty 1)])
         n11 (dot (gradient (inc ix) (inc iy)) [(- tx 1) (- ty 1)])]
     (clamp (+ 0.5 (* 0.5 (lerp (lerp n00 n10 u) (lerp n01 n11 u) v))) 0 1))))

(defn bounce
  "Move a point and reflect velocity at the rectangular bounds."
  [[x y] [vx vy] w h r]
  {:position [(clamp x r (- w r)) (clamp y r (- h r))]
   :velocity [(if (or (< x r) (> x (- w r))) (- vx) vx)
              (if (or (< y r) (> y (- h r))) (- vy) vy)]})

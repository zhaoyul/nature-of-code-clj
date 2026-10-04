(ns noc.algorithms-test
  (:require [clojure.test :refer [deftest is testing]]
            [noc.math :as m]
            [noc.learning :as l]
            [noc.chapters.ch07 :as ca]
            [noc.chapters.ch08 :as fractals]
            [noc.chapters.ch10 :as neural]
            [noc.chapters.extras08 :as extras]))

(deftest elementary-rule-90
  (testing "规则 90 等价于左右邻居异或"
    (is (= [0 0 1 0 1 0 0] (ca/elementary-next [0 0 0 1 0 0 0] 90)))
    (is (= [0 1 0 0 0 1 0] (ca/elementary-next [0 0 1 0 1 0 0] 90)))))

(deftest life-blinker
  (let [horizontal [[0 0 0 0 0] [0 0 0 0 0] [0 1 1 1 0] [0 0 0 0 0] [0 0 0 0 0]]
        vertical [[0 0 0 0 0] [0 0 1 0 0] [0 0 1 0 0] [0 0 1 0 0] [0 0 0 0 0]]]
    (is (= vertical (ca/life-next horizontal 5 5)))
    (is (= horizontal (ca/life-next vertical 5 5)))))

(deftest koch-growth
  (doseq [generation (range 6)]
    (let [segments (fractals/koch-lines generation)
          total-length (reduce + (map (fn [[a b]] (m/distance a b)) segments))
          expected (* 620 (m/pow (/ 4.0 3) generation))]
      (is (= (long (m/pow 4 generation)) (count segments)))
      (is (< (m/abs (- expected total-length)) 1.0e-7)))))

(deftest l-system-simultaneous-replacement
  (is (= ["A" "AB" "ABA" "ABAAB" "ABAABABA"]
         (vec (take 5 (iterate #(fractals/rewrite-sentence % {"A" "AB" "B" "A"}) "A")))))
  (testing "二维和三维海龟保持每条线段长度"
    (is (every? #(< (m/abs (- 4 (apply m/distance %))) 1.0e-8)
                (fractals/turtle-lines "F[+FF]-F" 4 0.5)))
    (is (every? (fn [[a b]]
                  (let [length (m/sqrt (reduce + (map #(m/pow (- %1 %2) 2) a b)))]
                    (< (m/abs (- 4 length)) 1.0e-8)))
                (extras/turtle3d "F[&F^F]<F>F" 4 0.5)))))

(deftest genetic-operators
  (binding [m/*rng* (m/seeded 42)]
    (is (= [1 2 3] (l/mutate-genes [1 2 3] 0 (constantly 9))))
    (is (= [9 9 9] (l/mutate-genes [1 2 3] 1 (constantly 9))))
    (is (every? #{1 2} (l/crossover [1 1 1 1] [2 2 2 2])))
    (is (contains? #{:a :b} (l/weighted-choice [:a :b] (constantly 0))))
    (let [brain (l/brain 3 5 2) child (l/crossover-brain brain (l/brain 3 5 2))]
      (is (= (count (l/brain-genes brain)) (count (l/brain-genes child))))
      (is (= [5 2] [(count (:w1 child)) (count (:w2 child))])))))

(deftest gesture-network-learns-original-eight-samples
  (binding [m/*rng* (m/seeded 42)]
    (let [initial (l/brain 2 8 4)
          trained (nth (iterate #(neural/gesture-epoch % 0.6) initial) 400)]
      (is (< (neural/gesture-loss trained) (* 0.1 (neural/gesture-loss initial))))
      (doseq [{:keys [inputs label]} neural/gesture-data]
        (is (= label (l/best-index (l/predict trained (neural/normalized-input inputs)))))))))

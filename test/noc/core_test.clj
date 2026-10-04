(ns noc.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io] [clojure.string :as str]
            [noc.book :as book] [noc.catalog :as catalog]
            [noc.engine :as engine] [noc.math :as m] [noc.registry :as registry]
            [noc.chapters.ch02 :as forces] [noc.chapters.ch06 :as physics]))

(defn finite-data? [value]
  (every? #(or (not (number? %)) (Double/isFinite (double %)))
          (tree-seq coll? seq value)))

(deftest original-translation-integrity
  (is (= "17a1f48e9d96631c5fb2d1ebeed629623d0bd71c979aa820bb45ef5a8f78ab18" (book/source-sha256)))
  (is (= 19 (count book/sections)))
  (let [images (filter #(.isFile %) (file-seq (io/file "resources/translations/the-nature-of-code/images")))
        refs (re-seq #"\[\[file:images/([^\]]+)\]\]" (book/original-org))]
    (is (= 385 (count images)))
    (is (= 387 (count refs)))
    (doseq [[_ filename] refs]
      (is (.exists (io/file "resources/translations/the-nature-of-code/images" filename)) filename)))
  (doseq [{:keys [id]} book/sections]
    (is (= (book/markdown id) (apply str (map :markdown (book/markdown-parts id)))) id)
    (is (not (str/includes? (book/markdown id) "../images/")) id)
    (let [filename (if (= id "index") "book_index" (str/replace id "-" "_"))
          notebook (io/file "notebooks" (str filename ".clj"))]
      (is (.exists notebook) (str "Missing translated section: " id))
      (is (str/includes? (slurp notebook)
                         (str (if (book/clojure-section? id) "(book/prose-part " "(book/part ")
                              (pr-str id) " "))
          (str "Notebook must include its translated section: " id))))
  (is (str/includes? (slurp "notebooks/index.clj") "\"book_index.clj\" \"索引\"")))

(deftest complete-example-coverage
  (is (= 99 (count registry/numbered-examples)))
  (is (= 67 (count registry/companion-examples)))
  (is (= 166 (count (set (map :id registry/examples)))))
  (is (= (set (map :id (:examples catalog/book))) (set (map :id registry/numbered-examples))))
  (is (= (set (map :directory (:companions catalog/book))) (set (map :directory registry/companion-examples)))))

(deftest every-example-executes
  (doseq [example registry/examples]
    (testing (str (:id example) " " (:title example))
      (let [result (engine/run (:id example) 120)
            commands (:scene result)]
        (is (= 120 (get-in result [:state :_frame])))
        (is (finite-data? (:state result)))
        (is (finite-data? commands))
        (is (seq commands))
        (is (every? #(and (vector? %) (keyword? (first %))) commands))
        (doseq [path (:assets example)]
          (is (.exists (io/file "public/js" path)) path))))))

(deftest reproducible-randomness
  (doseq [id ["0.1" "0.2" "0.5" "0.6" "1.9" "4.5" "9.1" "10.2" "11.2"]]
    (is (= (engine/run id 50 {:seed 123}) (engine/run id 50 {:seed 123})) id))
  (is (not= (:state (engine/run "0.1" 100 {:seed 42})) (:state (engine/run "0.1" 100 {:seed 43})))))

(deftest vector-laws
  (is (= [0.0 0.0] (m/normalize [0 0])))
  (is (< (m/abs (- 1 (m/mag (m/normalize [3 4])))) 1.0e-12))
  (is (= [4.0 6.0] (m/v+ [1 2] [3 4])))
  (is (< (m/abs (- 2 (m/mag (m/limit [3 4] 2)))) 1.0e-12)))

(deftest sampling-distributions
  (let [uniform (get-in (engine/run "0.2" 10000) [:state :counts])
        biased (get-in (engine/run "0.5" 10000) [:state :counts])
        n (count biased)
        approximate-mean (/ (reduce + (map-indexed #(* (/ (+ %1 0.5) n) %2) biased)) 10000)]
    (is (= 10000 (reduce + uniform)))
    (is (< (- (apply max uniform) (apply min uniform)) 200))
    (is (< 0.63 approximate-mean 0.70)))
  (is (< (m/abs (- (m/noise 1.0 0.3) (m/noise 1.00001 0.3))) 0.0001)))

(deftest force-and-momentum-laws
  (let [small (forces/apply-force (forces/body [0 0] 1) [2 3])
        large (forces/apply-force (forces/body [0 0] 2) [2 3])]
    (is (= [2.0 3.0] (:acceleration small)))
    (is (= [1.0 1.5] (:acceleration large))))
  (let [result (engine/run "2.9" 300 {:seed 99 :count 8})
        initial (engine/run "2.9" 0 {:seed 99 :count 8})
        momentum #(reduce m/v+ [0 0] (map (fn [b] (m/v* (:velocity b) (:mass b))) (get-in % [:state :bodies])))]
    (is (< (m/distance (momentum initial) (momentum result)) 1.0e-10))))

(deftest physics-contacts-and-constraints
  (let [a (physics/box-body 0 [100 100] 20 20 1)
        b (physics/box-body 1 [115 100] 20 20 1)
        c (physics/box-body 2 [150 100] 20 20 1)]
    (is (some? (physics/collision a b)))
    (is (nil? (physics/collision a c))))
  (let [a (physics/particle 0 [100 100] true)
        b (physics/particle 1 [230 100] false)
        world {:particles [a b] :springs [(physics/spring 0 1 80 1)] :attractors []}
        result (physics/verlet-step world {:width 640 :height 360 :gravity 0 :iterations 5
                                          :pressed? false :mouse [0 0]})]
    (is (zero? (m/distance [100 100] (get-in result [:particles 0 :position]))))
    (is (< (m/abs (- 80 (m/distance (get-in result [:particles 0 :position])
                                      (get-in result [:particles 1 :position])))) 1.0e-8))))

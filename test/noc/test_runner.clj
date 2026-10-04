(ns noc.test-runner
  (:require [clojure.test :as test] [noc.core-test] [noc.algorithms-test]
            [noc.physics-test] [noc.book-rendering-test] [noc.lesson-test]))

(defn -main [& _]
  (let [result (test/run-tests 'noc.core-test 'noc.algorithms-test 'noc.physics-test
                               'noc.book-rendering-test 'noc.lesson-test)]
    (shutdown-agents)
    (when (pos? (+ (:fail result) (:error result))) (System/exit 1))))

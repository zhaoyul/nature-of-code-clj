(ns noc.browser-check
  "Dedicated browser integration-check entrypoint, bundled with the sketches."
  (:refer-clojure :exclude [run!])
  (:require [noc.browser :as browser]))

(defn ^:export run! [] (browser/verify-all!))

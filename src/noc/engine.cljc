(ns noc.engine
  "The same finite-state simulation runs on the JVM and in a Quil canvas."
  (:require [noc.math :as m] [noc.registry :as registry]))

(defn defaults [example]
  (merge {:width (or (:width example) 640) :height (or (:height example) 360)
          :seed 42 :mouse [320 180] :pressed? false :clicked? false :frame 0}
         (into {} (map (fn [[k v]] [k (:default v)]) (:params example)))))

(defn config [example overrides] (merge (defaults example) overrides))

(defn setup [example config]
  (let [rng (m/seeded (:seed config 42))]
    (binding [m/*rng* rng]
      (assoc ((:setup example) config) :_rng @rng :_frame 0))))

(defn step [example state env]
  (let [rng (atom (:_rng state 42))]
    (binding [m/*rng* rng]
      (assoc ((:step example) state (assoc env :frame (:_frame state 0)))
             :_rng @rng :_frame (inc (:_frame state 0))))))

(defn scene [example state env]
  (binding [m/*rng* (atom (:_rng state 42))]
    (vec ((:scene example) state (assoc env :frame (:_frame state 0))))))

(defn run
  "Evaluate an example for a finite number of frames; no windows or timers.
   Supply :input-fn in options to script mouse/key interactions."
  ([id frames] (run id frames {}))
  ([id frames overrides]
   (when-not (and (integer? frames) (<= 0 frames 100000))
     (throw (ex-info "frames must be an integer between 0 and 100000" {:frames frames})))
   (let [example (registry/example id)
         env (config example (dissoc overrides :input-fn))
         input-fn (or (:input-fn overrides) (constantly {}))
         state (reduce (fn [state frame]
                         (step example state (merge env (input-fn frame))))
                       (setup example env) (range frames))]
     {:id (:id example) :frames frames :config env :state state
      :scene (scene example state env)})))

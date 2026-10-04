(ns noc.draw
  "Small, data-only drawing helpers. Quil interprets these commands in the notebook.")

(def ink [39 52 69])
(def accent [218 57 108])
(def teal [24 153 145])
(def paper [250 249 246])
(defn circle
  ([position diameter] (circle position diameter accent))
  ([[x y] diameter color]
   [[:stroke 39 52 69] (into [:fill] color) [:circle x y diameter]]))
(defn line
  ([a b] (line a b ink))
  ([[x y] [a b] color] [(into [:stroke] color) [:line x y a b]]))
(defn label
  ([s] (label s 12 24))
  ([s x y] [[:no-stroke] (into [:fill] ink) [:text (str s) x y]]))
(defn background [] [(into [:background] paper)])
(defn scene [& groups] (vec (mapcat identity groups)))
(defn arrow [[x y] [vx vy] color]
  (let [ex (+ x vx) ey (+ y vy)]
    [(into [:stroke] color) [:line x y ex ey]
     [:circle ex ey 4]]))

(ns user)

(defn start []
  ((requiring-resolve 'noc.dev/start!)))

(defn reset []
  ((requiring-resolve 'noc.dev/reload!)))

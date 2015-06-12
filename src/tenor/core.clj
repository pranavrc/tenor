(ns tenor.core
  (:use [overtone.live]
        [overtone.inst.piano]))

(defn play-chord [a-chord]
  (doseq [note a-chord] (piano note)))

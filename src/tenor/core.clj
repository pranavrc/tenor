(ns tenor.core
  (:use [overtone.live]
        [overtone.inst.piano]
        [tenor.constructs]
        [tenor.melody]
        [tenor.harmony]))

(defn play-note [instrument note]
 (instrument note))

(defn play-chord [instrument chord]
  (doseq [note chord]
    (play-note instrument note)))


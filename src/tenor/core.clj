(ns tenor.core
  (:use [overtone.live]
        [overtone.inst.piano]
        [tenor.constructs]))

(defn play-note [instrument note]
 (instrument note))

(defn play-chord [instrument note]
  (doseq [note (chord (find-note-name note) (rand-nth (keys SCALE)))]
    (play-note instrument note)))


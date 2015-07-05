(ns tenor.core
  (:use [overtone.live]
        [overtone.inst.piano]
        [tenor.constructs]
        [tenor.melody]
        [tenor.harmony]))

(defn play-note-with-instrument [instrument note]
 (instrument note))

(defn play-chord-with-instrument [instrument chord]
  (doseq [note chord]
    (play-note-with-instrument instrument note)))


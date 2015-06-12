(ns tenor.core)

(defprotocol NoteEntities
  (note-value [s] "1, 2, 4, 8, 16")
  (pitch-letter [s] "A, B, C, D, E, F, G")
  (octave [s] "1, 2, 3, 4, 5, 6 etc."))

(defrecord Note [notation]
  NoteEntities
  (note-value [this] (- (int (get notation 0)) (int \0)))
  (pitch-letter [this] (str (get notation 1)))
  (octave [this] (- (int (get notation 2)) (int \0))))

(defn time-signature [beat-count & [sig]]
  (cond
    (> beat-count 3)
    (let [beats (rand-nth [2 3 4])
          sig (concat (or sig '()) (range 1 (inc beats)))]
      (time-signature (- beat-count beats) sig))
    (and (<= beat-count 3) (> beat-count 0))
    (let [sig (concat (or sig '()) (range 1 (inc beat-count)))]
      (time-signature 0 sig))
    :else sig))

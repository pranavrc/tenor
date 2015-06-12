(ns tenor.core)

(defprotocol NoteEntities
  (note-value [s] "1, 2, 4, 8, 16")
  (pitch-letter [s] "A, B, C, D, E, F, G")
  (octave [s] "1, 2, 3, 4, 5, 6 etc.)"))

(defrecord Note [notation]
  NoteEntities
  (note-value [this] (- (int (get notation 0)) (int \0)))
  (pitch-letter [this] (str (get notation 1)))
  (octave [this] (- (int (get notation 2)) (int \0))))

(defn time-signature [beats note-value]
  ())

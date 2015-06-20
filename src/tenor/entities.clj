(ns tenor.entities
  (:use [tenor.constructs]))

(defprotocol Entity
  (repr [this])
  (generated [this]))

(defrecord Note [pitch-letter octave]
  Entity
  (repr [this] (make-note (str pitch-letter octave))))

(defrecord Beat [note-count note-value]
  Entity
  (generated [this] (segment-beat note-count note-value)))

(defrecord Measure [note-count note-value]
  Entity
  (generated [this] (map-measure
                      (segment-measure (time-signature note-count))
                      (generate-random-scale))))


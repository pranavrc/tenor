(ns tenor.entities)

(defprotocol Entity
  (repr [this])
  (generated [this]))

(defrecord Note [note-value pitch-letter octave]
  Entity
  (repr [this] (keyword (str pitch-letter octave)))
)

(defn make-note [notation]
  (let [notation-re #"^(1|2|4|8|16)([A-G])([#b]?)([1-9])$"
        components (re-matches notation-re notation)]
    (if components
      (->Note
        (Integer. (nth components 1))
        (str (nth components 2) (nth components 3))
        (Integer. (nth components 4)))
      (throw (Exception. "Invalid notation")))))

(defn time-signature [note-count & [sig]]
  (cond
    (> note-count 3)
    (let [beat (rand-nth [2 3 4])
          sig (conj (or sig '()) beat)]
      (time-signature (- note-count beat) sig))
    (and (<= note-count 3) (> note-count 0))
    (let [sig (conj (or sig '()) note-count)]
      (time-signature 0 sig))
    :else sig))

(defn segment-beat [note-count note-value]
  (let [sixteenth-count (* (/ 16 note-value) note-count)
        notes (range 1 (inc sixteenth-count))]
    (filter
      (fn [x] (or (= 1 x) (rand-nth [true false])))
      notes)))

(defrecord Beat [note-count note-value]
  Entity
  (generated [this] (segment-beat note-count note-value)))

(defrecord Measure [note-count note-value]
  Entity
  (generated [this] (time-signature note-count)))

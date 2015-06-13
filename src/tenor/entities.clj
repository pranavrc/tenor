(ns tenor.entities)

(defrecord Note [note-value pitch-letter octave])

(defn make-note [notation]
  (let [notation-re #"^(1|2|4|8|16)([A-G])([#b]?)([1-9])$"
        components (re-matches notation-re notation)]
    (if components
      (->Note
        (Integer. (nth components 1))
        (str (nth components 2) (nth components 3))
        (Integer. (nth components 4)))
      "Invalid notation")))

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

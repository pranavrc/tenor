;; tenor/melody.clj
;; Core functions to create the melody.

(ns tenor.melody
  (:use [overtone.live]))

;; --- Melody using intervals --- ;;

(defn perfect-unison [degree]
  "Identical notes. No interval jump."
  (+ 0 degree))

(defn up-step [degree]
  "One step up in the scale (Whole note or half, depending on degree)."
  (inc degree))

(defn down-step [degree]
  "One step down in the scale (Whole note or half, depending on degree)."
  (dec degree))

(defn up-leap [degree]
  "One leap up in the scale (More than 2 semitones)."
  (+ (rand-nth [2 3 4 5 6]) degree))

(defn down-leap [degree]
  "One leap down in the scale (More than 2 semitones)."
  (- (rand-nth [2 3 4 5 6]) degree))

(defn up-octave [degree]
  "One perfect octave up (Example: C3 to C4)."
  (+ 7 degree))

(defn down-octave [degree]
  "One perfect octave down (Example: C4 to C3)."
  (- 7 degree))

(defn weighted-random-interval-jumps [scale degree weights]
  "Generate an interval jump using weighted random selection,
  from the current note until another compatible note is reached.
  The chances of steps, leaps and octave transitions vary."
  (let [movements '(perfect-unison up-step down-step up-leap down-leap up-octave down-octave)
        current-move (weighted-choose movements weights)
        temp-degree ((resolve current-move) degree)]
    (if (and (> temp-degree 0) (>= (count scale) temp-degree))
      temp-degree
      (weighted-random-interval-jumps scale temp-degree weights))))

(defn conjunct-motion [scale degree]
  "Melodic motion where steps are more likely to occur than leaps."
  (weighted-random-interval-jumps scale degree '(0.06 0.35 0.35 0.08 0.08 0.04 0.04)))

(defn disjunct-motion [scale degree]
  "Melodic motion where leaps are more likely to occur than steps."
  (weighted-random-interval-jumps scale degree '(0.06 0.08 0.08 0.30 0.30 0.09 0.09)))


;; tenor/constructs.clj
;; Primitive components of music theory.

(ns tenor.constructs
  (:use [overtone.live]))


;; --- Notes --- ;;

(defn make-note [notation]
  "Take pitch letter + octave notation
  and generate an overtone note keyword."
  (let [notation-re #"^([A-G])([#b]?)([1-9])$"
        components (re-matches notation-re notation)]
    (if components
      (keyword (reduce str (rest components)))
      (throw (Exception. "Invalid notation")))))

;; --- Time Signature --- ;;

(defn generate-meter [beat-count & [sig]]
  "Decompose a measure into a random meter of beats,
  within the time signature's note count."
  (cond
    (> beat-count 3)
    (let [beat (rand-nth [2 3 4])
          sig (conj (or sig '()) beat)]
      (generate-meter (- beat-count beat) sig))
    (and (<= beat-count 3) (> beat-count 0))
    (let [sig (conj (or sig '()) beat-count)]
      (generate-meter 0 sig))
    :else sig))

;; --- Beats --- ;;;

(defn segment-beat [beat-count note-value
                    & {:keys [sparseness]
                       :or {sparseness 1}}]
  "Break a beat down into sixteenth-note positions and
  assign notes and rests to each position by iteration."
  (let [sixteenth-count (* (/ 16 note-value) beat-count)
        notes (range 1 (inc sixteenth-count))
        distribution (cons true (repeat sparseness false))]
    (filter
      (fn [x] (or (= 1 x) (rand-nth distribution)))
      notes)))

;; --- Scales --- ;;;

(defn generate-random-scale []
  "Generate a random scale (Example - :E3 :Dorian)."
  (let [scale-type (rand-nth (keys SCALE))
        ninth-octave-count 131
        note-name (find-note-name (rand-int ninth-octave-count))]
    (scale note-name scale-type)))

;; --- Measures --- ;;;

(defn segment-measure [measure
                       & {:keys [running-count result note-value sparseness]
                          :or {running-count 0 result '()
                               note-value 16 sparseness 1}}]
  "Segment a measure into beats and further segment the beats,
  essentially breaking the measure down into sixteenth-notes."
  (if (empty? measure)
    result
    (let [result (concat result (map #(+ running-count %)
                                     (segment-beat (first measure)
                                                   note-value
                                                   :sparseness sparseness)))
          running-count (+ running-count (* (/ 16 note-value) (first measure)))]
      (segment-measure (rest measure)
                       :running-count running-count
                       :result result
                       :note-value note-value
                       :sparseness sparseness))))

(defn string-measures [measures beat-count
                       & {:keys [running-string running-count]
                          :or {running-string '() running-count 0}}]
  "String multiple measures together into a single piece."
  (if (empty? measures)
    running-string
    (let [running-string (concat
                           running-string
                           (map #(+ running-count %) (first measures)))
          running-count (+ running-count beat-count)]
      (string-measures (rest measures)
                       beat-count
                       :running-string running-string
                       :running-count running-count))))

;; --- Intervals --- ;;

(defn construct-intervals [construct-melody scale note-count
                           & {:keys [running-interval degree]
                              :or {running-interval '()
                                   degree 1}}]
  "Build an interval-representation of the melody
  by traversing through the scale in steps/leaps/octave jumps."
  (if (<= note-count 0)
    running-interval
    (let [degree (construct-melody scale degree)
          running-interval (concat running-interval (list degree))
          note-count (dec note-count)]
      (construct-intervals construct-melody
                           scale
                           note-count
                           :degree degree
                           :running-interval running-interval))))

(defn generate-intervals [construct-melody scale note-count]
  "Start from the first note, construct intervals,
  end at the first or last note."
  (let [mid-intervals (construct-intervals construct-melody scale (- note-count 2))
        scale-count (count scale)]
    (concat '(1)
            mid-intervals
            (list (rand-nth `(1 ~scale-count))))))

(defn intervals->notes [intervals scale]
  "Convert intervals to notes in a scale."
  (map #(nth scale (dec %)) intervals))

;; --- Musical piece --- ;;

(defn map-entity [entity intervals]
  "Create a list of hash-maps of positions as keys
  and musical notes as values."
  (map #(hash-map :pos %1, :note %2) entity intervals))

(defn generate-rhythm [measure-count beat-count
                      & {:keys [note-value sparseness]
                         :or {note-value 16 sparseness 1}}]
  "Generate a rhythm with measure-count measures,
  each of time signature with beat-count and note-value."
  (let [time-sig (generate-meter beat-count)
        segmented (segment-measure time-sig note-value sparseness)
        rhythm (repeat measure-count segmented)]
    (string-measures rhythm beat-count)))

(defn generate-entity-map [construct-melody measure-count beat-count
                            & {:keys [note-value sparseness scale]
                               :or {note-value 16
                                    sparseness 1
                                    scale (generate-random-scale)}}]
  "Take a function to construct a melody, measure count, note count and note value,
  and generate a measure map of the melody and rhythm using map-entity."
  (let [rhythm (generate-rhythm measure-count beat-count note-value sparseness)
        scale-intervals (intervals->notes
                          (generate-intervals construct-melody scale (count rhythm)) scale)]
  (map-entity rhythm scale-intervals)))

;; --- Chords --- ;;

(defn chordify [entity-map construct-harmony]
  "Map an external procedure that constructs chords from notes,
  to the entire map of notes and positions."
  (filter (fn [x] (not (nil? x)))
          (map #(if (weighted-coin 0.33)
                  (construct-harmony (:pos %) (:note %))) entity-map)))

;; --- Playback --- ;;

(defmacro construct-note [time player entity]
  "Take time, instrument and note entity as arguments,
  and return overtone code that plays the note."
  `(list 'at ~time (list ~player ~entity)))

(defmacro construct-piece [entity-maps base-time player pivot-time]
  "Take an entity map and run construct-note on each
  note, creating a list of overtone playback code components."
  `(map #(construct-note (+ ~pivot-time (* (:pos %) ~base-time))
                         ~player (:note %))
        ~entity-maps))

(defn play-piece [entity-maps base-time player
                    & {:keys [pivot-time] :or {pivot-time (now)}}]
  "Play the constructed piece with current time as start time."
  `(let [time# ~pivot-time]
     ~@(construct-piece entity-maps base-time player pivot-time)))

;; --- Multiple voices --- ;;

(defn generate-parallel-voices [& body]
  "Run multiple pieces concurrently using pmap."
  (pmap #(eval %) `(list ~@body)))


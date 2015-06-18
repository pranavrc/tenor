(ns tenor.construct)

(defonce ninth-octave-count 131)

(defn generate-random-scale []
  (let [scale-type (rand-nth (keys SCALE))
        note-name (find-note-name (rand-int ninth-octave-count))]
    (scale note-name scale-type)))

(defn map-measure [measure scale]
  (map #(hash-map :pos %, :note (rand-nth scale)) measure))

(defmacro construct-note [time player entity]
  `(list 'at ~time (list ~player ~entity)))

(defmacro construct-measure [measure-maps
                             & {:keys [base-time player]
                                :or {base-time 1000 player piano}}]
  (let [measure-seq '()]
    (doseq [measure-map measure-maps]
      (conj measure-seq
            `(construct-note (* (:pos ~measure-map) ~base-time)
                             player (:note ~measure-map))))))

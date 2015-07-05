(ns tenor.test.constructs
  (:use [tenor.constructs]
        [tenor.melody]
        [tenor.harmony]
        [clojure.test]))

(defn is-sorted [a-list]
  (apply <= a-list))

(deftest test-make-note
  (is (= (make-note "F4") :F4)))

(deftest test-generate-meter
  (let [meter (generate-meter 11)]
    (is (= (reduce + meter) 11))))

(deftest test-segment-beat
  (let [segmented-beat (segment-beat 4 4)]
    (is (is-sorted segmented-beat))
    (is (<= (last segmented-beat) 16))))

(deftest test-generate-random-scale
  (let [random-scale (generate-random-scale)]
    (is (is-sorted random-scale))
    (is (= 8 (count random-scale)))))

(deftest test-segment-measure
  (let [segmented-measure (segment-measure [1 4 4 3])]
    (is (is-sorted segmented-measure))
    (is (<= (last segmented-measure) 12))))

(deftest test-string-measures
  (let [strung-measures (string-measures '((1 2 3) (1 3 4)) 4)]
    (is (is-sorted strung-measures))
    (is (= '(1 2 3 5 7 8) strung-measures))))

(deftest test-generate-intervals
  (let [generated-intervals (generate-intervals
                              conjunct-motion
                              (scale :a3 :minor) 8)]
    (is (= 8 (count generated-intervals)))
    (is (= 1 (first generated-intervals)))
    (is (<= (last (sort generated-intervals)) 8))))

(deftest test-intervals->notes
  (let [notes (intervals->notes '(1 2 3) (scale :a3 :minor))]
    (is (= '(57 59 60) notes))))

(deftest test-generate-rhythm
  (let [generated-rhythm (generate-rhythm 4 4)]
    (is (is-sorted generated-rhythm))
    (is (<= (last generated-rhythm) 16))))

(deftest test-generate-entity-map
  (let [entity-map (generate-entity-map conjunct-motion 1 2)]
    (is (= 1 (:pos (first entity-map))))))


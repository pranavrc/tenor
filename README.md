# Tenor

The concept of music is incredibly accommodating; anything from industrial noise to baroque classical can be appreciated with enough interest. This makes the problem statement simple - all we need is a random rhythm and melody generator and we could pass off its output as music.

But popular and contemporary music has tuned the ears of the average listener to certain preset patterns in melody, harmony and rhythm. As a result, most of us have developed a collective sense of what makes good music and what doesn't, while in fact, there is no such thing as good or bad music (objectively, of course). Now this makes the problem statement a lot more tangible - generating music is easy, generating music that *sounds good to the human ear* requires more effort.

The project is an attempt at constructing primitive music theory and using it as a base to build procedures that can generate melodies, in-tune to the ears of the average listener. Most of the heavy lifting is done by [Clojure](http://clojure.org/) and [Overtone](http://overtone.github.io/); we just build simple abstractions to represent concepts and patterns in music theory and use them to generate musical pieces.

This ~~article~~ \**cough*\* essay will hopefully serve as a primer in music theory whilst also describing its construction, and consequently the generation of music, in Clojure.

***

## Contents

- [Music to the human ears](#music-to-the-human-ears)
- [Rhythm](#rhythm)
  - [Time Signature](#time-signature)
  - [Popular songs in non-4/4 signature](#popular-songs-in-non-44-signature)
  - [Representing measures](#representing-measures)
  - [Meters](#meters)
  - [Generating a measure in 11/4](#generating-a-measure-in-114)
  - [Sparseness of a measure](#sparseness-of-a-measure)
  - [Stringing measures together](#stringing-measures-together)
  - [TL;DR Rhythm](#tldr-rhythm)
- [Melody](#melody)
  - [Notes](#notes)
  - [Octaves](#octaves)
  - [Semitones and Whole tones](#semitones-and-whole-tones)
  - [Scales](#scales)
  - [Intervals - unison, steps and leaps](#intervals---unison-steps-and-leaps)
  - [Melodic motion - conjunct and disjunct](#melodic-motion---conjunct-and-disjunct)
  - [Weighted random interval jumps](#weighted-random-interval-jumps)
  - [Simulating melodic motion](#simulating-melodic-motion)
  - [TL;DR Melody](#tldr-melody)
- [Combining Melody with Rhythm](#combining-melody-with-rhythm)
- [Harmony](#harmony)
  - [Chords and Arpeggios](#chords-and-arpeggios)
  - [*Chordifying* a musical piece](#chordifying-a-musical-piece)
- [Macroland - Music as Code and Code as Music](#macroland---music-as-code-and-code-as-music)
  - [Playing notes in Overtone](#playing-notes-in-overtone)
  - [Generating code for our musical piece](#generating-code-for-our-musical-piece)

***

### Music to the human ears

Most pieces in contemporary music share certain common characteristics in rhythm, harmony and melody. There's a high chance that one or more of these characteristics occur in a song that we happen to discover, and since we're attuned to these patterns already, the melody then becomes instantly identifiable without sounding like random noise.

Some of these idiosyncrasies could be:

- The famed [4/4 time signature](https://en.wikipedia.org/wiki/Time_signature#Most_frequent_time_signatures).
- The [call-and-response](https://en.wikipedia.org/wiki/Call_and_response_%28music%29#Popular_music) pattern.
- Phil Specter's [Wall of Sound](https://en.wikipedia.org/wiki/Wall_of_Sound).
- The [C-major scale](https://en.wikipedia.org/wiki/C_major).
- Variations of the C-D-G-C chord pattern or other common chord patterns.

Here's a [very interesting analysis](http://www.hooktheory.com/blog/i-analyzed-the-chords-of-1300-popular-songs-for-patterns-this-is-what-i-found/) on common patterns found in over 1300 popular songs. One way of generating listenable music would be to create procedures that conform to some of these (though not exactly these) norms.

***

### Rhythm

Rhythm is, perhaps, the most accessible and instantly decipherable component of a song because it lays out the fundamental structure over which melody and harmony interplay. A single rhythmic theme (say, the 4/4) also repeats fairly often in a song, so each individual measure in a musical piece need not be procedurally generated.

##### Time Signature

A [time signature](https://en.wikipedia.org/wiki/Time_signature) governs how many beats (or foot-taps) there are in a measure, and the duration of each beat. A signature in 4/4 simply means that we repeatedly tap 4 times in quarter-note (1/4 - its length depends on the length of the full note) intervals. Here, each tap is a beat and 4 taps make a measure (the repeating basic pattern of the song). Here's how we'd tap our feet to *Comfortably Numb* by Pink Floyd (a popular 4/4 song), for instance:

```
Hello Hel - lo Hello - (Rest) Is there - anybody 
Tap       - Tap      - Tap             - Tap

In there? - (Rest) - (Rest) Just - nod
Tap       - Tap    - Tap         - Tap
```

Let's take the first beat ('Hello Hel'), and decompose the quarter note (1/4) into sixteenth notes (1/16), which means we're gonna split the beat further into four other beats. Now, if we tap to *these* beats (we'd have to tap really fast), the quarter beat will look like this.

```
Hel - lo  - (rest)  - Hel
Tap - Tap - Tap     - Tap
```

This tells us that in a measure decomposed into sixteenth beats (assuming we do not cross over into 1/32 and further), notes and rests are, to an extent, randomly interspersed. To reaffirm this, we can look at *Smoke on the Water* by Deep Purple (another popular 4/4 song). Let's take the first measure (remember, 4 foot taps, each of quarter-note length):

```
Smoooo - ke on the - Waaate - rrrrrrr
Tap    - Tap       - Tap    - Tap
```

First beat, sixteenth notes:

```
Smo - oo  - oo  - oo
Tap - Tap - Tap - Tap
```

If 'Hello Hel' was `1-1-0-1` (note-note-rest-note), then 'Smoooo..' is `1-1-1-1` because there are no rests. We can observe the `1-1-1-1` decomposition for the first beat in other songs like *Eleanor Rigby* by The Beatles (just the dragged out 'I' syllable).

##### Popular songs in non-4/4 signature

*Happy Birthday* is in 3/4 time. Let's do the tap routine for the first four measures:

```
(Rest) - (Rest) - Happy
Tap    - Tap    - Tap

birth  - day    - to
Tap    - Tap    - Tap

you    - (Rest) - Happy
Tap    - Tap    - Tap

birth  - day    - to
Tap    - Tap    - Tap
```

*Money* by Pink Floyd is in 7/4 time.

```
Money - (Rest) - (Rest) - (Rest) - (Rest) - (Rest) - (Rest)
Tap   - Tap    - Tap    - Tap    - Tap    - Tap    - Tap

Get A - way    - (Rest) - (Rest) - (Rest) - (Rest) - (Rest)
Tap   - Tap    - Tap    - Tap    - Tap    - Tap    - Tap

Get a - good   - job    - with   - more   - pay'n  - you're
Tap   - Tap    - Tap    - Tap    - Tap    - Tap    - Tap

Ooo   - ohkaa  - aay    - (Rest) - (Rest) - (Rest) - (Rest)
Tap   - Tap    - Tap    - Tap    - Tap    - Tap    - Tap
```

*Four Sticks* by Led Zeppelin uses 5/4 in parts, *Paranoid Android* by Radiohead uses 7/8 in parts, the *Mission Impossible theme* by Lalo Schifrin uses 5/4.

##### Representing measures

Sixteenth beats give us enough granularity to represent positions in the measure where notes are placed. A 4/4 measure, decomposed into sixteenth beats, would look like this in positional notation:

```
(1-2-3-4)  -  (5-6-7-8)  -  (9-10-11-12) -  (13-14-15-16)
1/4 Beat 1 -  1/4 Beat 2 -  1/4 Beat 3   -  1/4 Beat 4
```

Any of these numbers from 1 to 16 could be notes or rests. For the sake of representation, let's ignore the rests (there can only be notes and rests, so if a number is missing, we know it's a rest). Let's go back to the *Comfortably Numb* example, whose first beat we decomposed as `1-1-0-1`. In our new notation of a measure, this would be `1-2-4` (3 is a rest, so it's ignored). Decomposing the other beats of the measure in the old beat-wise and new positional notations, we get:

```
Beat-wise                    |    Positional
-----------------------------|---------------------------------
                             |
1   -  1    -  0  -  1       |    1   -   2   -   4
Hel -  lo   -  _  -  Hel     |    Hel -   lo  -   Hel
                             |
1   -  0    -  1  -  1       |    5   -   7   -   8
lo  -  _    - Hel -  lo      |    lo  -   Hel -   lo
                             |
0   -  0    -  1  -  1       |    11  -  12
_   -  _    -  Is -  there   |    Is  -  there
                             |
1   -  1    -  1  -  1       |    13  - 14   - 15 - 16
any -  body -  in -  there?  |    any - body - in - there?
```

Ignoring the rests, this becomes `(1-2-4) - (5-7-8) - (11-12) - (13-14-15-16)` in our new positional notation. Let's put these together into a list:

```
[1 2 4 5 7 8 11 12 13 14 15 16]
```

We just represented the first rhythmic measure of *Comfortably Numb* in an ordered list. Similarly for *Smoke on the Water*, this is:

```
[1 2 3 4 7 8 9 10 11 12 13 14 15 16]
```

As we can see, only the 5th and 6th sixteenth-beats (immediately after 'Smooooke' and before 'on the') are rests, so the first measure of *Smoke on the Water* turns out to be densely populated with notes.

##### Meters

So far, we only looked at a measure as having beats of the same type (a `4/4` measure had 4 quarter-beats), but this is not an overarching rule for constructing measures.

A [meter](https://en.wikipedia.org/wiki/Meter_%28music%29) tells us how to *count* or *accentuate* beats in a measure. For instance, a measure in 11/4 time signature could be split into `2-2-3-2-2` or `4-3-2-2`, a measure in 4/4 time signature could be split into `2-2`, `3-1`, or so on. These are still comprised of quarter-beats, but the meter simply groups beats together to emphasize stress on certain groups of beats and de-emphasize stress on others. A measure in 11/4 can be counted in any of the following ways:

```
 one-two - one-two - one-two-three - one-two - one-two
 one-two-three-four - one-two-three - one-two - one-two
 one-two-three-four - one-two-three-four - one-two-three
```
 
The grouping could simply mean that the *one* beats are more emphasized (preferably using notes), than the others (could contain notes or rests).

For example, the song *Flower Punk* by Frank Zappa, a song with interchanging 5/4 and 7/4 measures, can be counted using the following meter:

```
one-two - one-two-three
one-two - one-two - one-two-three
one-two - one-two-three
one-two - one-two - one-two-three
```

We can count the *Mission Impossible* theme using the following meter:

```
one-two-three - one-two-three - one-two - one-two
```

##### Generating a measure in 11/4

So far, we've looked at decomposing a measure into a meter and further into a list of sixteenth-beats. The meter helps in singling out sixteenth-beat positions within the measure which *must* contain notes for emphasis, and each of the other sixteenth-beat positions could either contain a note or a rest.

The [generate-meter](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L21) function takes a beat count for a measure, and generates a random meter. Here are example meters for the 11/4 and 4/4 time signatures:

```
user=> (generate-meter 11)
(1 4 3 3)
```

This is the `one - one-two-three-four - one-two-three - one-two-three` counting pattern where all the ones *must* contain notes and not rests.

```
user=> (generate-meter 4)
(2 2)
```

The `one-two - one-two` meter with ones containing notes.

Now that we have the meter, we can further decompose these into sixteenth-beats using the [segment-measure](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L59) function. A 11/4 measure would contain `11*16/4 = 44` sixteenth-beats.

```
user=> (def meter-11 (generate-meter 11))
#'user/meter-11
user=> meter-11
(1 4 3 3)
user=> (segment-measure meter-11 :note-value 4)
(1 3 4 5 7 8 9 10 11 13 14 16 19 20 21 23 28 30 32 33 37 39 42 44)
user=> (segment-measure meter-11 :note-value 4)
(1 2 4 5 6 8 9 11 12 15 17 19 21 22 23 25 26 28 29 31 32 33 36 37 38 39 40 42 43)
user=> (segment-measure meter-11 :note-value 4)
(1 2 3 4 5 7 8 10 11 12 13 16 17 18 19 21 23 24 26 30 32 33 34 35 36 38 39 40 41 43 44)
```

Note that for our `1-4-3-3` (`4-16-12-12` in sixteenth-beats) meter (the *ones* would correspond with 1, 5, 21 and 33 sixteenth-beat positions respectively), every generated measure contains a note at the 1, 5, 21 and 33 note positions. A rest can never fall on these positions.

##### Sparseness of a measure

We introduce a new factor called *sparseness* which determines how dense a measure is (Remember? The first measure of *Smoke on the Water* had more notes than *Comfortably Numb*. Ergo, it was less sparse). The higher the *sparseness* of a measure, the more rests it contains. The default sparseness is 1, which means a sixteenth-beat has equal chances of being either a note or a rest.

```
user=> (segment-measure meter-11 :note-value 4 :sparseness 2)
(1 2 4 5 8 10 13 15 21 23 27 29 30 33 35 41 42 44)
```

Let's turn up the sparseness to an absurdly high number, say 100:

```
user=> (segment-measure meter-11 :note-value 4 :sparseness 100)
(1 5 21 33)
```

Voila, we have the *ones* from the meter! The other sixteenth-beats were never filled, they all ended up as rests.

##### Stringing measures together

The [string-measures](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L78) function takes a list of measures and the beat count of each measure, and strings them together. If `[1 3 4]` and `[1 2 4]` were the measures to be strung together, the result would be `[1 3 4 5 6 8]`.

```
user=> (string-measures '((1 3 4) (1 2 4)) 4)
(1 3 4 5 6 8)
```

The [generate-rhythm](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L132) function takes the measure count and beat count of each measure, and optionally the minimum note value (sixteen being the default) and sparseness (1 default), and generates the respective number of measures, each of the specified number of beats. For instance, this generates 10 measures, each of 4/4 time signature:

```
user=> (def rhythm (generate-rhythm 10 4))
#'user/rhythm
user=> rhythm
(1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 35 37 39)
user=> (count rhythm)
20
```

##### TL;DR Rhythm

- We construct a random *meter* for a single measure of a specific time signature using the `generate-meter` function. Say, `(1 4 3 3)` for a 11/4 measure (11 quarter-beats, counted `one - one-two-three-four - one-two-three - one-two-three`).
```
user=> (def meter-11 (generate-meter 11))
#'user/meter-11
user=> meter-11
(1 4 3 3)
```

- We segment the measure further into sixteenth-beats, using the `segment-measure` function. A 11/4 measure will have `11 * 16/4 = 44` sixteenth beats, and since the meter specifies positions that *must* contain notes, our measure will contain notes at `1`, `1 + (16/4)*1 = 5`, `5 + (16/4)*4 = 21` and `21 + (16/4) * 3 = 33` sixteenth-beat positions. The rest of the beats are populated randomly with notes or rests.
```
user=> (segment-measure meter-11 :note-value 4)
(1 3 4 5 7 8 9 10 11 13 14 16 19 20 21 23 28 30 32 33 37 39 42 44)
```

- The ratio of notes to rests in the measure can be determined by using the *sparseness* parameter (whose default value is 1).
```
user=> (segment-measure meter-11 :note-value 4 :sparseness 2)
(1 2 4 5 8 10 13 15 21 23 27 29 30 33 35 41 42 44)
user=> (segment-measure meter-11 :note-value 4 :sparseness 100)
(1 5 21 33)
```

- We use the [generate-rhythm](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L132) function to create a rhythm of multiple measures. It takes the measure count and beat count of each measure, and optionally the minimum note value (sixteen being the default) and sparseness (1 default), and generates the respective number of measures, each of the specified number of beats.

```
user=> (def rhythm (generate-rhythm 10 4))
#'user/rhythm
user=> rhythm
(1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 35 37 39)
user=> (count rhythm)
20
```

***

### Melody

Melody is, possibly, the identity of a song. It can be considered the prime contributing factor to the *mood* or *feel* of a song. Though a melody cannot exist without a rhythm, both rhythm and harmony can be considered as support structures that enrich the melody. There are probably a huge number of patterns and methods in which listenable melodies can be generated procedurally.

##### Notes

[Notes](https://en.wikipedia.org/wiki/Musical_note) are sounds of a certain pitch or frequency. The A4 note is, for instance, a sound of 440 Hz. The A5 note is a sound of 880 Hz. The musical note starts from C0 at 16.35 Hz. Unlike the English alphabet, musical notes in western musical notation start from C and end with B, as follows:

```
C  C#/Db  D  D#/Eb  E  F  F#/Gb  G  G#/Ab  A  A#/Bb  B
```

`#` indicates a *Sharp* and `b` indicates a *Flat*. Sharps, or flats, are indicated by the black keys on the piano. Ever wondered why there is no black key between `B#/Cb` and `E#/Fb`? There *is* no `B#/Cb` or `E#/Fb`. If you sharp a B, you get a C and if you sharp an E you get an F. This is merely a convention that arose from the constraint that there could only be 12 notes in an *octave*. In some forms of musical notation, E# and B# do exist.

The frequency ratio between a note and its previous note is the [twelfth root of two](https://en.wikipedia.org/wiki/Twelfth_root_of_two), or `1.0594`. For instance, `C#0 (17.32 Hz) / C0 (16.35 Hz) = 1.0594`.

##### Octaves

An [octave](https://en.wikipedia.org/wiki/Octave) is the distance between a note of frequency X and the note of frequency 2X, containing twelve notes. The range of frequencies between A4 and A5 constitute one octave.

For instance, here's the octave from A4 to A5 and the corresponding frequencies:

```
A4   A#4/Bb4  B4      C5      C#5/Db5   D5      D#5/Eb5   E5      F5      F#5/Gb5   G5      G#5/Ab5
440  466.16   498.88  523.25  554.37    587.33  622.25    659.25  698.45  739.99    783.99  830.61
```

##### Semitones and Whole tones

- The interval between one note and the next (A4 and A#4) is a [semitone](https://en.wikipedia.org/wiki/Semitone) or a half-step. They're indicated by `H`.
- The interval between a note and the note two semitones after it (A4 and B4) is a [whole tone](https://en.wikipedia.org/wiki/Major_second) or a whole-step. They're indicated by `W`.

##### Scales

[Scales](https://en.wikipedia.org/wiki/Scale_%28music%29) are groups of notes, ascending in frequency. [Scale Degrees](https://en.wikipedia.org/wiki/Degree_%28music%29) indicate the position of each note in the scale. The first note of a scale (the note after which the scale) is named, is called the *tonic* of the scale. Its scale degree is 1.

Depending on how a scale is built, it could exhibit a certain mood, characteristic or emotion. The most used types of scales are the major scales and the minor scales, but there are a lot of other types.

[Major scales](https://en.wikipedia.org/wiki/Major_scale) are constructed using the `W-W-H-W-W-W-H` pattern (W-Whole step, H-Half step). For example, the A4 major scale is as follows:

```
A4 - B4 - C#5 - D5 - E5 - F#5 - G#5 - A5
   W    W     H    W    W     W     H
```

They tend to express *positive* emotions like majesty, victory, curiosity, love, joy and so on, in general.

[Minor scales](http://en.wikipedia.org/wiki/Minor_scale) or *natural minor* scales are constructed using the `W-H-W-W-H-W-W` pattern. For example, the A4 minor is as follows:

```
A4 - B4 - C5 - D5 - E5 - F5 - G5 - A5
   W    H    W    W    W    W    W
```

They tend to express *negative* emotions like betrayal, melancholy, tragedy, ominousness and so on, in general.

Here's a document for further reading on [Characteristics of Musical Keys](http://biteyourownelbow.com/keychar.htm).

##### Intervals - unison, steps and leaps

An [interval](https://en.wikipedia.org/wiki/Interval_%28music%29) is the difference between two notes or pitches. The smallest interval is the semitone. Here are the intervals for the A4 minor scale (T - Tonic):

```
A4 - B4 - C5 - D5 - E5 - F5 - G5 - A5
T  - 1  - 2  - 3  - 4  - 5  - 6  - 7
```

C5, for instance, is 2 intervals away from A4 in the A4 minor scale. G5 is 6 intervals away.

Moving by zero intervals in the scale (staying on the same note) is called an [*unison*](https://en.wikipedia.org/wiki/Unison).
Moving by a single interval in the scale is called a [*step*](https://en.wikipedia.org/wiki/Steps_and_skips).
Moving by two or more intervals in the scale is called a [*leap*](https://en.wikipedia.org/wiki/Steps_and_skips).

In an octave, based on relative terms to the first note, each note has a designation that indicates the number of semitones required to reach it. These are the [main intervals](https://en.wikipedia.org/wiki/Interval_%28music%29#Main_intervals):

```
A4     | A#4/Bb4      | B4           | C5          | C#5/Db5     | D5             |
Unison | Minor second | Major second | Minor third | Major third | Perfect fourth |

D#5/Eb5       | E5          | F5          | F#5/Gb5       | G5            | G#5/Ab5        |
Perfect fifth | Minor sixth | Major sixth | Minor seventh | Major seventh | Perfect octave |
```

##### Melodic motion - conjunct and disjunct

[Melodic motion](https://en.wikipedia.org/wiki/Melodic_motion) characterizes the tendency of a melody to *jump around*, or the *contours* of the melody. The most common types of melodic motion are *conjunct motion* and *disjunct motion*, though there are lot of other ways in which a melody can be structured.

A melody that exhibits *conjunct motion* consists of a lot of *steps* (single intervals or successive notes) and *unisons* (same notes), and very little *leaps*.
A melody that exhibits *disjunct motion* consists of a lot of *leaps* (multiple intervals) and very little *steps* and *unisons*.

Most popular music uses *conjunct motion*. The melodies tend to be closely structured around a scale's notes by traversing the scale in steps, whilst avoiding leaps.

Let's pick apart the first four vocal measures of *My Generation* by *The Who*. These are roughly the notes of each of the syllables:

```
Peo - ple - try - to 
G   - G   - F   - F# 

put - us - d' - down
C   - C  - Bb - Bb

talk - in' - 'bout - my
D    - D   - E     - E

gen - er - a - tion
F   - F# - E - D
```

This is a tune in the F major scale `F - G - A - A#/Bb - C - D - E - F` with an F# added in. Here's how the melodic motion is (U - Unison, S - Step, L - Leap):

```
Peo - ple - try - to - put - us - d' - down - talk - in' - 'bout - my - gen - er - a - tion
G   - G   - F   - F# - C   - C  - Bb - Bb   - D    - D   - E     - E  - F   - F# - E - D
    U     S     S    L     U    S    U      L      U     S       S    S     S    S   S
```

That's just *two* leaps while we have *four* unisons and *nine* steps. This is clearly conjunct motion.

Melodic motion is the most important quality of a melody that determines its quality and *listenability*. Simulating melodic motion is going to be the purpose of every procedure we write that generates music.

##### Weighted random interval jumps

Let's take the F major scale:

```
user=> (def f-major (scale :f4 :major))
#'user/f-major
user=> f-major
(65 67 69 70 72 74 76 77)
```

Let's start with the first note F, construct one unison, two up-steps, one 3-interval up-leap, two down-steps, and end with the last note of the scale. That would give us `F - F - G - A - D - C - Bb - F`, or in scale degrees, `1 - 1 - 2 - 3 - 6 - 5 - 4 - 8`. This is typical conjunct motion.

In conjunct motion, since steps are more likely to appear than leaps, let's assign the chances as follows:

```
Unison | Up step | Down step | Up leap | Down leap | Octave up | Octave down
  6%   |   35%   |    35%    |    8%   |     8%    |     4%    |     4%
```

The [conjunct-motion](https://github.com/pranavrc/tenor/blob/master/src/tenor/melody.clj#L48) function takes a scale and a scale degree and generates a new degree in the scale by choosing a random interval based on these weights.

```
user=> (conjunct-motion f-major 3)
2
user=> (conjunct-motion f-major 3)
2
user=> (conjunct-motion f-major 3)
4
user=> (conjunct-motion f-major 3)
2
user=> (conjunct-motion f-major 3)
6
```

We started with the 3rd note (A4), and it generated the second note (down step, note G4) thrice, fourth note (up step, note Bb) once and the sixth note (3-interval leap, note D5) once. It took five tries to generate a leap.

Let's take the same scale, start with the first note F, construct one unison, two 3-interval up-leaps, one down-step, two 2-interval down-leaps, and end with the last note of the scale. That would give us `F - F - Bb - E - D - Bb - G - F`, or in scale degrees, `1 - 1 - 4 - 7 - 6 - 4 - 2 - 8`. This is typical disjunct motion.

In disjunct motion, since leaps are more likely to appear than steps, let's reassign the chances as follows:

```
Unison | Up step | Down step | Up leap | Down leap | Octave up | Octave down
  6%   |    8%   |     8%    |   30%   |    30%    |     9%    |     9%
```

The [disjunct-motion](https://github.com/pranavrc/tenor/blob/master/src/tenor/melody.clj#L52) function takes a scale and a scale degree and generates a new degree in the scale based on these new weights.

```
user=> (disjunct-motion f-major 3)
1
user=> (disjunct-motion f-major 3)
6
user=> (disjunct-motion f-major 3)
2
user=> (disjunct-motion f-major 3)
3
user=> (disjunct-motion f-major 3)
8
```

We started with the same third note (A4), and in the same five tries, we hit three leaps (notes F4, D5 and F5), one step (G4) and one unison (A4).

##### Simulating melodic motion

Using weighted random interval construction, we can generate one note after another and construct a coherent melody that's either *conjunct* or *disjunct*. What if we could do this for every note in a rhythmic measure, and consequently for every rhythmic measure in a musical piece?

[generate-intervals](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L112) is a higher-order function that takes three parameters:

- A *procedure* or a fundamental method for simulating melodic motion, such as the methods [conjunct-motion](https://github.com/pranavrc/tenor/blob/master/src/tenor/melody.clj#L48) or [disjunct-motion](https://github.com/pranavrc/tenor/blob/master/src/tenor/melody.clj#L52).
- A scale.
- Number of notes to generate.

It starts at the tonic, constructs a list of intervals in the scale(s) based on the procedure passed to it, and ends either at the tonic (Degree 1) or one octave above the tonic (Degree 1 or 8). Let's generate 20 notes in F4 major using both conjunct and disjunct motion:

```
user=> (def f-major (scale :f4 :major))
#'user/f-major
user=> f-major
(65 67 69 70 72 74 76 77)
user=> (def conjunct-20 (generate-intervals conjunct-motion f-major 20))
#'user/conjunct-20
user=> conjunct-20
(1 2 1 5 6 7 3 4 1 2 3 2 3 3 4 3 2 5 6 1)
user=> (def disjunct-20 (generate-intervals disjunct-motion f-major 20))
#'user/disjunct-20
user=> disjunct-20
(1 7 7 8 4 6 7 1 6 5 8 3 6 3 4 2 8 2 1 8)
```

We now have two 20-note melodies!

The [intervals->notes](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L121) function converts the intervals to actual *overtone* notes in the scale. It takes two arguments, the melody itself, and the scale. The `find-note-name` overtone function converts an overtone note into musical notation (Example: 65 to :F4).

```
user=> (intervals->notes conjunct-20 f-major)
(65 67 65 72 74 76 69 70 65 67 69 67 69 69 70 69 67 72 74 65)
user=> (intervals->notes disjunct-20 f-major)
(65 76 76 77 70 74 76 65 74 72 77 69 74 69 70 67 77 67 65 77)
user=> (map find-note-name (intervals->notes conjunct-20 f-major))
(:F4 :G4 :F4 :C5 :D5 :E5 :A4 :Bb4 :F4 :G4 :A4 :G4 :A4 :A4 :Bb4 :A4 :G4 :C5 :D5 :F4)
user=> (map find-note-name (intervals->notes disjunct-20 f-major))
(:F4 :E5 :E5 :F5 :Bb4 :D5 :E5 :F4 :D5 :C5 :F5 :A4 :D5 :A4 :Bb4 :G4 :F5 :G4 :F4 :F5)
```

Suh-weet! We just generated both conjunct and disjunct melodies by simulating melodic motion.

##### TL;DR Melody

- We construct a scale (F4 major, in our example) using overtone's `scale` function:

```
user=> (def f-major (scale :f4 :major))
#'user/f-major
user=> f-major
(65 67 69 70 72 74 76 77)
```

- The functions [conjunct-motion](https://github.com/pranavrc/tenor/blob/master/src/tenor/melody.clj#L48) and [disjunct-motion](https://github.com/pranavrc/tenor/blob/master/src/tenor/melody.clj#L52) take scales and scale degrees as their parameters, and generate a new scale degree based on weighted random interval jumps. The chances of generating a step are higher than a leap in conjunct motion, and vice-versa in disjunct motion:

```
user=> (conjunct-motion f-major 3)
2
user=> (conjunct-motion f-major 3)
4
user=> (disjunct-motion f-major 3)
1
user=> (disjunct-motion f-major 3)
6
```

- We use the higher-order function [generate-intervals](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L112) that takes three parameters (the procedure that generates new degrees, the scale, and the number of notes to generate), to generate multiple interval jumps and create a melodic line using conjunct and disjunct motion. It always starts and ends at the tonic (or one octave above), because a musical piece that doesn't end on the tonic doesn't feel *complete*.

```
user=> (def conjunct-20 (generate-intervals conjunct-motion f-major 20))
#'user/conjunct-20
user=> conjunct-20
(1 2 1 5 6 7 3 4 1 2 3 2 3 3 4 3 2 5 6 1)
user=> (def disjunct-20 (generate-intervals disjunct-motion f-major 20))
#'user/disjunct-20
user=> disjunct-20
(1 7 7 8 4 6 7 1 6 5 8 3 6 3 4 2 8 2 1 8)
```

- We use a combination of the functions [intervals->notes](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L121) (a function that converts the intervals to actual *overtone* notes in the scale; it takes two arguments, the melody itself, and the scale) and `find-note-name` (overtone function that converts an overtone note into musical notation, such as 65 to :F4) to convert the generated melodic line from scale degrees to musical notes.

```
user=> (intervals->notes conjunct-20 f-major)
(65 67 65 72 74 76 69 70 65 67 69 67 69 69 70 69 67 72 74 65)
user=> (intervals->notes disjunct-20 f-major)
(65 76 76 77 70 74 76 65 74 72 77 69 74 69 70 67 77 67 65 77)
user=> (map find-note-name (intervals->notes conjunct-20 f-major))
(:F4 :G4 :F4 :C5 :D5 :E5 :A4 :Bb4 :F4 :G4 :A4 :G4 :A4 :A4 :Bb4 :A4 :G4 :C5 :D5 :F4)
user=> (map find-note-name (intervals->notes disjunct-20 f-major))
(:F4 :E5 :E5 :F5 :Bb4 :D5 :E5 :F4 :D5 :C5 :F5 :A4 :D5 :A4 :Bb4 :G4 :F5 :G4 :F4 :F5)
```

***

### Combining Melody with Rhythm

So far, we've been able to generate a rhythmic measure of a particular time signature, and a rhythm-agnostic melody with notes and melodic motion. Combining these together is pretty straight-forward.

We used the [generate-rhythm](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L132) function to generate a 20-beat rhythm in 4/4 time signature earlier:

```
user=> (def rhythm (generate-rhythm 10 4))
#'user/rhythm
user=> rhythm
(1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 35 37 39)
user=> (count rhythm)
20
```

Now that we have our rhythm, we can combine the `intervals->notes` and `generate-intervals` functions like before, to create a melody that contains as many notes as we have beats in our measure (20 beats):

```
user=> (def f-major (scale :f4 :major))
user=> (def conjunct-intervals (generate-intervals conjunct-motion f-major (count rhythm)))
#'user/conjunct-intervals
user=> conjunct-intervals
(1 2 2 4 5 4 5 6 7 8 7 3 3 4 5 1 2 3 1 1)
user=> (def conjunct-melody (intervals->notes conjunct-intervals f-major))
#'user/conjunct-melody
user=> conjunct-melody
(65 67 67 70 72 70 72 74 76 77 76 69 69 70 72 65 67 69 65 65)
```

We now have the rhythm and the melody of 20 beats, as follows:

```
(rhythm)          1   3   5   7   9   11  13  15  17  19  21  23  25  27  29  31  33  35  37  39
(conjunct-melody) 65  67  67  70  72  70  72  74  76  77  76  69  69  70  72  65  67  69  65  65
```

The [map-entity](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L127) function takes a rhythm and a melody, and creates a list of hash-maps, each of the format `{:note note, :pos beat}`, indicating which note of the melody goes into which beat in the rhythm.

```
user=> (def musical-piece (map-entity rhythm conjunct-melody))
#'user/musical-piece
user=> musical-piece
({:note 65, :pos 1} {:note 67, :pos 3} {:note 67, :pos 5} {:note 70, :pos 7} {:note 72, :pos 9} {:note 70, :pos 11} {:note 72, :pos 13} {:note 74, :pos 15} {:note 76, :pos 17} {:note 77, :pos 19} {:note 76, :pos 21} {:note 69, :pos 23} {:note 69, :pos 25} {:note 70, :pos 27} {:note 72, :pos 29} {:note 65, :pos 31} {:note 67, :pos 33} {:note 69, :pos 35} {:note 65, :pos 37} {:note 65, :pos 39})
```

The [generate-entity-map](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L142) function wraps all of the above functionality into a single function. It takes all the parameters that we provided to other functions earlier - the procedure to simulate melodic motion, the measure count, beat count, and optionally, the note value, sparseness and scale - and creates hash-maps using `map-entity`.

```
user=> (generate-entity-map conjunct-motion 10 4 :scale (scale :f4 :major)))
({:note 65, :pos 1} {:note 67, :pos 2} {:note 72, :pos 4} {:note 74, :pos 5} {:note 76, :pos 6} {:note 65, :pos 8} {:note 74, :pos 9} {:note 72, :pos 10} {:note 74, :pos 12} {:note 72, :pos 13} {:note 74, :pos 14} {:note 67, :pos 16} {:note 65, :pos 17} {:note 67, :pos 18} {:note 72, :pos 20} {:note 65, :pos 21} {:note 65, :pos 22} {:note 67, :pos 24} {:note 69, :pos 25} {:note 67, :pos 26} {:note 70, :pos 28} {:note 77, :pos 29} {:note 65, :pos 30} {:note 67, :pos 32} {:note 72, :pos 33} {:note 70, :pos 34} {:note 69, :pos 36} {:note 70, :pos 37} {:note 72, :pos 38} {:note 77, :pos 40})
```

And that's it! We have a barebones *musical piece* with rhythm and melody. Now we can move on to ~~peace and~~ harmony!

***

### Harmony

If Melody is horizontal in time, [Harmony](https://en.wikipedia.org/wiki/Harmony) is vertical. Harmony uses simultaneously played notes, or *chords*, to create textures that act as a rich backdrop for the melody. Melody without harmony is like the painting of a beautiful mountainside, drawn against a plain white background instead of the sky.

##### Chords and Arpeggios

[Chords](https://en.wikipedia.org/wiki/Chord_%28music%29) are notes played simultaneously. These are usually harmonious notes that, when played together, produce a sound that's something more than the sum of their parts. If the same notes are played in a sequence instead of simultaneously, they become an [Arpeggio](https://en.wikipedia.org/wiki/Arpeggio).

A [*major chord*](https://en.wikipedia.org/wiki/Major_chord) is constructed by taking the root note, the note that is its *major third* (4 semitones forward) and its *perfect fifth* (7 semitones forward). For instance, C-major chord is `C - E - G`.

A [*minor chord*](https://en.wikipedia.org/wiki/Minor_chord) is constructed by taking the root note, the *minor third* (3 semitones forward) and the *perfect fifth*. The C-minor chord is `C - D#/Eb - G`.

There are numerous other ways of constructing chords (augmented, diminished, suspended etc), but just like the major and minor scales, the major and minor chords are the most popular.

##### *Chordifying* a musical piece

Using `generate-entity-map`, we built a map of `{:note note, :pos beat}` pairs and generated a musical piece with just rhythm and melody. Time to *harmonize* it.

The [harmony](https://github.com/pranavrc/tenor/blob/master/src/tenor/harmony.clj) namespace contains procedures that construct chords from notes. For instance, the [major-chords-octave-down](https://github.com/pranavrc/tenor/blob/master/src/tenor/harmony.clj#L21) function takes a note, constructs the major chord of the note shifted down by one octave (Example, F3 major chord if the note is F4), and returns a hash-map `{:pos beat :note major-chord-of-note-shifted-one-octave-down}` of that note.

The [chordify](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L156) higher-order function takes the map of our musical piece, and a *procedure* or fundamental method to build a chord out of a note (just like our `generate-intervals` function took `conjunct-motion` or `disjunct-motion` as parameters, remember?). It then takes our musical piece, picks out a few notes randomly, and generates a new musical piece with chords instead of notes at the same positions.

```
user=> musical-piece
({:note 65, :pos 1} {:note 67, :pos 3} {:note 67, :pos 5} {:note 70, :pos 7} {:note 72, :pos 9} {:note 70, :pos 11} {:note 72, :pos 13} {:note 74, :pos 15} {:note 76, :pos 17} {:note 77, :pos 19} {:note 76, :pos 21} {:note 69, :pos 23} {:note 69, :pos 25} {:note 70, :pos 27} {:note 72, :pos 29} {:note 65, :pos 31} {:note 67, :pos 33} {:note 69, :pos 35} {:note 65, :pos 37} {:note 65, :pos 39})
user=> (def chords (chordify musical-piece major-chords-octave-down))
#'user/chords
user=> chords
({:note [53 57 60], :pos 1} {:note [55 59 62], :pos 5} {:note [60 64 67], :pos 9} {:note [60 64 67], :pos 13} {:note [57 61 64], :pos 25} {:note [53 57 60], :pos 31} {:note [57 61 64], :pos 35})
```

Now that we have the chords, let's lay out our 20-beat musical piece again:

```
(rhythm)          1   3   5   7   9   11  13  15  17  19  21  23  25  27  29  31  33  35  37  39
(conjunct-melody) 65  67  67  70  72  70  72  74  76  77  76  69  69  70  72  65  67  69  65  65
(chords)          53      55      60      60                      57          53      57 
                  57      59      64      64                      61          57      61  
                  60      62      67      67                      64          60      64
```

And there we go! Our 20-beat musical piece in 4/4 is now complete!

***

### Macroland - Music as Code and Code as Music

*All work and no play makes Jack a dull chap - Van Morrison*

Now that we have the maps for both melody and chords in hand, we can now play them! We're gonna write some new code ~~that generates music~~ that generates *code* that generates music.

##### Playing notes in Overtone

Here's how we'd play a note in overtone: `(at epoch-time (instrument note))`

Here's an example: `(at 1436066513081 (piano 67))`

The `now` function returns the current epoch time, so now we have: `(at (now) (piano 67))`

Say we had a short melody of one measure `({:note 67, :pos 1} {:note 65, :pos 2} {:note 67, :pos 3} {:note 65, :pos 4})` with a beat-length of 250 milliseconds (4 beats, 1 second total). Let's play this measure with Overtone:

```
user=> (let [time (now)]
         (at (+ (* 250 1) time) (piano 67))
         (at (+ (* 250 2) time) (piano 65))
         (at (+ (* 250 3) time) (piano 67))
         (at (+ (* 250 4) time) (piano 65)))
```

##### Generating code for our musical piece

The [play-piece](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L177) function takes our musical piece (as a map), the duration of each beat, and the instrument function as parameters. It generates Overtone code to play the music. Let's try this first with our small one-measure melody:

```
user=> (play-piece '({:note 67, :pos 1} {:note 65, :pos 2} {:note 67, :pos 3} {:note 65, :pos 4}) 250 (fn [note] (piano note)))
(clojure.core/let [time (overtone.live/now)] (overtone.live/at (clojure.core/+ time 250) (#<user$eval19948$fn__19949 user$eval19948$fn__19949@4c68278f> 67)) (overtone.live/at (clojure.core/+ time 500) (#<user$eval19948$fn__19949 user$eval19948$fn__19949@4c68278f> 65)) (overtone.live/at (clojure.core/+ time 750) (#<user$eval19948$fn__19949 user$eval19948$fn__19949@4c68278f> 67)) (overtone.live/at (clojure.core/+ time 1000) (#<user$eval19948$fn__19949 user$eval19948$fn__19949@4c68278f> 65)))
```

*...work in progress...*

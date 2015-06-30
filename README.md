# Tenor

The concept of music is incredibly accommodating; anything from industrial noise to baroque classical can be appreciated with enough interest. This makes the problem statement simple - all we need is a random rhythm and melody generator and we could pass off its output as music.

But popular and contemporary music has tuned the ears of the average listener to certain preset patterns in melody, harmony and rhythm. As a result, most of us have developed a collective sense of what makes good music and what doesn't, while in fact, there is no such thing as good or bad music (objectively, of course). Now this makes the problem statement a lot more tangible - generating music is easy, generating music that *sounds good to the human ear* requires more effort.

The project is an attempt at constructing primitive music theory and using it as a base to build procedures that can generate melodies, in-tune to the ears of the average listener.

## Contents

- [Music to the human ears](#music-to-the-human-ears)
- [Rhythm](#rhythm)
  - [Time Signature](#time-signature)
  - [Popular songs in non-4/4 signature](#popular-songs-in-non-44-signature)
  - [Representing measures](#representing-measures)
  - [Meters](#meters)
  - [Generating a measure in 11/4](#generating-a-measure-in-114)
  - [Sparseness of a measure](#sparseness-of-a-measure)
  - [TL;DR](#tldr)

### Music to the human ears

Most pieces in contemporary music share certain common characteristics in rhythm, harmony and melody. There's a high chance that one or more of these characteristics occur in a song that we happen to discover, and since we're attuned to these patterns already, the melody then becomes instantly identifiable without sounding like random noise.

Some of these idiosyncrasies could be:

- The famed [4/4 time signature](https://en.wikipedia.org/wiki/Time_signature#Most_frequent_time_signatures).
- The [call-and-response](https://en.wikipedia.org/wiki/Call_and_response_%28music%29#Popular_music) pattern.
- Phil Specter's [Wall of Sound](https://en.wikipedia.org/wiki/Wall_of_Sound).
- The [C-major scale](https://en.wikipedia.org/wiki/C_major).
- Variations of the C-D-G-C chord pattern or other common chord patterns.

Here's a [very interesting analysis](http://www.hooktheory.com/blog/i-analyzed-the-chords-of-1300-popular-songs-for-patterns-this-is-what-i-found/) on common patterns found in over 1300 popular songs. One way of generating listenable music would be to create procedures that conform to some of these (though not exactly these) norms.

### Rhythm

Rhythm is, perhaps, the most accessible and instantly decipherable component of a song because it lays out the fundamental structure over which melody and harmony interplay. A single rhythmic theme (say, the 4/4) also repeats fairly often in a song, so measures in a specific time signature need not be procedurally generated.

##### Time Signature

A time-signature governs how many beats (or foot-taps) there are in a measure, and the duration of each beat. A signature in 4/4 simply means that we repeatedly tap 4 times in quarter-note (1/4 - its length depends on the length of the full note) intervals. Here, each tap is a beat and 4 taps make a measure (the repeating basic pattern of the song). Here's how we'd tap our feet to *Comfortably Numb* by Pink Floyd (a popular 4/4 song), for instance:

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

For example, the song *Flower Punk* by Frank Zappa, a song with interchanging 5/4 and 7/4 measures, could be counted using the following meter:

```
one-two - one-two-three
one-two - one-two - one-two-three
one-two - one-two-three
one-two - one-two - one-two-three
```

We could count the *Mission Impossible* theme using the following meter:

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

##### TL;DR

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

*...work in progress...*

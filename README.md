# Tenor

The concept of music is incredibly accomodating; anything from industrial noise to baroque classical can be appreciated with enough interest. This makes the problem statement simple - all we need is a random rhythm and melody generator and we could pass off its output as music.

But popular and contemporary music has tuned the ears of the average listener to certain preset patterns in melody, harmony and rhythm. As a result, most of us have developed a collective sense of what makes good music and what doesn't, while in fact, there is no such thing as good or bad music (objectively, of course). Now this makes the problem statement a lot more tangible - generating music is easy, generating music that *sounds good to the human ear* requires more effort.

The project is an attempt at constructing primitive music theory and using it as a base to build procedures that can generate melodies, in-tune to the ears of the average listener.

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

#### Time Signature

A time-signature governs how many beats (or foot-taps) there are in a measure, and the duration of each beat. A signature in 4/4 simply means that we repeatedly tap 4 times in quarter-note (1/4 - its length depends on the length of the full note) intervals. Here, each tap is a beat and 4 taps make a measure (the repeating basic pattern of the song). Here's how we'd tap our feet to *Comfortably Numb* by Pink Floyd (a popular 4/4 song), for instance:

```
Hello Hel - lo Hello - (Rest) Is there - anybody 
Tap       - Tap      - Tap             - Tap

In there? - (Rest) - (Rest) Just - nod
Tap       - Tap    - Tap         - Tap
```

Let's take the first beat ('Hello Hel'), and decompose the quarter note (1/4) into sixteenth notes (1/16), which means we're gonna split the beat further into four other beats. Now, if we tap to *these* beats (we'd have to tap really fast), the beat will look like this.

```
Hel - lo  - (rest)  - Hel
Tap - Tap - Tap     - Tap
```

This tells us that in a measure decomposed into sixteenth notes (assuming we do not cross over into 1/32 and further), notes and rests are, to an extent, randomly interspersed. To reaffirm this, we can look at *Smoke on the Water* by Deep Purple (another popular 4/4 song). Let's take the first measure (remember, 4 foot taps, each of quarter-note length):

```
Smoooo - ke on the - Waaate - rrrrrrr
Tap    - Tap       - Tap    - Tap
```

First beat, sixteenth notes:

```
Smo - oo  - oo  - oo
Tap - Tap - Tap - Tap
```

If 'Hello Hel' was 1-1-0-1 (note-note-rest-note), then 'Smoooo..' is 1-1-1-1 because there are no rests. We can observe the 1-1-1-1 decomposition for the first beat in other songs like *Eleanor Rigby* by The Beatles.

#### Popular songs in non-4/4 signature

*Money* by Pink Floyd is in 7/4 time. Let's do the tap routine:

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

*Four Sticks* by Led Zeppelin uses 5/4 in parts, *Paranoid Android *by Radiohead uses 7/8 in parts, the *Mission Impossible theme* by Lalo Schifrin uses 5/4.

#### Representing measures

The distribution of beats inside a measure can be randomized, though. A measure in `11/8` time signature could be split into `2-2-3-2-2` or `4-3-2-2`, a measure in `4/4` time signature could be split into `2-2`, `3-1`, or so on. Further levels of decomposition can be done where each individual beat is segmented even further into note positions, fills, or rests. The `4` in `4-3-2-2` could, for instance, be decomposed into `1-3-4` where the numbers indicates notes (which would mean the first note extends until 3).

The [time-signature](https://github.com/pranavrc/tenor/blob/master/src/tenor/constructs.clj#L21) function takes a beat count for a measure, and generates random beat distributions for that measures of that signature.

```
user=> (time-signature 11)
(1 4 3 3)
user=> (time-signature 11)
(1 3 3 4)
user=> (time-signature 4)
(2 2)
user=> (time-signature 4)
(4)
```


*...work in progress...*

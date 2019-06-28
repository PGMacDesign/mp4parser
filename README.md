
 [![JitPack](https://jitpack.io/v/pgmacdesign/mp4parser.svg)](https://jitpack.io/#pgmacdesign/mp4parser)

Java MP4 Parser
====================

A Java API to read, write and create MP4 container. Manipulating containers is different from encoding and decoding videos and audio. 


Using the library
------------------

The library is published to Jitpack. To include it in your project use the following code:

Gradle:

Include this in your top-level gradle file
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Include this in your module-level gradle file:
```
    implementation 'com.github.PGMacDesign.mp4parser:isoparser:1.2.10'
```

For other build instructions (IE Maven), See [this Jetpack link](https://jitpack.io/#PGMacDesign/mp4parser/1.2.10)


Updated by PGMacDesign
------------------

This Library was updated in April of 2019 with the following goals:

1) Fix Logging - There is a lot of erroneous logging occurring on the 1.x branch. I have added some core code to help resolve that issue.
If you want to disable or enable logging for *debug*, just call the following:

```
LoggingCore.setShouldLog(false)
```
Where false will disable logging and true will enable it. By default, this is set to true to mirror how the original creator wrote it.

2) Optimization - There was a pull request located [here](https://github.com/sannies/mp4parser/pull/349) by Takke that indicated a significant improvement in conversion speed for larger videos.
The code here updated that and included it.

3) File Writing Callback - In its current iteration, the only way to keep track of the progress of file writing was to read the logs.
This has been resolved by including a new callback interface that can be used. The interface has one callback method with 3 variables:

```
    public interface Mp4TrimmerTimeCallback {
        void chunkWritten(long bytesWritten, long totalBytes, float percentage);
    }
```

In it, the first var is the number of bytes that have been written.
The second is the total number of bytes that will be written.
The third is a percentage calculation (float between 0 and 1) dividing the first into the second.
Please note that if an issue happens in calculating the total size, 0 will be sent for the percentage.

What can you do?
--------------------

Typical tasks for the MP4 Parser are: 

- Muxing audio/video into an MP4 file
- Append recordings that use same encode settings
- Adding/Changing metadata
- Shorten recordings by omitting frames

My examples will all use H264 and AAC as these two codecs are most typical for MP4 files. AC-3 is also not uncommon as the codec is well known from DVD. 
There are also MP4 files with H263/MPEG-2 video tracks but they are no longer used widespread as most android phones. You can also

Muxing Audio/Video
--------------------

The API and the process is straight-forward:

1. You wrap each raw format file into an appropriate Track object. 
  ```java
H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("video.h264"));
AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("audio.aac"));
  ```

2. These Track object are then added to a Movie object
  ```java
Movie movie = new Movie();
movie.addTrack(h264Track);
movie.addTrack(aacTrack);
  ```

3. The Movie object is fed into an MP4Builder to create the container. 
  ```java
Container mp4file = new DefaultMp4Builder().build(movie);
  ```

4. Write the container to an appropriate sink.
  ```java
FileChannel fc = new FileOutputStream(new File("output.mp4")).getChannel();
mp4file.writeContainer(fc);
fc.close();
  ```

There are cases where the frame rate is signalled out of band or is known in advance so that the H264 doesn't contain it literally. 
In this case you will have to supply it to the constructor. 

There are Track implementations for the following formats: 

 * H264
 * AAC
 * AC3
 * EC3 

and additionally two subtitle tracks that do not directly wrap a raw format but they are conceptually similar.

Typical Issues
--------------------

Audio and video are not in sync. Whenever there are problems with timing possible make sure to start 

Audio starts before video
--------------------

In AAC there are always samplerate/1024 sample/s so each sample's duration is 1000 * 1024 / samplerate milliseconds. 

 * 48KHz => ~21.3ms
 * 44.1KHz => ~23.2ms

By omitting samples from the start you can easily shorten the audio track. Remove as many as you need. You will not be able 
to match audio and video exactly with that but the human perception is more sensible to early audio than to late audio. 

Remember: If someone is only 10 meters away the delay between audio and video is >30ms. The brain is used to that!

```java
AACTrackImpl aacTrackOriginal = new AACTrackImpl(new FileDataSourceImpl("audio.aac"));
// removes the first sample and shortens the AAC track by ~22ms
CroppedTrack aacTrackShort = new CroppedTrack(aacTrackOriginal, 1, aacTrack.getSamples().size());
```




Append Recordings with Same Encode Settings 
-------------------------------------------

It is important to emphasize that you cannot append any two tracks with: 
 
 * Different resolutions 
 * Different frame-rates

What can't you do?
--------------------

Create JPEGs from a movie. No - this is no decoder. The MP4 Parser doesn't know how to do that. 
Create a movie from JPEGs

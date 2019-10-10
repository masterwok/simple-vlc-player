[![Release](https://jitpack.io/v/masterwok/simple-vlc-player.svg)](https://jitpack.io/#masterwok/simple-vlc-player)

# [deprecated] simple-vlc-player

I'm not longer maintaining this project due to time constraints.

An Android media player library powered by [LibVLC](https://wiki.videolan.org/LibVLC/) and [Open Subtitles](http://trac.opensubtitles.org/projects/opensubtitles/wiki/DevReadFirst).

## Notice

I'm currently unable to maintain this project. There are a few open issues that need to be resolved. Pull requests are welcome.

## Usage

Options can be provided for the initialization of LibVLC by using the ```VlcOptionsProvider``` singleton. This optional configuration should only be provided once during app initialization, or at some point before starting the ```MediaPlayerActivity```. If no options are provided, then a default configuration is provided when initializing LibVLC. To make life easier, the ```VlcOptionsProvider.Builder``` class is available to help build a list of common options. If an option is not provided to the builder, then the default value for that option is used. For example, the following enables LibVLC verbose logging, sets the subtitle background opactiy, and sets the subtitle encoding:

```java
VlcOptionsProvider
        .getInstance()
        .setOptions(
                new VlcOptionsProvider.Builder(this)
                        .withSubtitleBackgroundOpacity(255)
                        // See R.array.subtitles_encoding_values
                        .withSubtitleEncoding("KOI8-R")
                        .setVerbose(true)
                        .build()
        );
```

The ```MediaPlayerActivity``` can be started by providing a required media Uri and an optional subtitle Uri. The subtitle Uri must be a local file. Consumers of this library should also supply an [opensubtitles.org User-Agent](http://trac.opensubtitles.org/projects/opensubtitles/wiki/DevReadFirst) and preferred subtitle language via the intent. As stated in the Open Subtitles documentation, **the temporary User-Agent should only be used during development and testing** as it periodically changes.

```java
Intent intent = new Intent(this, MediaPlayerActivity.class);

intent.putExtra(MediaPlayerActivity.MediaUri, videoUri);
intent.putExtra(MediaPlayerActivity.SubtitleUri, subtitleUri);
intent.putExtra(MediaPlayerActivity.OpenSubtitlesUserAgent, "TemporaryUserAgent")
// See R.array.language_values
intent.putExtra(MediaPlayerActivity.SubtitleLanguageCode, "rus")

startActivity(intent);
```

## Configuration

Add this in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and add the following in the dependent module:

```gradle
dependencies {
    implementation 'com.github.masterwok:simple-vlc-player:1.1.16'
}
```

## Projects using simple-vlc-player
- [Bit Cast](https://play.google.com/store/apps/details?id=com.masterwok.bitcast)

## Licensing

Please refer to the [VLC FAQ](https://wiki.videolan.org/Frequently_Asked_Questions/#May_I_redistribute_libVLC_in_my_application.3F).

## Screenshots

![Local Playback](/sample/screenshots/localPlayback.jpg?raw=true "Local Playback")
![Renderer Item Selection](/sample/screenshots/rendererItemSelection.jpg?raw=true "Renderer Item Selection")
![Casting](/sample/screenshots/casting.jpg?raw=true "Casting")
![Subtitles](/sample/screenshots/subtitles.jpg?raw=true "Subtitles")
<img src="/sample/screenshots/subtitleSelection.jpg?raw=true" height="600" title="Subtitle Selection Dialog">
<img src="/sample/screenshots/lockScreenAndNotification.jpg?raw=true" height="600" title="Lock Screen and Notification">

[![Release](https://jitpack.io/v/masterwok/simple-vlc-player.svg)](https://jitpack.io/#masterwok/simple-vlc-player)

# simple-vlc-player
An Android media player library powered by [LibVLC](https://wiki.videolan.org/LibVLC/).

## Usage

Options can be provided for the initialization of LibVLC by using the ```VlcOptionsProvider``` singleton. This optional configuration should only be provided once during app initialization, or at some point before starting the ```MediaPlayerActivity```. If no options are provided, then a default configuration is provided when initializing LibVLC. To make life easier, the ```VlcOptionsProvider.Builder``` class is available to help build a list of common options. If an option is not provided to the builder, then the default value for that option is used. For example, the following enables LibVLC verbose logging and sets the subtitle background opactiy:

```java
VlcOptionsProvider
        .getInstance()
        .setOptions(
                new VlcOptionsProvider.Builder(this)
                        .withSubtitleBackgroundOpacity(255)
                        .setVerbose(true)
                        .build()
        );
```

The ```MediaPlayerActivity``` can be started by providing a required media URI and an optional subtitle URI. The subtitle URI must be a local file.

```java
Intent intent = new Intent(this, MediaPlayerActivity.class);

intent.putExtra(MediaPlayerActivity.MediaUri, videoUri);
intent.putExtra(MediaPlayerActivity.SubtitleUri, subtitleUri);

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
    implementation 'com.github.masterwok:simple-vlc-player:1.0.4'
}
```
unless you're a fan of large APKs, you'll probably want to add the following to the build.gradle of your app so an APK is generated per ABI:

```gradle
android {
    ...
    splits {
        abi {
            enable true
            reset()
            include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            universalApk false
        }
    }
}

// Map for the version code that gives each ABI a value.
ext.abiCodes = [
        'armeabi-v7a': 1,
        'arm64-v8a'  : 2,
        'x86'        : 3,
        'x86_64'     : 4
]

import com.android.build.OutputFile

android.applicationVariants.all { variant ->
    variant.outputs.each { output ->
        def baseAbiVersionCode = project.ext.abiCodes.get(output.getFilter(OutputFile.ABI))

        if (baseAbiVersionCode != null) {
            output.versionCodeOverride = baseAbiVersionCode * 10000000 + variant.versionCode
        }
    }
}
```

## Licensing

Please refer to the [VLC FAQ](https://wiki.videolan.org/Frequently_Asked_Questions/#May_I_redistribute_libVLC_in_my_application.3F).

## Screenshots

![Local Playback](/sample/screenshots/localPlayback.jpg?raw=true "Local Playback")
![Renderer Item Selection](/sample/screenshots/rendererItemSelection.jpg?raw=true "Renderer Item Selection")
![Casting](/sample/screenshots/casting.jpg?raw=true "Casting")
<img src="/sample/screenshots/lockScreenAndNotification.jpg?raw=true" height="600" title="Lock Screen and Notification">

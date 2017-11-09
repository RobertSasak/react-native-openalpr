# react-native-openalpr

[OpenALPR](https://github.com/openalpr/openalpr) integration for React Native. Provides a camera component that recognizes license plates in real-time.  Supports both iOS and Android.

<img alt="OpenALPR iOS Demo Video" src="https://cdn-images-1.medium.com/max/800/1*u1nTJMFc34aDLTPCIr0-cQ.gif" width=200 height=350 /> <img alt="OpenALPR Android Demo Video" src="https://user-images.githubusercontent.com/10334791/27850595-62dc852e-615e-11e7-875c-57a017dbb28c.gif" width=200 height=350 />

## Requirements
- iOS 9+
- Android 5.0+
- RN 0.41+

## Installation

### Installation with React Native

Start by adding the package and linking it.

```sh
$ yarn add react-native-openalpr
$ react-native link react-native-openalpr
```

or if you are using npm:

```sh
$ npm i -S react-native-openalpr
$ react-native link react-native-openalpr
```
Unfortunately, the `react-native link` command does not do everything it needs to do, so continue on to the project specific instructions below.

### iOS Specific Setup

#### Camera Permissions
- Add an entry for `NSCameraUsageDescription` in your `info.plist` explaining why your app will use the camera. If you forget to add this, your app will crash!

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  ...
 	<key>NSCameraUsageDescription</key>
 	<string>We use your camera for license plate recognition to make it easier for you to add your vehicle.</string>
</dict>
```

#### Linking
The project needs to be linked against four libraries: leptonica, opencv, tesseract, and openalpr.
- In Xcode, open your project (`.xcodeproj`).
- Go to `File -> Add Files` (or `Option + Command + A`)
- Click the `Options` button on the bottom and tick the `Copy items if needed` option.
- Add all four frameworks (leptonica, opencv, tesseract, openalpr) from the `node_modules/react-native-openalpr/ios/Frameworks`. This should cause the project to add a framework search path to the project's build settings (e.g. `$(PROJECT_DIR)/../node_modules/react-native-openalpr/ios/Frameworks`).
- Ensure that all four frameworks are included in the `Link Binary With Libraries` build phase by selecting your project in the tray on the left, selecting the `Build Phases` tab, then checking that each framework is included in the list of `Link Binary With Libraries`.

#### Resources
The alpr library requires a config file (`openalpr.conf`) and a data folder (`runtime_data`), both of which are included in the openalpr framework, but must be copied to the application resources:
  - Select your project on the project navigator, then, on the main pane, go to `Targets` → `<Your Project>` → `Build Phases` → `Copy Bundle Resources`, and click on the `+`.
  - Select `Add Other...`
  - Browse *into* the `openalpr.framework` bundle, and command-select both `runtime_data` and `openalpr.conf`.  Unselect `Copy items if needed` and select `Create folder references`.

#### Bitcode
Because the OpenCV binary framework release is compiled without bitcode, the other frameworks built by this script are also built without it, which ultimately means your Xcode project  also cannot be built with bitcode enabled.  [Per this message](http://stackoverflow.com/a/32728516/868173), it sounds like we want this feature disabled for OpenCV anyway.

To disable bitcode in your project:

- In `Build Settings` → `Build Options`, search for `Enable Bitcode` and set it to `No`.  

## Android-specific Setup

#### Camera Permissions
- Add permissions for `CAMERA` and `FLASHLIGHT` and the related features (below) to `AndroidManifest.xml`. If you forget to add these permissions, your app will crash!

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  <!-- Camera Permissions -->
  <uses-permission android:name="android.permission.CAMERA" />

  <uses-feature
      android:name="android.hardware.camera"
      android:required="false" />
  <uses-feature
      android:name="android.hardware.camera.autofocus"
      android:required="false" />

  <uses-permission android:name="android.permission.FLASHLIGHT" />
```

#### Add to Gradle
  ######  Your `android/settings.gradle` file should have following lines:

```java

include ':openalpr'
project(':openalpr').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-openalpr/android/libraries/openalpr')
include ':opencv'
project(':opencv').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-openalpr/android/libraries/opencv')
include ':react-native-openalpr'
project(':react-native-openalpr').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-openalpr/android')

```

  ###### and `android/app/build.gradle` file should have the following under dependencies:
  `compile project(':react-native-openalpr')`

#### Linking
The library is linked automatically with leptonica, opencv, tesseract, and openalpr ([openalpr](https://github.com/SandroMachado/openalpr-android)).
To make it work, copy and paste the directory with the runtime needed data to your project at path `android/app/src/main/assets/runtime_data`.

The `runtime_data` file can be found in `/Example/android/app/src/main/assets/` in this repo. Open `runtime_data/openalpr.conf` file and replace `com.awesomeproject` with your package name

#### Add to an Activity
  Open your activity, usually located in `android/app/src/main/java/[your package]/MainApplication.java`.
  Add `import com.cardash.openalpr.CameraReactPackage;` to the imports at the top of the file.
  Add `new CameraReactPackage()` to the list returned by the `getPackages()` method.

## Usage

OpenALPR exposes a camera component (based on [react-native-camera](https://github.com/lwansbrough/react-native-camera)) that is optimized to run OpenALPR image processing on a live camera stream. Among other parameters, the camera accepts a callback, `onPlateRecognized`, for when a plate is recognized.

```js
import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  StatusBar,
} from 'react-native';

import Camera from 'react-native-openalpr';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  textContainer: {
    position: 'absolute',
    top: 100,
    left: 50,
  },
  text: {
    textAlign: 'center',
    fontSize: 20,
  },
});

export default class PlateRecognizer extends React.Component {
  constructor(props) {
    super(props);

    this.camera = null;
    this.state = {
      camera: {
        aspect: Camera.constants.Aspect.fill,
      },
      plate: 'Scan a plate',
    };
  }

  onPlateRecognized = ({ plate, confidence }) => {
    if (confidence > 90) {
      this.setState({
        plate,
      })
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <Camera
          ref={(cam) => {
            this.camera = cam;
          }}
          style={styles.preview}
          aspect={this.state.camera.aspect}
          captureQuality={Camera.constants.CaptureQuality.medium}
          country="us"
          onPlateRecognized={this.onPlateRecognized}
          plateOutlineColor="#ff0000"
          showPlateOutline
          torchMode={Camera.constants.TorchMode.off}
          touchToFocus
        />
        <View style={styles.textContainer}>
          <Text style={styles.text}>{this.state.plate}</Text>
        </View>
      </View>

    );
  }
}

AppRegistry.registerComponent('PlateRecognizer', () => PlateRecognizer);

```

### Options

#### `aspect`
The aspect ratio of the camera. Can be one of:
- `Camera.constants.Aspect.stretch`
- `Camera.constants.Aspect.fit`
- `Camera.constants.Aspect.fill`

#### `captureQuality`
The resolution at which video frames are captured and analyzed. For completeness, several options are provided. However, it is strongly recommended that you stick with one of the following for the best frame rates and accuracy:
- `Camera.constants.CaptureQuality.medium` (480x360)
- `Camera.constants.CaptureQuality.480p` (640x480)

#### `country`
Specifies which OpenALPR config file to load, corresponding to the country whose plates you wish to recognize. Currently supported values are:
- `au`
- `br`
- `eu`
- `fr`
- `gb`
- `kr`
- `mx`
- `sg`
- `us`
- `vn2`

#### `onPlateRecognized`
This callback receives a hash with keys:
- `plate`, representing the recognized license plate string; and
- `confidence`, OpenALPR's confidence in the result

#### `plateOutlineColor`
Hex string specifying the color of the border to draw around the recognized plate. Example: `#ff0000` for red.

#### `showPlateOutline`
If true, this draws an outline over the recognized plate

#### `torchMode`
Turns the flashlight on or off. Can be one of:
- `Camera.constants.TorchMode.on`
- `Camera.constants.TorchMode.off`
- `Camera.constants.TorchMode.auto`

#### `touchToFocus`
If true, this focuses the camera where the user taps

## Examples
- [Example Project](https://github.com/cardash/react-native-openalpr/tree/master/Example)

## Development
- This project works with iOS and Android.  It may have some bugs depending on how the underlying native components are updated

### Running the Example project on Android While Developing

1) Clone the repo and enter the `Example` directory

```ssh
git clone https://github.com/cardash/react-native-openalpr.git
cd react-native-openalpr
cd Example
```

2) From the `Example` directory, run `npm install`

3) Copy the `android` folder from `/react-native-openalpr/android` to `/react-native-openalpr/Example/node_modules/react-native-openalpr/`

4) Open Android Studio and import the project `react-native-openalpr/Example/android` and wait until Android Studio indexes and links.

5) Run `npm start` from dir /react-native-openalpr/Example/

6) Open the path in your browser `http://localhost:8081/index.android.bundle?platform=android&dev=true&hot=false&minify=false`

7) Create file the `/react-native-openalpr/Example/android/app/src/main/assets/index.android.bundle`. Copy and paste the data from browser window to the file you just created and save.

8) Return to Android Studio and run project on your development device.

Note: If you are getting errors, double check that you have completed all of the steps above. If you are having issues running `npm start` on Mac OSX and are using homebrew, [this issue might help](https://github.com/facebook/react-native/issues/910).

## Credits
- OpenALPR built from [OpenALPR-iOS](https://github.com/twelve17/openalpr-ios)
- Project scaffold based on [react-native-camera](https://github.com/lwansbrough/react-native-camera)

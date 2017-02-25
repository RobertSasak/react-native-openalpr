# react-native-openalpr

[OpenALPR](https://github.com/openalpr/openalpr) integration for React Native. Provides a camera component that recognizes license plates in real-time. Currently only for iOS.

**gif goes here when functionality is built**

## Requirements
- iOS 9+
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
Unfortunately, the `react-native link` command is not doing everything that it needs to be doing, so continue on to the project level instructions.

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
- Ensure that all four frameworks are included in the `Link Binary With Libraries` build phase.

#### Resources
The alpr library requires a config file (`openalpr.conf`) and a data folder (`runtime_data`), both of which are included in the openalpr framework, but must be copied to the application resources:
  - Select your project on the project navigator, then, on the main pane, go to `Targets` → `<Your Project>` → `Build Phases` → `Copy Bundle Resources`, and click on the `+`.  
  - Select `Add Other...`
  - Browse *into* the `openalpr.framework` bundle, and command-select both `runtime_data` and `openalpr.conf`.  Unselect `Copy items if needed` and select `Create folder references`.

#### Bitcode
Because the OpenCV binary framework release is compiled without bitcode, the other frameworks built by this script are also built without it, which ultimately means your Xcode project  also cannot be built with bitcode enabled.  [Per this message](http://stackoverflow.com/a/32728516/868173), it sounds like we want this feature disabled for OpenCV anyway.  

To disable bitcode in your project:

- In `Build Settings` → `Build Options`, search for `Enable Bitcode` and set it to `No`.  

## Usage

OpenALPR exposes a camera component (based on [react-native-camera](https://github.com/lwansbrough/react-native-camera)) that is optimized to run OpenALPR image processing on a live camera stream. Among other parameters, the camera accepts a callback, `onPlateRecognized`, for when a plate is recognized.

`OpenALPR` takes in a stream as the first parameter, options as the second parameter, and a callback as the third parameter. The callback only returns when `OpenALPR` finds a valid license plate with a high degree of confidence.

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
    right: 150,
    bottom: 0,
  },
  text: {
    textAlign: 'center',
    fontSize: 20,
    transform: [{ rotate: '90deg'}],
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

  onPlateRecognized = ({plate, confidence}) => {
    if (confidence > 0.9) {
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
          torchMode={Camera.constants.TorchMode.off}
          captureQuality={Camera.constants.CaptureQuality.medium}
          defaultTouchToFocus
          onPlateRecognized={this.onPlateRecognized}
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

#### `torchMode`
Turns the flashlight on or off. Can be one of:
- `Camera.constants.TorchMode.on`
- `Camera.constants.TorchMode.off`
- `Camera.constants.TorchMode.auto`

#### `captureQuality`
The resolution at which video frames are captured and analyzed. For completeness, several options are provided. However, it is strongly recommended that you stick with one of the following for the best frame rates and accuracy:
- `Camera.constants.CaptureQuality.medium` (480x360)
- `Camera.constants.CaptureQuality.480p` (640x480)

#### `defaultOnFocusComponent`
If true, this focuses the camera where the user taps

#### `onPlateRecognized`
This callback receives a hash with keys:
- `plate`, representing the recognized license plate string; and
- `confidence`, OpenALPR's confidence in the result

## Examples
- [Example Project](https://github.com/cardash/react-native-openalpr/tree/master/Example)

## Development
- This project currently only works with iOS.

## Limitations
Because this library is performing OCR, orientation is highly important (e.g. if you input sideways or upside-down text, it won't be recognized properly. Rather than rotate the frame buffers depending on the device orientation, which has a time and memory cost, the decision was made to enforce a correct orientation: LandscapeRight. So:
- The camera must be oriented so that the home button is on the right in order for proper recognition to occur.

## Credits
- OpenALPR built from [OpenALPR-iOS](https://github.com/twelve17/openalpr-ios)
- Project scaffold based on [react-native-camera](https://github.com/lwansbrough/react-native-camera)

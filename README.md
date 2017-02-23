# react-native-openalpr

[OpenALPR](https://github.com/openalpr/openalpr) integration for React Native. Currently only for iOS.

**gif goes here when functionality is built**

## Installation

### Requirements

You will need a separate camera library (to be determined)

### Installation with React Native

Simply add the package and link it.

```sh
$ yarn add react-native-openalpr
$ react-native link react-native-openalpr
```

## Usage

OpenALPR takes in either an image stream or an image. It is unclear if our soon-to-be-built camera package will support image streams.

`OpenALPR` takes in a stream as the first parameter, options as the second parameter, and a callback as the third parameter. The callback only returns when `OpenALPR` finds a valid license plate with a high degree of confidence.

```js
import React from 'react'
import { StyleSheet, Dimensions, AppRegistry, View, Text, } from 'react-native'
import Camera from 'react-native-camera'
import OpenALPR from 'react-native-openalpr'

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
    height: Dimensions.get('window').height,
    width: Dimensions.get('window').width,
  },
})

const recognizePlate = stream =>
  OpenALPR(stream, {}, data => console.log(data))
  /*
    data => {
      plate: 'AKS4329',
      confidence: 86.457352,
      region_confidence: 95,
      region: 'ga',
      plate_index: 0,
      processing_time_ms: 84.982811 }
  */

const PlateRecognizer = props =>
  <View style={styles.container}>
    <Camera
      stream={stream => recognizePlate(stream)}
      style={styles.preview}
      aspect={Camera.constants.Aspect.fill}>
    </Camera>
  </View>  

AppRegistry.registerComponent('PlateRecognizer', () => PlateRecognizer)
```

### Options

#### `stream`

If OpenALPR is to receive an image stream, you can set the `stream` option to `true`. This is the default behavior. If you do not want to process an image stream, but would prefer to analyze a static image (which might be useful on slower mobile phones), you can set `stream` to `false` and `OpenALPR` will process a static image.

```js
OpenALPR(stream, {}, () => {}) // receives a stream by default
OpenALPR(image, { stream: false, }, () => {}) // receives a static image
```

## Examples

**link to example project**

## Development

This project currently only works with iOS.

**describe how to get the project running in xcode for development**

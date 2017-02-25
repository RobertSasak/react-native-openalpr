/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

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
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  overlay: {
    position: 'absolute',
    padding: 16,
    alignItems: 'center',
  },
  topOverlay: {
    top: 0,
    bottom: 0,
    right: 0,
    width: 20,
    backgroundColor: 'rgba(0, 255, 0, 0.4)',
    alignItems: 'center',
  },
  bottomOverlay: {
    top: 0,
    left: 0,
    bottom: 0,
    backgroundColor: 'rgba(255,0,0,0.4)',
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  }
});

export default class AwesomeProject extends React.Component {
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

  onPlateRecognized = (event) => {
    if (event.confidence > 0.9) {
      this.setState({
        plate: event.plate,
      })
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <StatusBar
          animated
          hidden
        />
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
        <View style={[styles.overlay, styles.topOverlay]}>
        </View>
        <View style={[styles.overlay, styles.bottomOverlay]}>
        </View>
        <View style={styles.textContainer}>
          <Text style={styles.text}>{this.state.plate}</Text>
        </View>
      </View>

    );
  }
}

AppRegistry.registerComponent('AwesomeProject', () => AwesomeProject);

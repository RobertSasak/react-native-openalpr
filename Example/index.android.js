import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  StatusBar,
  Switch,
  Picker
} from 'react-native';

import Camera from './components/camera.js'

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  textContainer: {
    position: 'absolute',
    top: 100,
    right: 50,
    bottom: 0,
  },
  switch: {
    backgroundColor: '#fff',
    position: 'absolute',
    top: 50,
    left: 50,
    padding: 10
  },
  text: {
    textAlign: 'center',
    fontSize: 20,
    backgroundColor: '#fff',
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
    left: 0,
    right: 0,
    backgroundColor: 'rgba(0, 255, 0, 0.4)',
    alignItems: 'center',
  },
  bottomOverlay: {
    right: 0,
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
      captureQuality: Camera.constants.CaptureQuality.medium,
      aspect: Camera.constants.CaptureQuality.stretch,
      rotate: false,
      torch: false,
      showBorder: true,
      color: "#ff0000",
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
        <StatusBar
          animated
          hidden
        />
        <Camera
          ref={(cam) => {
            this.camera = cam;
          }}
          style={styles.preview}
          aspect={this.state.aspect}
          captureQuality={this.state.captureQuality}
          country="us"
          onPlateRecognized={this.onPlateRecognized}
          plateOutlineColor={this.state.color}
          showPlateOutline={this.state.showBorder}
          torchMode={this.state.torch ? Camera.constants.TorchMode.on : Camera.constants.TorchMode.off}
          rotateMode={this.state.rotate ? Camera.constants.RotateMode.on : Camera.constants.RotateMode.off}
          touchToFocus
        />
        <View style={[styles.overlay, styles.topOverlay]}>
        </View>
        <View style={[styles.overlay, styles.bottomOverlay]}>
        </View>
        <View style={styles.textContainer}>
          <Text style={styles.text}>{this.state.plate}</Text>
        </View>
        <View style={styles.switch}>
          <Text style={styles.text}>orientation</Text>
          <Switch
            onValueChange={(value) => this.setState({rotate: value})}
            value={this.state.rotate} />
          <Text style={styles.text}>Torch</Text>
          <Switch
            onValueChange={(value) => this.setState({torch: value})}
            value={this.state.torch} />
          <Text style={styles.text}>Border</Text>
          <Switch
            onValueChange={(value) => this.setState({showBorder: value})}
            value={this.state.showBorder} />
          <Text style={styles.text}>Color</Text>
          <Picker
            style={{color: '#fff'}}
            selectedValue={this.state.color}
            onValueChange={(value) => this.setState({color: value})}>
              <Picker.Item label="Red" value="#ff0000" />
              <Picker.Item label="Green" value="#00ff00" />
              <Picker.Item label="Blue" value="#0000ff" />
          </Picker>
          <Text style={styles.text}>Quality</Text>
          <Picker
            style={{color: '#fff'}}
            selectedValue={this.state.captureQuality}
            onValueChange={(value) => this.setState({captureQuality: value})}>
              <Picker.Item label="Low" value={Camera.constants.CaptureQuality.low} />
              <Picker.Item label="Medium" value={Camera.constants.CaptureQuality.medium} />
              <Picker.Item label="High" value={Camera.constants.CaptureQuality.high} />
          </Picker>
          <Text style={styles.text}>Aspect</Text>
          <Picker
            style={{color: '#fff'}}
            selectedValue={this.state.aspect}
            onValueChange={(value) => this.setState({aspect: value})}>
              <Picker.Item label="Fill" value={Camera.constants.Aspect.fill} />
              <Picker.Item label="Fit" value={Camera.constants.Aspect.fit} />
              <Picker.Item label="Stretch" value={Camera.constants.Aspect.stretch} />
          </Picker>
        </View>
      </View>

    );
  }
}

AppRegistry.registerComponent('AwesomeProject', () => AwesomeProject);

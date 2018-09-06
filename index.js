import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  NativeModules,
  Platform,
  StyleSheet,
  requireNativeComponent,
  View,
} from 'react-native';

const CameraManager = NativeModules.ALPRCameraManager;
const CAMERA_REF = 'camera';

function convertNativeProps(props) {
  const newProps = { ...props };
  if (typeof props.aspect === 'string') {
    newProps.aspect = Camera.constants.Aspect[props.aspect];
  }

  if (typeof props.torchMode === 'string') {
    newProps.torchMode = Camera.constants.TorchMode[props.torchMode];
  }

  if (typeof props.captureQuality === 'string') {
    newProps.captureQuality = Camera.constants.CaptureQuality[props.captureQuality];
  }

  // delete this prop because we are going to replace it with our own
  delete newProps.onPlateRecognized;
  return newProps;
}

export default class Camera extends Component {

  static constants = {
    Aspect: CameraManager.Aspect,
    CaptureQuality: CameraManager.CaptureQuality,
    TorchMode: CameraManager.TorchMode
  };

  static propTypes = {
    ...View.propTypes,
    aspect: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureQuality: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    country: PropTypes.string,
    onPlateRecognized: PropTypes.func,
    plateOutlineColor: PropTypes.string,
    showPlateOutline: PropTypes.bool,
    torchMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    touchToFocus: PropTypes.bool,
  };

  static defaultProps = {
    aspect: CameraManager.Aspect.fill,
    captureQuality: CameraManager.CaptureQuality.medium,
    country: 'us',
    plateOutlineColor: '#0028ff',
    showPlateOutline: true,
    torchMode: CameraManager.TorchMode.off,
    touchToFocus: true,
  };

  static checkVideoAuthorizationStatus = CameraManager.checkVideoAuthorizationStatus;

  setNativeProps(props) {
    this.refs[CAMERA_REF].setNativeProps(props);
  }

  constructor() {
    super();
    this.state = {
      isAuthorized: false
    };
  }

  onPlateRecognized = (event) => {
      if(this.props.onPlateRecognized) {
        this.props.onPlateRecognized(event.nativeEvent);
      }
  }

  async componentWillMount() {
    let check = Camera.checkVideoAuthorizationStatus;

    if (check) {
      const isAuthorized = await check();
      this.setState({ isAuthorized });
    }
  }

  render() {
    const nativeProps = convertNativeProps(this.props);

    return <ALPRCamera ref={CAMERA_REF} onPlateRecognized={this.onPlateRecognized} {...nativeProps} />;
  }

}

export const constants = Camera.constants;

const ALPRCamera = requireNativeComponent(
  'ALPRCamera',
   Camera,
   {
     nativeOnly: {
       'rotateMode': true,
       'mounted': true
     }
   }
);

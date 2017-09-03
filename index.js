import React, { Component, PropTypes } from 'react';
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

  if (typeof props.flashMode === 'string') {
    newProps.flashMode = Camera.constants.FlashMode[props.flashMode];
  }

  if (typeof props.captureQuality === 'string') {
    newProps.captureQuality = Camera.constants.CaptureQuality[props.captureQuality];
  }

  if (typeof props.captureTarget === 'string') {
    newProps.captureTarget = Camera.constants.CaptureTarget[props.captureTarget];
  }


  if (props.type) {
    newProps.type = Camera.constants.Type['back'];
  }

  // delete this prop because we are going to replace it with our own
  delete newProps.onPlateRecognized;
  return newProps;
}

export default class Camera extends Component {

  static constants = {
    Aspect: CameraManager.Aspect,
    Type: CameraManager.Type,
    CaptureTarget: CameraManager.CaptureTarget,
    CaptureQuality: CameraManager.CaptureQuality,
    TorchMode: CameraManager.TorchMode,
    FlashMode: CameraManager.FlashMode,
  };

  static propTypes = {
    ...View.propTypes,
    aspect: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureTarget: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureQuality: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    type: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    playSoundOnCapture: PropTypes.bool,
    country: PropTypes.string,
    onPlateRecognized: PropTypes.func,
    plateOutlineColor: PropTypes.string,
    showPlateOutline: PropTypes.bool,
    torchMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    flashMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    touchToFocus: PropTypes.bool,
  };

  static defaultProps = {
    aspect: CameraManager.Aspect.fill,
    captureTarget: CameraManager.CaptureTarget.cameraRoll,
    captureQuality: CameraManager.CaptureQuality.medium,
    type: CameraManager.Type.back,
    playSoundOnCapture: true,
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

  capture(options) {
    const props = convertNativeProps(this.props);
    options = {
      playSoundOnCapture: props.playSoundOnCapture,
      target: props.captureTarget,
      captureQuality: props.captureQuality,
      type: 2,
      ...options
    };

    return CameraManager.capture(options);
  }

  hasFlash() {
    if (Platform.OS === 'android') {
      const props = convertNativeProps(this.props);
      return CameraManager.hasFlash({
        type: props.type
      });
    }
    return CameraManager.hasFlash();
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

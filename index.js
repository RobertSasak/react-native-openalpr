import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { NativeModules, requireNativeComponent } from 'react-native'

const { ALPRCameraManager } = NativeModules

const ALPRCamera = requireNativeComponent('ALPRCamera', Camera, {
  nativeOnly: {
    rotateMode: true,
    mounted: true,
  },
})

class Camera extends Component {
  onPlateRecognized = ({ nativeEvent }) =>
    this.props.onPlateRecognized(nativeEvent)

  render() {
    return (
      <ALPRCamera {...this.props} onPlateRecognized={this.onPlateRecognized} />
    )
  }
}

Camera.propTypes = {
  aspect: PropTypes.number,
  captureQuality: PropTypes.number,
  country: PropTypes.string,
  onPlateRecognized: PropTypes.func,
  plateOutlineColor: PropTypes.string,
  showPlateOutline: PropTypes.bool,
  torchMode: PropTypes.PropTypes.number,
  zoom: PropTypes.PropTypes.number,
  touchToFocus: PropTypes.bool,
}

Camera.defaultProps = {
  aspect: ALPRCameraManager.Aspect.fill,
  captureQuality: ALPRCameraManager.CaptureQuality.medium,
  country: 'us',
  plateOutlineColor: '#0028ff',
  showPlateOutline: true,
  zoom: 0,
  torchMode: ALPRCameraManager.TorchMode.off,
  touchToFocus: true,
  onPlateRecognized: () => {},
}

export default Camera

export const Aspect = ALPRCameraManager.Aspect
export const CaptureQuality = ALPRCameraManager.CaptureQuality
export const TorchMode = ALPRCameraManager.TorchMode
export const RotateMode = ALPRCameraManager.RotateMode

// Take a picture of what is currently seen by the user.
// Possible options: width (int), height (int) and quality (float).
// @return a Promise<String:uri>.
// @warn Currently only works on iOS.
export const takePicture = ALPRCameraManager.takePicture

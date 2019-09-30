import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { NativeModules, requireNativeComponent, Text } from 'react-native'

const { ALPRCameraManager } = NativeModules

const ALPRCamera = requireNativeComponent('ALPRCamera', Camera, {
  nativeOnly: {
    rotateMode: true,
    mounted: true,
  },
})

class Camera extends Component {
  state = {
    isAuthorized: false,
  }

  async componentDidMount() {
    const check = ALPRCameraManager.checkVideoAuthorizationStatus

    if (check) {
      const isAuthorized = await check()
      this.setState({ isAuthorized })
    }
  }

  onPlateRecognized = ({ nativeEvent }) =>
    this.props.onPlateRecognized(nativeEvent)

  render() {
    const { isAuthorized } = this.state
    if (isAuthorized) {
      return (
        <ALPRCamera
          {...this.props}
          onPlateRecognized={this.onPlateRecognized}
        />
      )
    } else {
      return <Text>You have not granted permission to use camera</Text>
    }
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
  touchToFocus: PropTypes.bool,
}

Camera.defaultProps = {
  aspect: ALPRCameraManager.Aspect.fill,
  captureQuality: ALPRCameraManager.CaptureQuality.medium,
  country: 'us',
  plateOutlineColor: '#0028ff',
  showPlateOutline: true,
  torchMode: ALPRCameraManager.TorchMode.off,
  touchToFocus: true,
  onPlateRecognized: () => {},
}

export default Camera

export const Aspect = ALPRCameraManager.Aspect
export const CaptureQuality = ALPRCameraManager.CaptureQuality
export const TorchMode = ALPRCameraManager.TorchMode

// Take a picture of what is currently seen by the user.
// Possible options: width (int), height (int) and quality (float).
// @return a Promise<String:uri>.
// @warn Currently only works on iOS.
export const takePicture = ALPRCameraManager.takePicture

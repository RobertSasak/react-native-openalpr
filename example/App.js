import React, { Component } from 'react'
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Switch,
  Picker,
  StatusBar,
  ScrollView,
  Platform,
  Image,
  Dimensions,
} from 'react-native'
import Camera, {
  Aspect,
  CaptureQuality,
  TorchMode,
  RotateMode,
  takePicture,
} from 'react-native-openalpr'
import { check, request, PERMISSIONS, RESULTS } from 'react-native-permissions'

const cameraPermission = Platform.select({
  ios: PERMISSIONS.IOS.CAMERA,
  android: PERMISSIONS.ANDROID.CAMERA,
})

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  camera: {
    flex: 1,
  },
  options: {
    position: 'absolute',
    top: 100,
    left: 20,
    right: 20,
    bottom: 20,
    borderRadius: 10,
    backgroundColor: 'white',
  },
  button: {
    position: 'absolute',
    right: 0,
    top: 20,
    color: 'white',
    width: 80,
    height: 80,
    alignItems: 'center',
  },
  buttonText: {
    fontSize: 50,
    alignSelf: 'center',
    color: '#2ab7ca',
  },
  plateText: {
    position: 'absolute',
    top: 20,
    left: 20,
    fontSize: 30,
    color: '#fe4a49',
  },
  confidenceText: {
    position: 'absolute',
    top: 20,
    left: 200,
    fontSize: 30,
    color: '#fe4a49',
  },
  switchGroup: {
    flex: 1,
    flexDirection: 'row',
    margin: 20,
    marginBottom: 0,
  },
  switchText: {
    flex: 1,
    fontSize: 25,
  },
  switch: {
    padding: 10,
  },
  pickerGroup: {},
  pickerText: {
    fontSize: 25,
    paddingTop: 20,
    textAlign: 'center',
  },
  picker: {
    backgroundColor: '#eee',
  },
  takePicture: {
    position: 'absolute',
    bottom: 50,
    right: 50,
    borderRadius: 10,
    backgroundColor: '#fe4a49',
  },
  takePictureText: {
    color: 'white',
    padding: 10,
    fontSize: 20,
  },
  pictureWrapper: {
    position: 'absolute',
    top: 20,
    left: 20,
    right: 20,
    bottom: 20,
    backgroundColor: 'gray',
    borderColor: 'white',
    borderWidth: 1,
  },
  picture: {
    transform: [{ rotate: '90deg' }],
    width: Dimensions.get('screen').width - 42,
    height: Dimensions.get('screen').height - 42,
    resizeMode: 'contain',
  },
})

const MySwitch = ({ title, value, onValueChange }) => (
  <View style={styles.switchGroup}>
    <Text style={styles.switchText}>{title}</Text>
    <Switch onValueChange={onValueChange} value={value} style={styles.switch} />
  </View>
)

const MyPicker = ({ title, selectedValue, values, onValueChange }) => (
  <View style={styles.pickerGroup}>
    <Text style={styles.pickerText}>{title}</Text>
    <Picker
      selectedValue={selectedValue}
      onValueChange={onValueChange}
      style={styles.picker}>
      {values.map(({ label, value }) => (
        <Picker.Item key={value} label={label} value={value} />
      ))}
    </Picker>
  </View>
)

const colorOptions = [
  { label: 'Red', value: '#ff0000' },
  { label: 'Green', value: '#00ff00' },
  { label: 'Blue', value: '#0000ff' },
]

const aspectOptions = [
  { label: 'Fill', value: Aspect.fill },
  { label: 'Fit', value: Aspect.fit },
  { label: 'Stretch', value: Aspect.stretch },
]

const qualityOptions = [
  { label: 'Low', value: CaptureQuality.low },
  { label: 'Medium', value: CaptureQuality.medium },
  { label: 'High', value: CaptureQuality.high },
]

const countryOptions = [
  { label: 'eu', value: 'eu' },
  { label: 'us', value: 'us' },
]

export default class App extends Component {
  state = {
    showOptions: false,
    plate: 'Point at a plate',
    confidence: '',
    error: null,
    showCamera: false,
    // Camera options
    camera: {
      aspect: Aspect.fill,
    },
    captureQuality: CaptureQuality.medium,
    aspect: CaptureQuality.stretch,
    rotateMode: false,
    torchMode: false,
    showPlateOutline: true,
    plateOutlineColor: '#ff0000',
    country: 'eu',
    touchToFocus: true,
  }

  async componentDidMount() {
    this.checkPermission()
  }

  async checkPermission() {
    switch (await check(cameraPermission)) {
      case RESULTS.UNAVAILABLE:
        this.setState({
          error:
            'This feature is not available (on this device / in this context)',
        })
        break
      case RESULTS.DENIED:
        this.requestPermission()
        break
      case RESULTS.GRANTED:
        this.setState({
          showCamera: true,
        })
        break
      case RESULTS.BLOCKED:
        this.setState({
          error: 'The permission is denied and not requestable anymore',
        })
        break
    }
  }

  async requestPermission() {
    switch (await request(cameraPermission)) {
      case RESULTS.UNAVAILABLE:
        this.setState({
          error:
            'This feature is not available (on this device / in this context)',
        })
        break
      case RESULTS.DENIED:
        this.setState({
          error: 'The permission has been denied',
        })
        break
      case RESULTS.GRANTED:
        this.setState({
          showCamera: true,
        })
        break
      case RESULTS.BLOCKED:
        this.setState({
          error: 'The permission is denied and not requestable anymore',
        })
        break
    }
  }

  onPlateRecognized = ({ plate, confidence }) => {
    this.setState({
      plate,
      confidence,
    })
  }

  toggleOptions = () => this.setState({ showOptions: !this.state.showOptions })

  onTakePicturePress = async () => {
    const picture = await takePicture({
      width: 4032,
      height: 3024,
      quality: 0.8,
    })
    this.setState({
      picture,
    })
  }

  onClosePicturePress = () =>
    this.setState({
      picture: null,
    })

  render() {
    const {
      showCamera,
      error,
      showOptions,
      plate,
      plateOutlineColor,
      showPlateOutline,
      captureQuality,
      aspect,
      rotateMode,
      torchMode,
      country,
      confidence,
      touchToFocus,
      picture,
    } = this.state
    return (
      <View style={styles.container}>
        {/* <StatusBar  /> */}
        {showCamera && (
          <Camera
            style={styles.camera}
            aspect={aspect}
            captureQuality={captureQuality}
            country={country}
            onPlateRecognized={this.onPlateRecognized}
            plateOutlineColor={plateOutlineColor}
            showPlateOutline={showPlateOutline}
            torchMode={this.state.torchMode ? TorchMode.on : TorchMode.off}
            // rotateMode={this.state.rotateMode ? RotateMode.on : RotateMode.off}
            touchToFocus={touchToFocus}
          />
        )}
        {error && <Text>{error}</Text>}
        <TouchableOpacity style={styles.button} onPress={this.toggleOptions}>
          <Text style={styles.buttonText}>{showOptions ? '✕' : '☰'}</Text>
        </TouchableOpacity>
        <Text style={styles.plateText}>{plate}</Text>
        <Text style={styles.confidenceText}>
          {confidence ? confidence + '%' : ''}
        </Text>
        <TouchableOpacity
          style={styles.takePicture}
          onPress={this.onTakePicturePress}>
          <Text style={styles.takePictureText}>Take picure</Text>
        </TouchableOpacity>
        {picture && (
          <TouchableOpacity
            style={styles.pictureWrapper}
            onPress={this.onClosePicturePress}>
            <Image style={styles.picture} source={{ uri: picture }} />
          </TouchableOpacity>
        )}
        {showOptions && (
          <ScrollView style={styles.options}>
            <MySwitch
              title="Orientation"
              value={rotateMode}
              onValueChange={value => this.setState({ rotateMode: value })}
            />
            <MySwitch
              title="Torch"
              value={torchMode}
              onValueChange={value => this.setState({ torchMode: value })}
            />
            <MySwitch
              title="Show plate outline"
              value={showPlateOutline}
              onValueChange={value =>
                this.setState({ showPlateOutline: value })
              }
            />
            <MySwitch
              title="Touch to focus"
              value={touchToFocus}
              onValueChange={value => this.setState({ touchToFocus: value })}
            />
            <MyPicker
              title="Color"
              selectedValue={plateOutlineColor}
              onValueChange={value =>
                this.setState({ plateOutlineColor: value })
              }
              values={colorOptions}
            />
            <MyPicker
              title="Quality"
              selectedValue={captureQuality}
              onValueChange={value => this.setState({ captureQuality: value })}
              values={qualityOptions}
            />
            <MyPicker
              title="Aspect"
              selectedValue={aspect}
              onValueChange={value => this.setState({ aspect: value })}
              values={aspectOptions}
            />
            <MyPicker
              title="Country"
              selectedValue={country}
              onValueChange={value => this.setState({ country: value })}
              values={countryOptions}
            />
          </ScrollView>
        )}
      </View>
    )
  }
}

import React from 'react'
import { StyleSheet } from 'react-native'

import Camera from 'react-native-openalpr'

const App = () => {
    return (
        <>
            <Camera
                showPlateOutline
                aspects={Camera.constants.Aspect.fill}
                style={styles.camera}
            />
        </>
    )
}

const styles = StyleSheet.create({
    camera: {
        flex: 1,
    },
})

export default App

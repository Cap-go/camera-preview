# Capacitor Camera Preview Plugin

<a href="https://capgo.app/"><img src='https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png' alt='Capgo - Instant updates for capacitor'/></a>

<div align="center">
<h2><a href="https://capgo.app/">Check out: Capgo — Live updates for capacitor</a></h2>
</div>

<p>
  Capacitor plugin that allows camera interaction from Javascript and HTML<br>(based on cordova-plugin-camera-preview).
</p>
<br>

This plugin is compatible Capacitor 4 only.

**PR's are greatly appreciated.**

-- [@riderx](https://github.com/riderx), current maintainers

<!-- # Features

<ul>
  <li>Start a camera preview from HTML code.</li>
  <li>Maintain HTML interactivity.</li>
  <li>Drag the preview box.</li>
  <li>Set camera color effect.</li>
  <li>Send the preview box to back of the HTML content.</li>
  <li>Set a custom position for the camera preview box.</li>
  <li>Set a custom size for the preview box.</li>
  <li>Set a custom alpha for the preview box.</li>
  <li>Set the focus mode, zoom, color effects, exposure mode, white balance mode and exposure compensation</li>
  <li>Tap to focus</li>
</ul> -->

# Installation

```
yarn add @capgo/camera-preview

or

npm install @capgo/camera-preview
```

Then run

```
npx cap sync
```

## Extra Android installation steps

**Important** `camera-preview` 3+ requires Gradle 7.
Open `android/app/src/main/AndroidManifest.xml` and above the closing `</manifest>` tag add this line to request the CAMERA permission:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

```

For more help consult the [Capacitor docs](https://capacitorjs.com/docs/android/configuration#configuring-androidmanifestxml).

## Extra iOS installation steps

You will need to add two permissions to `Info.plist`. Follow the [Capacitor docs](https://capacitorjs.com/docs/ios/configuration#configuring-infoplist) and add permissions with the raw keys `NSCameraUsageDescription` and `NSMicrophoneUsageDescription`. `NSMicrophoneUsageDescription` is only required, if audio will be used. Otherwise set the `disableAudio` option to `true`, which also disables the microphone permission request.

## Extra Web installation steps

Add `import '@capgo/camera-preview'` to you entry script in ionic on `app.module.ts`, so capacitor can register the web platform from the plugin

### API

<docgen-index>

* [`start(...)`](#start)
* [`stop()`](#stop)
* [`capture(...)`](#capture)
* [`captureSample(...)`](#capturesample)
* [`getSupportedFlashModes()`](#getsupportedflashmodes)
* [`getHorizontalFov()`](#gethorizontalfov)
* [`setFlashMode(...)`](#setflashmode)
* [`flip()`](#flip)
* [`setOpacity(...)`](#setopacity)
* [`stopRecordVideo()`](#stoprecordvideo)
* [`startRecordVideo(...)`](#startrecordvideo)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### start(...)

```typescript
start(options: CameraPreviewOptions) => Promise<void>
```

Start the camera preview instance.

| Param         | Type                                                                  | Description                                  |
| ------------- | --------------------------------------------------------------------- | -------------------------------------------- |
| **`options`** | <code><a href="#camerapreviewoptions">CameraPreviewOptions</a></code> | the options to start the camera preview with |

**Since:** 0.0.1

--------------------


### stop()

```typescript
stop() => Promise<void>
```

Stop the camera preview instance.

**Since:** 0.0.1

--------------------


### capture(...)

```typescript
capture(options: CameraPreviewPictureOptions) => Promise<{ value: string; }>
```

Switch camera.

| Param         | Type                                                                                | Description                           |
| ------------- | ----------------------------------------------------------------------------------- | ------------------------------------- |
| **`options`** | <code><a href="#camerapreviewpictureoptions">CameraPreviewPictureOptions</a></code> | the options to switch the camera with |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

**Since:** 0.0.1

--------------------


### captureSample(...)

```typescript
captureSample(options: CameraSampleOptions) => Promise<{ value: string; }>
```

Capture a sample image.

| Param         | Type                                                                | Description                                  |
| ------------- | ------------------------------------------------------------------- | -------------------------------------------- |
| **`options`** | <code><a href="#camerasampleoptions">CameraSampleOptions</a></code> | the options to capture the sample image with |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

**Since:** 0.0.1

--------------------


### getSupportedFlashModes()

```typescript
getSupportedFlashModes() => Promise<{ result: CameraPreviewFlashMode[]; }>
```

Get supported flash modes.

**Returns:** <code>Promise&lt;{ result: CameraPreviewFlashMode[]; }&gt;</code>

**Since:** 0.0.1

--------------------


### getHorizontalFov()

```typescript
getHorizontalFov() => Promise<{ result: any; }>
```

Get horizontal field of view.

**Returns:** <code>Promise&lt;{ result: any; }&gt;</code>

**Since:** 0.0.1

--------------------


### setFlashMode(...)

```typescript
setFlashMode(options: { flashMode: CameraPreviewFlashMode | string; }) => Promise<void>
```

Set flash mode.

| Param         | Type                                | Description                            |
| ------------- | ----------------------------------- | -------------------------------------- |
| **`options`** | <code>{ flashMode: string; }</code> | the options to set the flash mode with |

**Since:** 0.0.1

--------------------


### flip()

```typescript
flip() => Promise<void>
```

Flip camera.

**Since:** 0.0.1

--------------------


### setOpacity(...)

```typescript
setOpacity(options: CameraOpacityOptions) => Promise<void>
```

Set opacity.

| Param         | Type                                                                  | Description                                |
| ------------- | --------------------------------------------------------------------- | ------------------------------------------ |
| **`options`** | <code><a href="#cameraopacityoptions">CameraOpacityOptions</a></code> | the options to set the camera opacity with |

**Since:** 0.0.1

--------------------


### stopRecordVideo()

```typescript
stopRecordVideo() => Promise<void>
```

Stop recording video.

**Since:** 0.0.1

--------------------


### startRecordVideo(...)

```typescript
startRecordVideo(options: CameraPreviewOptions) => Promise<void>
```

Start recording video.

| Param         | Type                                                                  | Description                               |
| ------------- | --------------------------------------------------------------------- | ----------------------------------------- |
| **`options`** | <code><a href="#camerapreviewoptions">CameraPreviewOptions</a></code> | the options to start recording video with |

**Since:** 0.0.1

--------------------


### Interfaces


#### CameraPreviewOptions

| Prop                               | Type                 | Description                                                                                                                                                   |
| ---------------------------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`parent`**                       | <code>string</code>  | Parent element to attach the video preview element to (applicable to the web platform only)                                                                   |
| **`className`**                    | <code>string</code>  | Class name to add to the video preview element (applicable to the web platform only)                                                                          |
| **`width`**                        | <code>number</code>  | The preview width in pixels, default window.screen.width                                                                                                      |
| **`height`**                       | <code>number</code>  | The preview height in pixels, default window.screen.height                                                                                                    |
| **`x`**                            | <code>number</code>  | The x origin, default 0 (applicable to the android and ios platforms only)                                                                                    |
| **`y`**                            | <code>number</code>  | The y origin, default 0 (applicable to the android and ios platforms only)                                                                                    |
| **`toBack`**                       | <code>boolean</code> | Brings your html in front of your preview, default false (applicable to the android only)                                                                     |
| **`paddingBottom`**                | <code>number</code>  | The preview bottom padding in pixes. Useful to keep the appropriate preview sizes when orientation changes (applicable to the android and ios platforms only) |
| **`rotateWhenOrientationChanged`** | <code>boolean</code> | Rotate preview when orientation changes (applicable to the ios platforms only; default value is true)                                                         |
| **`position`**                     | <code>string</code>  | Choose the camera to use 'front' or 'rear', default 'front'                                                                                                   |
| **`storeToFile`**                  | <code>boolean</code> | Defaults to false - Capture images to a file and return the file path instead of returning base64 encoded data                                                |
| **`disableExifHeaderStripping`**   | <code>boolean</code> | Defaults to false - Android Only - Disable automatic rotation of the image, and let the browser deal with it (keep reading on how to achieve it)              |
| **`enableHighResolution`**         | <code>boolean</code> | Defaults to false - iOS only - Activate high resolution image capture so that output images are from the highest resolution possible on the device *          |
| **`disableAudio`**                 | <code>boolean</code> | Defaults to false - Web only - Disables audio stream to prevent permission requests and output switching                                                      |
| **`lockAndroidOrientation`**       | <code>boolean</code> | Android Only - Locks device orientation when camera is showing.                                                                                               |
| **`enableOpacity`**                | <code>boolean</code> | Defaults to false - Android and Web only. Set if camera preview can change opacity.                                                                           |
| **`enableZoom`**                   | <code>boolean</code> | Defaults to false - Android only. Set if camera preview will support pinch to zoom.                                                                           |


#### CameraPreviewPictureOptions

| Prop          | Type                                                    | Description                                                                          |
| ------------- | ------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| **`height`**  | <code>number</code>                                     | The picture height, optional, default 0 (Device default)                             |
| **`width`**   | <code>number</code>                                     | The picture width, optional, default 0 (Device default)                              |
| **`quality`** | <code>number</code>                                     | The picture quality, 0 - 100, default 85                                             |
| **`format`**  | <code><a href="#pictureformat">PictureFormat</a></code> | The picture format, jpeg or png, default jpeg on `Web`. quality has no effect on png |


#### CameraSampleOptions

| Prop          | Type                | Description                              |
| ------------- | ------------------- | ---------------------------------------- |
| **`quality`** | <code>number</code> | The picture quality, 0 - 100, default 85 |


#### CameraOpacityOptions

| Prop          | Type                | Description                                           |
| ------------- | ------------------- | ----------------------------------------------------- |
| **`opacity`** | <code>number</code> | The percent opacity to set for camera view, default 1 |


### Type Aliases


#### CameraPosition

<code>"rear" | "front"</code>


#### PictureFormat

<code>"jpeg" | "png"</code>


#### CameraPreviewFlashMode

<code>"off" | "on" | "auto" | "red-eye" | "torch"</code>

</docgen-api>

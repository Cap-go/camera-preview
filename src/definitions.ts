export type CameraPosition = "rear" | "front";
export interface CameraPreviewOptions {
  /** Parent element to attach the video preview element to (applicable to the web platform only) */
  parent?: string;
  /** Class name to add to the video preview element (applicable to the web platform only) */
  className?: string;
  /** The preview width in pixels, default window.screen.width */
  width?: number;
  /** The preview height in pixels, default window.screen.height */
  height?: number;
  /** The x origin, default 0 (applicable to the android and ios platforms only) */
  x?: number;
  /** The y origin, default 0 (applicable to the android and ios platforms only) */
  y?: number;
  /**  Brings your html in front of your preview, default false (applicable to the android only) */
  toBack?: boolean;
  /** The preview bottom padding in pixes. Useful to keep the appropriate preview sizes when orientation changes (applicable to the android and ios platforms only) */
  paddingBottom?: number;
  /** Rotate preview when orientation changes (applicable to the ios platforms only; default value is true) */
  rotateWhenOrientationChanged?: boolean;
  /** Choose the camera to use 'front' or 'rear', default 'front' */
  position?: CameraPosition | string;
  /** Defaults to false - Capture images to a file and return the file path instead of returning base64 encoded data */
  storeToFile?: boolean;
  /** Defaults to false - Android Only - Disable automatic rotation of the image, and let the browser deal with it (keep reading on how to achieve it) */
  disableExifHeaderStripping?: boolean;
  /** Defaults to false - iOS only - Activate high resolution image capture so that output images are from the highest resolution possible on the device **/
  enableHighResolution?: boolean;
  /** Defaults to false - Web only - Disables audio stream to prevent permission requests and output switching */
  disableAudio?: boolean;
  /**  Android Only - Locks device orientation when camera is showing. */
  lockAndroidOrientation?: boolean;
  /** Defaults to false - Android and Web only.  Set if camera preview can change opacity. */
  enableOpacity?: boolean;
  /** Defaults to false - Android only.  Set if camera preview will support pinch to zoom. */
  enableZoom?: boolean;
  /** default to false - IOS only. Set the CameraPreview to use the video mode preset */
  cameraMode?: boolean;
}

export interface CameraPreviewPictureOptions {
  /** The picture height, optional, default 0 (Device default) */
  height?: number;
  /** The picture width, optional, default 0 (Device default) */
  width?: number;
  /** The picture quality, 0 - 100, default 85 */
  quality?: number;
  /** The picture format, jpeg or png, default jpeg on `Web`.
   *
   * quality has no effect on png */
  format?: PictureFormat;
}

export type PictureFormat = "jpeg" | "png";

export interface CameraSampleOptions {
  /** The picture quality, 0 - 100, default 85 */
  quality?: number;
}

export type CameraPreviewFlashMode =
  | "off"
  | "on"
  | "auto"
  | "red-eye"
  | "torch";

export interface CameraOpacityOptions {
  /** The percent opacity to set for camera view, default 1 */
  opacity?: number;
}

export interface CameraPreviewPlugin {
  /**
   * Start the camera preview instance.
   * @param {CameraPreviewOptions} options the options to start the camera preview with
   * @returns {Promise<void>} an Promise that resolves when the instance is started
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  start(options: CameraPreviewOptions): Promise<void>;
  /**
   * Stop the camera preview instance.
   * @returns {Promise<void>} an Promise that resolves when the instance is stopped
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  stop(): Promise<void>;
  /**
   * Switch camera.
   * @param {CameraPreviewOptions} options the options to switch the camera with
   * @returns {Promise<void>} an Promise that resolves when the camera is switched
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  capture(options: CameraPreviewPictureOptions): Promise<{ value: string }>;
  /**
   * Capture a sample image.
   * @param {CameraSampleOptions} options the options to capture the sample image with
   * @returns {Promise<string>} an Promise that resolves with the sample image as a base64 encoded string
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  captureSample(options: CameraSampleOptions): Promise<{ value: string }>;
  /**
   * Get supported flash modes.
   * @returns {Promise<CameraPreviewFlashMode[]>} an Promise that resolves with the supported flash modes
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  getSupportedFlashModes(): Promise<{
    result: CameraPreviewFlashMode[];
  }>;
  /**
   * Get horizontal field of view.
   * @returns {Promise<any>} an Promise that resolves with the horizontal field of view
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  getHorizontalFov(): Promise<{
    result: any;
  }>;
  /**
   * Set flash mode.
   * @param options the options to set the flash mode with
   * @returns {Promise<void>} an Promise that resolves when the flash mode is set
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  setFlashMode(options: {
    flashMode: CameraPreviewFlashMode | string;
  }): Promise<void>;
  /**
   * Flip camera.
   * @returns {Promise<void>} an Promise that resolves when the camera is flipped
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  flip(): Promise<void>;
  /**
   * Set opacity.
   * @param {CameraOpacityOptions} options the options to set the camera opacity with
   * @returns {Promise<void>} an Promise that resolves when the camera color effect is set
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  setOpacity(options: CameraOpacityOptions): Promise<void>;
  /**
   * Stop recording video.
   * @param {CameraPreviewOptions} options the options to stop recording video with
   * @returns {Promise<{videoFilePath: string}>} an Promise that resolves when the camera zoom is set
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  stopRecordVideo(): Promise<{ videoFilePath: string }>;
  /**
   * Start recording video.
   * @param {CameraPreviewOptions} options the options to start recording video with
   * @returns {Promise<void>} an Promise that resolves when the video recording is started
   * @throws An error if the something went wrong
   * @since 0.0.1
   */
  startRecordVideo(options: CameraPreviewOptions): Promise<void>;
}

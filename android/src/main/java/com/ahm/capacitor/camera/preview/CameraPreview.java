package com.ahm.capacitor.camera.preview;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;

@CapacitorPlugin(
  name = "CameraPreview",
  permissions = {
    @Permission(
      strings = { CAMERA, RECORD_AUDIO },
      alias = CameraPreview.CAMERA_WITH_AUDIO_PERMISSION_ALIAS
    ),
    @Permission(
      strings = { CAMERA },
      alias = CameraPreview.CAMERA_ONLY_PERMISSION_ALIAS
    ),
  }
)
public class CameraPreview
  extends Plugin
  implements CameraActivity.CameraPreviewListener {

  static final String CAMERA_WITH_AUDIO_PERMISSION_ALIAS = "cameraWithAudio";
  static final String CAMERA_ONLY_PERMISSION_ALIAS = "cameraOnly";

  private static String VIDEO_FILE_PATH = "";
  private static final String VIDEO_FILE_EXTENSION = ".mp4";

  private String captureCallbackId = "";
  private String snapshotCallbackId = "";
  private String recordCallbackId = "";
  private String cameraStartCallbackId = "";

  // keep track of previously specified orientation to support locking orientation:
  private int previousOrientationRequest =
    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

  private CameraActivity fragment;
  private final int containerViewId = 20;

  @PluginMethod
  public void start(PluginCall call) {
    boolean disableAudio = call.getBoolean("disableAudio", false);
    String permissionAlias = disableAudio
      ? CAMERA_ONLY_PERMISSION_ALIAS
      : CAMERA_WITH_AUDIO_PERMISSION_ALIAS;

    if (PermissionState.GRANTED.equals(getPermissionState(permissionAlias))) {
      startCamera(call);
    } else {
      requestPermissionForAlias(
        permissionAlias,
        call,
        "handleCameraPermissionResult"
      );
    }
  }

  @PluginMethod
  public void flip(PluginCall call) {
    try {
      fragment.switchCamera();
      call.resolve();
    } catch (Exception e) {
      Logger.debug(getLogTag(), "Camera flip exception: " + e);
      call.reject("failed to flip camera");
    }
  }

  @PluginMethod
  public void setOpacity(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.error("Camera is not running");
      return;
    }

    bridge.saveCall(call);
    Float opacity = Objects.requireNonNull(call.getFloat("opacity", 1F));
    fragment.setOpacity(opacity);
  }

  @PluginMethod
  public void capture(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }
    bridge.saveCall(call);
    captureCallbackId = call.getCallbackId();

    Integer quality = Objects.requireNonNull(call.getInt("quality", 85));
    // Image Dimensions - Optional
    Integer width = Objects.requireNonNull(call.getInt("width", 0));
    Integer height = Objects.requireNonNull(call.getInt("height", 0));
    fragment.takePicture(width, height, quality);
  }

  @PluginMethod
  public void captureSample(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }
    bridge.saveCall(call);
    snapshotCallbackId = call.getCallbackId();

    Integer quality = Objects.requireNonNull(call.getInt("quality", 85));
    fragment.takeSnapshot(quality);
  }

  @PluginMethod
  public void getSupportedPictureSizes(final PluginCall call) {
    CameraManager cameraManager = (CameraManager) this.getContext()
      .getSystemService(Context.CAMERA_SERVICE);

    JSArray ret = new JSArray();
    try {
      String[] cameraIdList = cameraManager.getCameraIdList();
      for (String cameraId : cameraIdList) {
        CameraCharacteristics characteristics =
          cameraManager.getCameraCharacteristics(cameraId);

        // Determine the facing of the camera
        Integer lensFacing = characteristics.get(
          CameraCharacteristics.LENS_FACING
        );
        String facing = "Unknown";
        if (lensFacing != null) {
          switch (lensFacing) {
            case CameraCharacteristics.LENS_FACING_FRONT:
              facing = "Front";
              break;
            case CameraCharacteristics.LENS_FACING_BACK:
              facing = "Back";
              break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
              facing = "External";
              break;
          }
        }

        StreamConfigurationMap map = characteristics.get(
          CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        );
        if (map == null) {
          continue;
        }

        Size[] jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
        JSObject camera = new JSObject();
        camera.put("facing", facing);
        JSArray supportedPictureSizes = new JSArray();
        if (jpegSizes != null) {
          for (Size size : jpegSizes) {
            JSObject sizeJson = new JSObject();
            sizeJson.put("width", size.getWidth());
            sizeJson.put("height", size.getHeight());
            supportedPictureSizes.put(sizeJson);
          }
          camera.put("supportedPictureSizes", supportedPictureSizes);
          ret.put(camera);
        }
      }
      JSObject finalRet = new JSObject();
      finalRet.put("supportedPictureSizes", ret);
      call.resolve(finalRet);
    } catch (CameraAccessException ex) {
      Logger.error(getLogTag(), "Cannot call getSupportedPictureSizes", ex);
      call.reject(
        String.format("Cannot call getSupportedPictureSizes. Error: %s", ex)
      );
    }
  }

  @PluginMethod
  public void stop(final PluginCall call) {
    bridge
      .getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            FrameLayout containerView = getBridge()
              .getActivity()
              .findViewById(containerViewId);

            // allow orientation changes after closing camera:
            getBridge()
              .getActivity()
              .setRequestedOrientation(previousOrientationRequest);

            if (containerView != null) {
              ((ViewGroup) getBridge().getWebView().getParent()).removeView(
                  containerView
                );
              getBridge().getWebView().setBackgroundColor(Color.WHITE);
              FragmentManager fragmentManager = getActivity()
                .getFragmentManager();
              FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
              fragmentTransaction.remove(fragment);
              fragmentTransaction.commit();
              fragment = null;

              call.resolve();
            } else {
              call.reject("camera already stopped");
            }
          }
        }
      );
  }

  @PluginMethod
  public void getSupportedFlashModes(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();
    List<String> supportedFlashModes;
    supportedFlashModes = params.getSupportedFlashModes();
    JSONArray jsonFlashModes = new JSONArray();

    if (supportedFlashModes != null) {
      for (int i = 0; i < supportedFlashModes.size(); i++) {
        jsonFlashModes.put(supportedFlashModes.get(i));
      }
    }

    JSObject jsObject = new JSObject();
    jsObject.put("result", jsonFlashModes);
    call.resolve(jsObject);
  }

  @PluginMethod
  public void getHorizontalFov(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    float horizontalViewAngle = params.getHorizontalViewAngle();

    JSObject jsObject = new JSObject();
    jsObject.put("result", horizontalViewAngle);
    call.resolve(jsObject);
  }

  @PluginMethod
  public void setFlashMode(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }

    String flashMode = call.getString("flashMode");
    if (flashMode == null || flashMode.isEmpty()) {
      call.reject("flashMode required parameter is missing");
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    List<String> supportedFlashModes;
    supportedFlashModes = camera.getParameters().getSupportedFlashModes();
    if (
      supportedFlashModes != null && supportedFlashModes.contains(flashMode)
    ) {
      params.setFlashMode(flashMode);
    } else {
      call.reject("Flash mode not recognised: " + flashMode);
      return;
    }

    fragment.setCameraParameters(params);

    call.resolve();
  }

  @PluginMethod
  public void startRecordVideo(final PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }
    final String filename = "videoTmp";
    VIDEO_FILE_PATH = getActivity().getCacheDir().toString() + "/";

    final String position = Objects.requireNonNull(
      call.getString("position", "front")
    );
    final Integer width = Objects.requireNonNull(call.getInt("width", 0));
    final Integer height = Objects.requireNonNull(call.getInt("height", 0));
    final Boolean withFlash = Objects.requireNonNull(
      call.getBoolean("withFlash", false)
    );
    final Integer maxDuration = Objects.requireNonNull(
      call.getInt("maxDuration", 0)
    );
    // final Integer quality = Objects.requireNonNull(call.getInt("quality", 0));
    bridge.saveCall(call);
    recordCallbackId = call.getCallbackId();

    bridge
      .getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            fragment.startRecord(
              getFilePath(),
              position,
              width,
              height,
              70,
              withFlash,
              maxDuration
            );
          }
        }
      );

    call.resolve();
  }

  @PluginMethod
  public void stopRecordVideo(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }

    System.out.println("stopRecordVideo - Callbackid=" + call.getCallbackId());

    bridge.saveCall(call);
    recordCallbackId = call.getCallbackId();

    // bridge.getActivity().runOnUiThread(new Runnable() {
    //     @Override
    //     public void run() {
    //         fragment.stopRecord();
    //     }
    // });

    fragment.stopRecord();
    // call.resolve();
  }

  @PermissionCallback
  private void handleCameraPermissionResult(PluginCall call) {
    boolean disableAudio = call.getBoolean("disableAudio", false);
    String permissionAlias = disableAudio
      ? CAMERA_ONLY_PERMISSION_ALIAS
      : CAMERA_WITH_AUDIO_PERMISSION_ALIAS;

    if (PermissionState.GRANTED.equals(getPermissionState(permissionAlias))) {
      startCamera(call);
    } else {
      call.reject("Permission failed");
    }
  }

  private void startCamera(final PluginCall call) {
    String position = call.getString("position");

    if (position == null || position.isEmpty() || "rear".equals(position)) {
      position = "back";
    } else {
      position = "front";
    }

    @NonNull
    final Integer x = Objects.requireNonNull(call.getInt("x", 0));
    @NonNull
    final Integer y = Objects.requireNonNull(call.getInt("y", 0));
    @NonNull
    final Integer width = Objects.requireNonNull(call.getInt("width", 0));
    @NonNull
    final Integer height = Objects.requireNonNull(call.getInt("height", 0));
    @NonNull
    final Integer paddingBottom = Objects.requireNonNull(
      call.getInt("paddingBottom", 0)
    );
    final Boolean toBack = Objects.requireNonNull(
      call.getBoolean("toBack", false)
    );
    final Boolean storeToFile = Objects.requireNonNull(
      call.getBoolean("storeToFile", false)
    );
    final Boolean enableOpacity = Objects.requireNonNull(
      call.getBoolean("enableOpacity", false)
    );
    final Boolean enableZoom = Objects.requireNonNull(
      call.getBoolean("enableZoom", false)
    );
    final Boolean disableExifHeaderStripping = Objects.requireNonNull(
      call.getBoolean("disableExifHeaderStripping", false)
    );
    final Boolean lockOrientation = Objects.requireNonNull(
      call.getBoolean("lockAndroidOrientation", false)
    );
    previousOrientationRequest = getBridge()
      .getActivity()
      .getRequestedOrientation();

    fragment = new CameraActivity();
    fragment.setEventListener(this);
    fragment.defaultCamera = position;
    fragment.tapToTakePicture = false;
    fragment.dragEnabled = false;
    fragment.tapToFocus = true;
    fragment.disableExifHeaderStripping = disableExifHeaderStripping;
    fragment.storeToFile = storeToFile;
    fragment.toBack = toBack;
    fragment.enableOpacity = enableOpacity;
    fragment.enableZoom = enableZoom;
    fragment.disableAudio = call.getBoolean("disableAudio", false);

    bridge
      .getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            DisplayMetrics metrics = getBridge()
              .getActivity()
              .getResources()
              .getDisplayMetrics();
            // lock orientation if specified in options:
            if (lockOrientation) {
              getBridge()
                .getActivity()
                .setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_LOCKED
                );
            }

            // offset
            int computedX = (int) TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP,
              x,
              metrics
            );
            int computedY = (int) TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP,
              y,
              metrics
            );

            // size
            int computedWidth;
            int computedHeight;
            int computedPaddingBottom;

            if (paddingBottom != 0) {
              computedPaddingBottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                paddingBottom,
                metrics
              );
            } else {
              computedPaddingBottom = 0;
            }

            if (width != 0) {
              computedWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                width,
                metrics
              );
            } else {
              Display defaultDisplay = getBridge()
                .getActivity()
                .getWindowManager()
                .getDefaultDisplay();
              final Point size = new Point();
              defaultDisplay.getSize(size);

              computedWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                size.x,
                metrics
              );
            }

            if (height != 0) {
              computedHeight =
                (int) TypedValue.applyDimension(
                  TypedValue.COMPLEX_UNIT_DIP,
                  height,
                  metrics
                ) -
                computedPaddingBottom;
            } else {
              Display defaultDisplay = getBridge()
                .getActivity()
                .getWindowManager()
                .getDefaultDisplay();
              final Point size = new Point();
              defaultDisplay.getSize(size);

              computedHeight =
                (int) TypedValue.applyDimension(
                  TypedValue.COMPLEX_UNIT_PX,
                  size.y,
                  metrics
                ) -
                computedPaddingBottom;
            }

            fragment.setRect(
              computedX,
              computedY,
              computedWidth,
              computedHeight
            );

            FrameLayout containerView = getBridge()
              .getActivity()
              .findViewById(containerViewId);
            if (containerView == null) {
              containerView = new FrameLayout(
                getActivity().getApplicationContext()
              );
              containerView.setId(containerViewId);

              getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
              ((ViewGroup) getBridge().getWebView().getParent()).addView(
                  containerView
                );
              if (toBack) {
                getBridge()
                  .getWebView()
                  .getParent()
                  .bringChildToFront(getBridge().getWebView());
                setupBroadcast();
              }

              FragmentManager fragmentManager = getBridge()
                .getActivity()
                .getFragmentManager();
              FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
              fragmentTransaction.add(containerView.getId(), fragment);
              fragmentTransaction.commit();

              // NOTE: we don't return invoke call.resolve here because it must be invoked in onCameraStarted
              // otherwise the plugin start method might resolve/return before the camera is actually set in CameraActivity
              // onResume method (see this line mCamera = Camera.open(defaultCameraId);) and the next subsequent plugin
              // method invocations (for example, getSupportedFlashModes) might fails with "Camera is not running" error
              // because camera is not available yet and hasCamera method will return false
              // Please also see https://developer.android.com/reference/android/hardware/Camera.html#open%28int%29
              bridge.saveCall(call);
              cameraStartCallbackId = call.getCallbackId();
            } else {
              call.reject("camera already started");
            }
          }
        }
      );
  }

  @Override
  protected void handleOnResume() {
    super.handleOnResume();
  }

  @Override
  public void onPictureTaken(String originalPicture) {
    JSObject jsObject = new JSObject();
    jsObject.put("value", originalPicture);
    bridge.getSavedCall(captureCallbackId).resolve(jsObject);
  }

  @Override
  public void onPictureTakenError(String message) {
    bridge.getSavedCall(captureCallbackId).reject(message);
  }

  @Override
  public void onSnapshotTaken(String originalPicture) {
    JSObject jsObject = new JSObject();
    jsObject.put("value", originalPicture);
    bridge.getSavedCall(snapshotCallbackId).resolve(jsObject);
  }

  @Override
  public void onSnapshotTakenError(String message) {
    bridge.getSavedCall(snapshotCallbackId).reject(message);
  }

  @Override
  public void onFocusSet(int pointX, int pointY) {}

  @Override
  public void onFocusSetError(String message) {}

  @Override
  public void onBackButton() {}

  @Override
  public void onCameraStarted() {
    PluginCall pluginCall = bridge.getSavedCall(cameraStartCallbackId);
    pluginCall.resolve();
    bridge.releaseCall(pluginCall);
  }

  @Override
  public void onStartRecordVideo() {}

  @Override
  public void onStartRecordVideoError(String message) {
    bridge.getSavedCall(recordCallbackId).reject(message);
  }

  @Override
  public void onStopRecordVideo(String file) {
    PluginCall pluginCall = bridge.getSavedCall(recordCallbackId);
    JSObject jsObject = new JSObject();
    jsObject.put("videoFilePath", file);
    pluginCall.resolve(jsObject);
  }

  @Override
  public void onStopRecordVideoError(String error) {
    bridge.getSavedCall(recordCallbackId).reject(error);
  }

  private boolean hasView(PluginCall call) {
    if (fragment == null) {
      return false;
    }

    return true;
  }

  private boolean hasCamera(PluginCall call) {
    if (!this.hasView(call)) {
      return false;
    }

    if (fragment.getCamera() == null) {
      return false;
    }

    return true;
  }

  private String getFilePath() {
    String fileName = "videoTmp";

    int i = 1;

    while (
      new File(VIDEO_FILE_PATH + fileName + VIDEO_FILE_EXTENSION).exists()
    ) {
      // Add number suffix if file exists
      fileName = "videoTmp" + '_' + i;
      i++;
    }

    return VIDEO_FILE_PATH + fileName + VIDEO_FILE_EXTENSION;
  }

  private void setupBroadcast() {
    /** When touch event is triggered, relay it to camera view if needed so it can support pinch zoom */

    getBridge().getWebView().setClickable(true);
    getBridge()
      .getWebView()
      .setOnTouchListener(
        new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            if (
              (null != fragment) &&
              (fragment.toBack == true) &&
              null != fragment.frameContainerLayout
            ) {
              fragment.frameContainerLayout.dispatchTouchEvent(event);
            }
            return false;
          }
        }
      );
  }
}

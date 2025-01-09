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
import android.util.Log;
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
  implements CameraActivityV2.CameraPreviewListener {

  static final String CAMERA_WITH_AUDIO_PERMISSION_ALIAS = "cameraWithAudio";
  static final String CAMERA_ONLY_PERMISSION_ALIAS = "cameraOnly";

  private static String VIDEO_FILE_PATH = "";
  private static final String VIDEO_FILE_EXTENSION = ".mp4";

  private String captureCallbackId = "";
  private String snapshotCallbackId = "";
  private String recordCallbackId = "";
  private String cameraStartCallbackId = "";

  private int previousOrientationRequest =
    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
  private CameraActivityV2 fragment;
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
    captureCallbackId = call.getCallbackId();
    bridge.saveCall(call);
    fragment.takePicture();
  }

  @PluginMethod
  public void startRecordVideo(final PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }

    final Integer width = Objects.requireNonNull(call.getInt("width", 0));
    final Integer height = Objects.requireNonNull(call.getInt("height", 0));
    final Boolean withFlash = Objects.requireNonNull(
      call.getBoolean("withFlash", false)
    );

    final String filename = "videoTmp";
    VIDEO_FILE_PATH = getActivity().getCacheDir().toString() + "/";

    bridge.saveCall(call);
    recordCallbackId = call.getCallbackId();

    bridge
      .getActivity()
      .runOnUiThread(() -> {
        fragment.startRecordVideo(getFilePath(), width, height, withFlash);
      });

    call.resolve();
  }

  @PluginMethod
  public void stopRecordVideo(PluginCall call) {
    if (!this.hasCamera(call)) {
      call.reject("Camera is not running");
      return;
    }

    bridge.saveCall(call);
    recordCallbackId = call.getCallbackId();
    fragment.stopRecordVideo();
  }

  @PluginMethod
  public void stop(final PluginCall call) {
    bridge
      .getActivity()
      .runOnUiThread(() -> {
        FrameLayout containerView = getBridge()
          .getActivity()
          .findViewById(containerViewId);

        getBridge()
          .getActivity()
          .setRequestedOrientation(previousOrientationRequest);

        if (containerView != null) {
          ((ViewGroup) getBridge().getWebView().getParent()).removeView(
              containerView
            );
          getBridge().getWebView().setBackgroundColor(Color.WHITE);
          FragmentManager fragmentManager = getActivity().getFragmentManager();
          FragmentTransaction fragmentTransaction =
            fragmentManager.beginTransaction();
          fragmentTransaction.remove(fragment);
          fragmentTransaction.commit();
          fragment = null;

          call.resolve();
        } else {
          call.reject("camera already stopped");
        }
      });
  }

  private void startCamera(final PluginCall call) {
    String position = call.getString("position");
    position = (position == null ||
        position.isEmpty() ||
        "rear".equals(position))
      ? "back"
      : "front";

    final Integer x = Objects.requireNonNull(call.getInt("x", 0));
    final Integer y = Objects.requireNonNull(call.getInt("y", 0));
    final Integer width = Objects.requireNonNull(call.getInt("width", 0));
    final Integer height = Objects.requireNonNull(call.getInt("height", 0));
    final Integer paddingBottom = Objects.requireNonNull(
      call.getInt("paddingBottom", 0)
    );
    final Boolean toBack = Objects.requireNonNull(
      call.getBoolean("toBack", false)
    );
    final Boolean enableOpacity = Objects.requireNonNull(
      call.getBoolean("enableOpacity", false)
    );
    final Boolean enableZoom = Objects.requireNonNull(
      call.getBoolean("enableZoom", false)
    );
    final Boolean lockOrientation = Objects.requireNonNull(
      call.getBoolean("lockAndroidOrientation", false)
    );

    previousOrientationRequest = getBridge()
      .getActivity()
      .getRequestedOrientation();

    fragment = new CameraActivityV2();
    fragment.setEventListener(this);
    //    fragment.defaultCamera = position;
    //    fragment.enableOpacity = enableOpacity;
    fragment.enableZoom = enableZoom;
    fragment.setDisableAudio(call.getBoolean("disableAudio", false));

    bridge
      .getActivity()
      .runOnUiThread(() -> {
        DisplayMetrics metrics = getBridge()
          .getActivity()
          .getResources()
          .getDisplayMetrics();

        if (lockOrientation) {
          getBridge()
            .getActivity()
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }

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
        int computedPaddingBottom = paddingBottom != 0
          ? (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            paddingBottom,
            metrics
          )
          : 0;

        int computedWidth;
        int computedHeight;

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

        fragment.setRect(computedX, computedY, computedWidth, computedHeight);

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
          }

          FragmentManager fragmentManager = getBridge()
            .getActivity()
            .getFragmentManager();
          FragmentTransaction fragmentTransaction =
            fragmentManager.beginTransaction();
          fragmentTransaction.add(containerView.getId(), fragment);
          fragmentTransaction.commit();

          bridge.saveCall(call);
          cameraStartCallbackId = call.getCallbackId();
        } else {
          call.reject("camera already started");
        }
      });
  }

  private boolean hasCamera(PluginCall call) {
    return fragment != null && fragment.getCamera() != null;
  }

  private String getFilePath() {
    String fileName = "videoTmp";
    int i = 1;
    while (
      new File(VIDEO_FILE_PATH + fileName + VIDEO_FILE_EXTENSION).exists()
    ) {
      fileName = "videoTmp" + '_' + i;
      i++;
    }
    return VIDEO_FILE_PATH + fileName + VIDEO_FILE_EXTENSION;
  }

  // CameraPreviewListener Implementation
  @Override
  public void onPictureTaken(String originalPicture) {
    JSObject jsObject = new JSObject();
    jsObject.put("value", originalPicture);
    PluginCall call = bridge.getSavedCall(captureCallbackId);
    if (call != null) {
      call.resolve(jsObject);
    }
  }

  @Override
  public void onPictureTakenError(String message) {
    Log.e("CameraPreview", String.format("onPictureTakenError: %s", message));
    PluginCall pluginCall = bridge.getSavedCall(captureCallbackId);
    if (pluginCall == null) {
      return;
    }
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
  public void onCameraStarted() {
    PluginCall pluginCall = bridge.getSavedCall(cameraStartCallbackId);
    if (pluginCall == null) {
      return;
    }
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

  // Add this method to handle camera permissions
  @PermissionCallback
  private void handleCameraPermissionResult(PluginCall call) {
    boolean disableAudio = call.getBoolean("disableAudio", false);
    String permissionAlias = disableAudio
      ? CAMERA_ONLY_PERMISSION_ALIAS
      : CAMERA_WITH_AUDIO_PERMISSION_ALIAS;

    if (getPermissionState(permissionAlias) == PermissionState.GRANTED) {
      startCamera(call);
    } else {
      call.reject("User denied camera permission");
    }
  }
}

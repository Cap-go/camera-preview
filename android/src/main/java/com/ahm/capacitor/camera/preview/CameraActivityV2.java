package com.ahm.capacitor.camera.preview;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Fragment;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.size.Size;
import com.otaliastudios.cameraview.size.SizeSelectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CameraActivityV2 extends Fragment {

    private CameraView camera;
    private CameraPreviewListener eventListener;
    private boolean enableOpacity = false;
    private boolean enableZoom = false;
    private boolean disableAudio = false;
    private String defaultCamera = "back";
    private int width;
    private int height;
    private int x;
    private int y;

    public interface CameraPreviewListener {
        void onPictureTaken(String originalPicture);
        void onPictureTakenError(String message);
        void onSnapshotTaken(String originalPicture);
        void onSnapshotTakenError(String message);
        void onCameraStarted();
        void onStartRecordVideo();
        void onStartRecordVideoError(String message);
        void onStopRecordVideo(String file);
        void onStopRecordVideoError(String error);
    }

    public void setEventListener(CameraPreviewListener listener) {
        this.eventListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                           @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        
        camera = new CameraView(getContext());
        
        // Configure camera settings
        camera.setFacing(defaultCamera.equals("front") ? Facing.FRONT : Facing.BACK);
        camera.setMode(Mode.PICTURE);
        camera.setFlash(Flash.OFF);
        camera.setAudio(disableAudio ? com.otaliastudios.cameraview.controls.Audio.OFF 
                                   : com.otaliastudios.cameraview.controls.Audio.ON);
        
        if (enableZoom) {
            // camera.setPinchToZoom(true);
        }
        
        camera.setPreviewStreamSize(SizeSelectors.and(
            SizeSelectors.minWidth(width > 0 ? width : 1024),
            SizeSelectors.minHeight(height > 0 ? height : 768),
            SizeSelectors.biggest()
        ));

        FrameLayout frameLayout = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            width > 0 ? width : FrameLayout.LayoutParams.MATCH_PARENT,
            height > 0 ? height : FrameLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(x, y, 0, 0);
        camera.setLayoutParams(params);
        
        frameLayout.addView(camera);
        
        if (enableOpacity) {
            camera.setAlpha(0.7f);
        }

        return frameLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera != null) {
            camera.open();
            if (eventListener != null) {
                eventListener.onCameraStarted();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.destroy();
        }
    }

    public void takePicture() {
        if (camera != null) {
            camera.takePicture();
            camera.addCameraListener(new com.otaliastudios.cameraview.CameraListener() {
                @Override
                public void onPictureTaken(@NonNull PictureResult result) {
                    result.toFile(new File(getContext().getCacheDir(), "picture.jpg"), file -> {
                        try {
                            byte[] bytes = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                bytes = Files.readAllBytes(file.toPath());
                            }
                            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                            if (eventListener != null) {
                                eventListener.onPictureTaken(base64);
                            }
                        } catch (IOException e) {
                            if (eventListener != null) {
                                eventListener.onPictureTakenError(e.getMessage());
                            }
                        }
                    });
                }
            });
        }
    }

    public void startRecordVideo(String filePath) {
        if (camera != null) {
            try {
                camera.setMode(Mode.VIDEO);
                File file = new File(filePath);
                
                // Ensure parent directory exists
                file.getParentFile().mkdirs();
                
                // Add the listener before starting recording
                camera.addCameraListener(new com.otaliastudios.cameraview.CameraListener() {
                    @Override
                    public void onVideoTaken(@NonNull VideoResult result) {
                        if (eventListener != null) {
                            eventListener.onStopRecordVideo(filePath);
                        }
                        camera.removeCameraListener(this);
                    }

                    @Override
                    public void onVideoRecordingStart() {
                        if (eventListener != null) {
                            eventListener.onStartRecordVideo();
                        }
                    }

                    @Override
                    public void onVideoRecordingEnd() {
                        // Video recording has ended naturally
                        Log.d("CameraActivityV2", "Video recording ended naturally");
                    }

                    @Override
                    public void onCameraError(@NonNull CameraException error) {
                        Log.e("CameraActivityV2", "Camera error during recording", error);
                        if (eventListener != null) {
                            eventListener.onStopRecordVideoError(error.getMessage());
                        }
                        camera.removeCameraListener(this);
                    }
                });

                // Ensure we're in a good state before recording
                if (!camera.isOpened()) {
                    camera.open();
                }

                // Start recording with a small delay to ensure camera is ready
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        camera.takeVideo(file);
                    } catch (Exception e) {
                        Log.e("CameraActivityV2", "Failed to start recording", e);
                        if (eventListener != null) {
                            eventListener.onStartRecordVideoError(e.getMessage());
                        }
                    }
                }, 500);

            } catch (Exception e) {
                Log.e("CameraActivityV2", "Error setting up video recording", e);
                if (eventListener != null) {
                    eventListener.onStartRecordVideoError(e.getMessage());
                }
            }
        }
    }

    public void stopRecordVideo() {
        if (camera != null) {
            try {
                if (camera.isTakingVideo()) {
                    camera.stopVideo();
                } else {
                    Log.w("CameraActivityV2", "Attempted to stop video when not recording");
                }
            } catch (Exception e) {
                Log.e("CameraActivityV2", "Error stopping video recording", e);
                if (eventListener != null) {
                    eventListener.onStopRecordVideoError(e.getMessage());
                }
            }
        }
    }

    public void switchCamera() {
        if (camera != null) {
            Facing currentFacing = camera.getFacing();
            camera.setFacing(currentFacing == Facing.BACK ? Facing.FRONT : Facing.BACK);
        }
    }

    public void setFlashMode(String flashMode) {
        if (camera != null) {
            switch (flashMode.toLowerCase()) {
                case "on":
                    camera.setFlash(Flash.ON);
                    break;
                case "off":
                    camera.setFlash(Flash.OFF);
                    break;
                case "auto":
                    camera.setFlash(Flash.AUTO);
                    break;
                case "torch":
                    camera.setFlash(Flash.TORCH);
                    break;
            }
        }
    }

    public void setOpacity(float opacity) {
        if (camera != null) {
            camera.setAlpha(opacity);
        }
    }

    public void setZoom(float zoom) {
        if (camera != null) {
            camera.setZoom(zoom);
        }
    }

    public void setRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        if (camera != null && camera.getParent() instanceof FrameLayout) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) camera.getLayoutParams();
            params.width = width;
            params.height = height;
            params.setMargins(x, y, 0, 0);
            camera.setLayoutParams(params);
        }
    }

    public void setDisableAudio(boolean disable) {
        this.disableAudio = disable;
        if (camera != null) {
            camera.setAudio(disable ? com.otaliastudios.cameraview.controls.Audio.OFF 
                                  : com.otaliastudios.cameraview.controls.Audio.ON);
        }
    }

    public CameraView getCamera() {
        return camera;
    }
}

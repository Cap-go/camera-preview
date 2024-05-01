package com.ahm.capacitor.camera.preview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import androidx.annotation.NonNull;

class CustomTextureView
  extends TextureView
  implements TextureView.SurfaceTextureListener {

  private final String TAG = "CustomTextureView";

  CustomTextureView(Context context) {
    super(context);
  }

  @Override
  public void onSurfaceTextureAvailable(
    @NonNull SurfaceTexture surface,
    int width,
    int height
  ) {}

  @Override
  public void onSurfaceTextureSizeChanged(
    @NonNull SurfaceTexture surface,
    int width,
    int height
  ) {}

  @Override
  public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
    return true;
  }

  @Override
  public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
}

#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN macro.
CAP_PLUGIN(CameraPreview, "CameraPreview",
           CAP_PLUGIN_METHOD(start, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stop, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(capture, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(captureSample, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(flip, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setOpacity, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getSupportedFlashModes, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getHorizontalFov, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setFlashMode, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(startRecordVideo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopRecordVideo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getSupportedPictureSizes, CAPPluginReturnPromise);
           // New lens methods
           CAP_PLUGIN_METHOD(listLense, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(changeLense, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getCurrentLense, CAPPluginReturnPromise);
)

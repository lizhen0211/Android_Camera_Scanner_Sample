package com.lz.scanner.camera;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by lz on 2018/4/21.
 */

public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private Camera mCamera;

    private int displayOrientation;

    private Camera.Parameters mParams;

    public CameraManager() {
    }

    /**
     * Opens a rear-facing camera with {@link Camera#open(int)}, if one exists, or opens camera 0.
     */
    public void open() {

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.w(TAG, "No cameras!");
            return;
        }

        int index = 0;
        while (index < numCameras) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                break;
            }
            index++;
        }

        Camera camera;
        if (index < numCameras) {
            Log.i(TAG, "Opening camera #" + index);
            camera = Camera.open(index);
        } else {
            Log.i(TAG, "No camera facing back; returning camera #0");
            camera = Camera.open(0);
        }

        this.mCamera = camera;
    }

    /**
     * 设置相机参数
     *
     * @param screenWidth
     * @param screenHeight
     */
    public void setCameraParams(int screenWidth, int screenHeight) {
        if (mCamera == null) {
            Log.e(TAG, "camera is null when setCameraParams");
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        //设置预览窗口大小
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        if (sizes.size() > 0) {
            Camera.Size maxSupportedSize = CameraUtil.getMaxSupportedSize(sizes);
            Log.e(TAG + "previewSize", maxSupportedSize.width + ":" + maxSupportedSize.height);
            int[] appropriateSize = CameraUtil.getAppropriateSize(sizes, maxSupportedSize, screenWidth, screenHeight);
            params.setPreviewSize(appropriateSize[0], appropriateSize[1]);
        }

        //设置对焦模式
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Autofocus mode is supported
            // 设置连续对焦模式
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //设置图片质量
        params.setJpegQuality(100);

        //设置图片大小
        List<Camera.Size> supportedPictureSizes = params.getSupportedPictureSizes();
        if (supportedPictureSizes.size() > 0) {
            for (Camera.Size size : supportedPictureSizes) {
                //Log.e(TAG + "PictureSize", size.width + ":" + size.height);
            }
            //params.setPictureSize(,);
        }
        Log.e(TAG, params.getPictureSize().width + ":" + params.getPictureSize().height);

        //设置相机显示方向
        mCamera.setDisplayOrientation(displayOrientation);
        params.setRotation(displayOrientation);
        mCamera.setParameters(params);
        mParams = params;
    }

   /* public synchronized Camera.Parameters getParameters() {
        return mCamera.getParameters();
    }*/

    public synchronized Camera.Parameters getParameters() {
        return mParams;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public synchronized void setOneShotPreviewCallback(Camera.PreviewCallback cb) {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(cb);
        }
    }

    public synchronized void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public synchronized boolean isReleaseCamera() {
        return mCamera == null;
    }
}

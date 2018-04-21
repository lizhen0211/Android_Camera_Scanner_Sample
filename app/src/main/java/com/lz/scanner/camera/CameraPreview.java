package com.lz.scanner.camera;

/**
 * Created by lz on 2017/6/27.
 */

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CameraManager mCameraManager;
    private PreviewCallBack previewCallBack;
    private int screenWidth;
    private int screenHeight;
    private Context context;
    private boolean hasCameraInited;
    private boolean hasStartPreview;

    private static final String TAG = CameraPreview.class.getSimpleName();

    public CameraPreview(Context context, CameraManager cameraManager, int screenWidth, int screenHeight) {
        super(context);
        mCameraManager = cameraManager;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.context = context;
        initCamera(context, screenWidth, screenHeight);
        mCamera = mCameraManager.getCamera();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void onResume() {
        if (!hasCameraInited) {
            initCamera(context, screenWidth, screenHeight);
            mCamera = mCameraManager.getCamera();
        }
        if (!hasStartPreview) {
            //开启预览
            startPreview();
        }
    }

    private void startPreview() {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            //设置预览回调
            mCamera.setOneShotPreviewCallback(this);
            //通过SurfaceView显示取景画面
            mCamera.setPreviewDisplay(mHolder);
            //开始预览
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
        hasStartPreview = true;
    }

    /**
     * 初始化相机
     *
     * @param context
     * @param screenWidth
     * @param screenHeight
     */
    private void initCamera(Context context, int screenWidth, int screenHeight) {
        //设置相机显示方向
        mCameraManager.setDisplayOrientation(CameraUtil.getDisplayOrientation(context));
        mCameraManager.open();
        //设置相机参数
        mCameraManager.setCameraParams(screenWidth, screenHeight);
        hasCameraInited = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasCameraInited) {
            initCamera(context, screenWidth, screenHeight);
            mCamera = mCameraManager.getCamera();
        }
        if (!hasStartPreview) {
            //开启预览
            startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        mCameraManager.releaseCamera();
        hasCameraInited = false;
        hasStartPreview = false;
        Log.e(TAG, "releaseCamera");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        startPreview();
    }

    public void setPreviewCallBack(PreviewCallBack previewCallBack) {
        this.previewCallBack = previewCallBack;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e(TAG, data.toString());
        if (previewCallBack != null) {
            previewCallBack.onPreviewFrame(data, camera);
        }
    }

    public void setOneShotPreviewCallback() {
        mCamera.setOneShotPreviewCallback(this);
    }

    public interface PreviewCallBack {
        void onPreviewFrame(byte[] data, Camera camera);
    }
}
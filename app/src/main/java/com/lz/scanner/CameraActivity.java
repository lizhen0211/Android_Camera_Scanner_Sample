package com.lz.scanner;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lz.scanner.camera.CameraPreview;
import com.lz.scanner.camera.CameraUtil;
import com.lz.scanner.camera.open.OpenCameraInterface;

import java.util.List;

public class CameraActivity extends Activity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private CameraPreview mPreview;
    private Camera mCamera;
    private int displayOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        FrameLayout previewLayout = (FrameLayout) findViewById(R.id.camera_preview_layout);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();

        //检测相机是否有相机硬件
        if (OpenCameraInterface.checkCameraHardware(this)) {
            mCamera = OpenCameraInterface.open();
            //设置相机参数
            setCameraParams(screenWidth, screenHeight);
            //初始化相机
            initCamera(previewLayout);
        } else {
            Toast.makeText(CameraActivity.this, "not support", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 初始化相机
     *
     * @param previewLayout
     */
    private void initCamera(FrameLayout previewLayout) {
        mPreview = new CameraPreview(this, mCamera);
        //添加预览回调
        mPreview.setPreviewCallBack(previewCallBack);
        mPreview = new CameraPreview(this, mCamera);
        previewLayout.addView(mPreview);
    }

    /**
     * 设置相机参数
     *
     * @param screenWidth
     * @param screenHeight
     */
    private void setCameraParams(int screenWidth, int screenHeight) {
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
        displayOrientation = CameraUtil.getDisplayOrientation(this);
        mCamera.setDisplayOrientation(displayOrientation);
        params.setRotation(displayOrientation);
        mCamera.setParameters(params);
    }

    private CameraPreview.PreviewCallBack previewCallBack = new CameraPreview.PreviewCallBack() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    };

    /*private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }*/
}

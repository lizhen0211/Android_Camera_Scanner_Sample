package com.lz.scanner;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lz.scanner.camera.CameraPreview;
import com.lz.scanner.camera.CameraUtil;
import com.lz.scanner.camera.open.OpenCameraInterface;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class CameraActivity extends Activity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private CameraPreview mPreview;
    private Camera mCamera;
    private int displayOrientation;
    //预览窗口距离屏幕的间距 单位：Pix
    private int previewMarginLeltAndRight = 50;
    //预览窗口距离屏幕顶部的间距
    private int previewMarginTop = 150;
    private int previewMarginTopDp;
    //预览窗口图片显示视图
    private ImageView previewIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        previewIV = (ImageView) findViewById(R.id.preview_iv);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) previewIV.getLayoutParams();
        previewMarginTopDp = DisplayUtil.dip2px(this, previewMarginTop);
        layoutParams.setMargins(layoutParams.leftMargin, previewMarginTopDp, layoutParams.rightMargin, layoutParams.bottomMargin);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

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
        public void onPreviewFrame(final byte[] data, Camera camera) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Camera.Parameters parameters = mCamera.getParameters();
                    int previewWidth = parameters.getPreviewSize().width;
                    int previewHeight = parameters.getPreviewSize().height;
                    YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), previewWidth, previewHeight, null);

                    //压缩图片
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 100, out);
                    //图片采样
                    byte[] bytes = out.toByteArray();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = false;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    Matrix matrix = new Matrix();
                    matrix.preRotate(displayOrientation);

                    int bitmapWidth = bitmap.getWidth(); // 得到图片的宽，高
                    int bitmapHeight = bitmap.getHeight();
                    int wh = bitmapWidth > bitmapHeight ? bitmapHeight : bitmapWidth;// 裁切后所取的正方形区域边长
                    int retX = bitmapWidth > bitmapHeight ? (bitmapWidth - bitmapHeight) / 2 : 0;// 基于原图，取正方形左上角x坐标
                    int retY = bitmapWidth > bitmapHeight ? 0 : (bitmapHeight - bitmapWidth) / 2;

                    int previewWindowMarginDp = DisplayUtil.dip2px(CameraActivity.this, previewMarginLeltAndRight);
                    int newWidth = wh - previewWindowMarginDp;
                    int newHeight = wh - previewWindowMarginDp;
                    int bitmapCutOffset;
                    if (previewMarginTopDp > 0) {
                        //bitmapCutOffset = previewWidth / 2 - previewMarginTop - (newHeight + bitmapMarginDp / 2) / 2;
                        bitmapCutOffset = previewWidth / 2 - previewMarginTopDp - (newHeight) / 2;
                    } else {
                        bitmapCutOffset = 0;
                    }

                    final Bitmap newbitmap = Bitmap.createBitmap(bitmap, retX + previewMarginLeltAndRight / 2 - bitmapCutOffset, retY + previewMarginLeltAndRight / 2, newWidth, newHeight, matrix, false);
                    //final Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

                    Log.e(TAG, Thread.currentThread().getName());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                previewIV.setImageBitmap(newbitmap);
                                mPreview.setOneShotPreviewCallback();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    };

    /*private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }*/
}

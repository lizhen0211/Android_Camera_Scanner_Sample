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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lz.scanner.camera.CameraManager;
import com.lz.scanner.camera.CameraPreview;
import com.lz.scanner.camera.CameraThreadPool;
import com.lz.scanner.camera.CameraUtil;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends Activity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private CameraPreview mPreview;
    //预览窗口距离屏幕的间距
    private static final int previewMarginDip = 50;
    //预览窗口距离屏幕间距 单位：像素
    private int previewWindowMarginPx;
    //预览窗口距离屏幕顶部的间距 等于0时预览框居中
    private static final int previewMarginTopDip = 0;
    //预览窗口距离屏幕顶部的间距 单位：像素
    private int previewMarginTopPx;
    //预览窗口图片显示视图
    private ImageView previewIV;
    private boolean isPreviewIVVisiable = true;
    //扫描视图
    private ScanView scanView;

    private CameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        previewMarginTopPx = DisplayUtil.dip2px(this, previewMarginTopDip);
        previewWindowMarginPx = DisplayUtil.dip2px(CameraActivity.this, previewMarginDip);

        previewIV = (ImageView) findViewById(R.id.preview_iv);
        previewIV.setVisibility(isPreviewIVVisiable ? View.VISIBLE : View.GONE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) previewIV.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, previewMarginTopPx, layoutParams.rightMargin, layoutParams.bottomMargin);
        if (previewMarginTopPx == 0) {
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        } else {
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }

        FrameLayout previewLayout = (FrameLayout) findViewById(R.id.camera_preview_layout);

        //检测相机是否有相机硬件
        if (CameraUtil.checkCameraHardware(this)) {
            cameraManager = new CameraManager();
            //初始化相机
            initCamera(previewLayout, previewCallBack);
            //初始化扫描窗口
            initScanView();
        } else {
            Toast.makeText(CameraActivity.this, "not support", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();
    }

    private void initScanView() {
        scanView = (ScanView) findViewById(R.id.scan_view);
        Camera.Parameters parameters = cameraManager.getParameters();
        if (parameters != null) {
            Camera.Size previewSize = parameters.getPreviewSize();
            if (previewMarginTopPx > 0) {
                int scanRecTop = previewMarginTopPx;
                int scanRecLeft = previewWindowMarginPx;
                int scanRecRight = previewWindowMarginPx + (previewSize.height - 2 * previewWindowMarginPx);
                int scanRecBottom = scanRecTop + (previewSize.height - 2 * previewWindowMarginPx);
                scanView.setScanRec(new Rect(scanRecLeft, scanRecTop, scanRecRight, scanRecBottom));
            } else {
                int scanRecTop = previewSize.width / 2 - (previewSize.height - 2 * previewWindowMarginPx) / 2;
                int scanRecLeft = previewWindowMarginPx;
                int scanRecRight = previewWindowMarginPx + (previewSize.height - 2 * previewWindowMarginPx);
                int scanRecBottom = scanRecTop + (previewSize.height - 2 * previewWindowMarginPx);
                scanView.setScanRec(new Rect(scanRecLeft, scanRecTop, scanRecRight, scanRecBottom));
            }
        }
    }

    /**
     * 初始化相机
     *
     * @param previewLayout
     */
    private void initCamera(FrameLayout previewLayout, CameraPreview.PreviewCallBack previewCallBack) {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        mPreview = new CameraPreview(this, cameraManager, previewCallBack, screenWidth, screenHeight);
        //添加预览回调
        //mPreview.setPreviewCallBack(previewCallBack);
        previewLayout.addView(mPreview);
    }

    private CameraPreview.PreviewCallBack previewCallBack = new CameraPreview.PreviewCallBack() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            CameraThreadPool.addTask(new Runnable() {
                @Override
                public void run() {
                    if (cameraManager.isReleaseCamera()) {
                        Log.e(TAG, "has release 1");
                    }
                    Camera.Parameters parameters = cameraManager.getParameters();
                    Camera.Size cameraPreviewSize = parameters.getPreviewSize();
                    int previewWidth = cameraPreviewSize.width;
                    int previewHeight = cameraPreviewSize.height;
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
                    matrix.preRotate(cameraManager.getDisplayOrientation());

                    int bitmapWidth = bitmap.getWidth(); // 得到图片的宽，高
                    int bitmapHeight = bitmap.getHeight();
                    int wh = bitmapWidth > bitmapHeight ? bitmapHeight : bitmapWidth;// 裁切后所取的正方形区域边长
                    int retX = bitmapWidth > bitmapHeight ? (bitmapWidth - bitmapHeight) / 2 : 0;// 基于原图，取正方形左上角x坐标
                    int retY = bitmapWidth > bitmapHeight ? 0 : (bitmapHeight - bitmapWidth) / 2;

                    int newWidth = wh - 2 * previewWindowMarginPx;
                    int newHeight = wh - 2 * previewWindowMarginPx;
                    int bitmapCutOffset;
                    if (previewMarginTopPx > 0) {
                        //bitmapCutOffset = previewWidth / 2 - previewMarginTopPx - (newHeight + bitmapMarginDp / 2) / 2;
                        bitmapCutOffset = previewWidth / 2 - previewMarginTopPx - newHeight / 2;
                    } else {
                        bitmapCutOffset = 0;
                    }

                    final Bitmap newbitmap = Bitmap.createBitmap(bitmap, retX + previewWindowMarginPx - bitmapCutOffset, retY + previewWindowMarginPx, newWidth, newHeight, matrix, false);
                    //final Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isPreviewIVVisiable) {
                                    previewIV.setImageBitmap(newbitmap);
                                }
                                if (!cameraManager.isReleaseCamera()) {
                                    cameraManager.setOneShotPreviewCallback(mPreview);
                                } else {
                                    Log.e(TAG, "has release 2");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onOpeningCameraFailed() {
            Log.e(TAG, "onOpeningCameraFailed");
        }
    };
}

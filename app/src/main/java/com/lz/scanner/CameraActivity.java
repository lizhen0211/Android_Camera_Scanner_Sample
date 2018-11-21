package com.lz.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
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
    //预览窗口距离屏幕左右的间距
    private static final int previewMarginLeftOrRightDip = 50;
    //预览窗口距离屏幕间距 单位：像素
    private int previewMarginLeftOrRightPx;
    //预览窗口距离屏幕顶部的间距 等于0时预览框居中
    private static final int previewMarginTopDip = 30;
    //预览窗口距离屏幕顶部的间距 单位：像素
    private int previewMarginTopPx;
    private static final int previewMarginOffsetDip = 0;
    //预览窗口距离屏幕上下偏移量 使正方形变成矩形 单位：像素
    private int previewMarginOffsetPx;
    //扫描窗口左、上、右、下
    private int scanRecLeft, scanRecTop, scanRecRight, scanRecBottom;

    //预览窗口图片显示视图
    private ImageView previewIV;
    private boolean isPreviewIVVisiable = true;
    //扫描视图
    private ScanView scanView;

    private CameraManager cameraManager;
    private boolean hasScanViewInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        scanView = (ScanView) findViewById(R.id.scan_view);

        previewMarginTopPx = DisplayUtil.dip2px(this, previewMarginTopDip);
        previewMarginLeftOrRightPx = DisplayUtil.dip2px(CameraActivity.this, previewMarginLeftOrRightDip);
        previewMarginOffsetPx = DisplayUtil.dip2px(this, previewMarginOffsetDip);

        previewIV = (ImageView) findViewById(R.id.preview_iv);
        previewIV.setVisibility(isPreviewIVVisiable ? View.VISIBLE : View.GONE);
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
        if (CameraUtil.checkCameraHardware(this)) {
            // 处理：1、锁屏恢复后，重新初始化Camera；2、从Camera权限页面返回case
            mPreview.onResume();
            //处理从Camera权限页面返回case.当进入CameraActivity时没有Camera权限，ScanView不会被初始化.
            if (!hasScanViewInitialized) {
                initScanView();
            }
        }
    }

    private void initScanView() {
        Camera.Parameters parameters = cameraManager.getParameters();
        if (parameters != null) {
            Camera.Size previewSize = parameters.getPreviewSize();
            if (previewMarginTopPx > 0) {
                scanRecTop = previewMarginTopPx + previewMarginOffsetPx;
                scanRecLeft = previewMarginLeftOrRightPx;
                scanRecRight = previewMarginLeftOrRightPx + (previewSize.height - 2 * previewMarginLeftOrRightPx);
                scanRecBottom = scanRecTop + (previewSize.height - 2 * previewMarginLeftOrRightPx) - 2 * previewMarginOffsetPx;
                scanView.setScanRec(new Rect(scanRecLeft, scanRecTop, scanRecRight, scanRecBottom));
            } else {
                scanRecTop = previewSize.width / 2 - (previewSize.height - 2 * previewMarginLeftOrRightPx) / 2 + previewMarginOffsetPx;
                scanRecLeft = previewMarginLeftOrRightPx;
                scanRecRight = previewMarginLeftOrRightPx + (previewSize.height - 2 * previewMarginLeftOrRightPx);
                scanRecBottom = scanRecTop + (previewSize.height - 2 * previewMarginLeftOrRightPx) - 2 * previewMarginOffsetPx;
                scanView.setScanRec(new Rect(scanRecLeft, scanRecTop, scanRecRight, scanRecBottom));
            }
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) previewIV.getLayoutParams();
            layoutParams.setMargins(previewMarginLeftOrRightPx, scanRecTop, previewMarginLeftOrRightPx, layoutParams.bottomMargin);
            previewIV.setLayoutParams(layoutParams);
            hasScanViewInitialized = true;
            Log.e(TAG, "initScanView");
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

                    int newBitmapX = scanRecTop;
                    int newBitmapY = scanRecLeft;
                    int newBitmapWidth = scanRecBottom - scanRecTop;
                    int newBitmapHeight = scanRecRight - scanRecLeft;

                    // 截取预览框中的bitmap
                    final Bitmap newbitmap = Bitmap.createBitmap(bitmap, newBitmapX, newBitmapY, newBitmapWidth, newBitmapHeight, matrix, false);
                    //final Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

                    //显示截取的预览框中的bitmap，调试使用
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isPreviewIVVisiable) {
                                if (newbitmap != null && !newbitmap.isRecycled()) {
                                    previewIV.setImageBitmap(newbitmap);
                                }
                            }
                        }
                    });

                    //解析二维码图片
                    String result = QRUtil.decodeQRCodeByBitmap(newbitmap);

                    if (newbitmap != null && !newbitmap.isRecycled()) {
                        newbitmap.recycle();
                    }
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }

                    if (!TextUtils.isEmpty(result)) {
                        Intent intent = new Intent();
                        intent.putExtra("result", result);
                        setResult(RESULT_OK, intent);
                        CameraActivity.this.finish();
                    } else {
                        try {
                            if (!cameraManager.isReleaseCamera()) {
                                //接收下一次预览回调
                                cameraManager.setOneShotPreviewCallback(mPreview);
                            } else {
                                Log.e(TAG, "has release 2");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onOpeningCameraFailed() {
            Log.e(TAG, "onOpeningCameraFailed");
            showMissingPermissionDialog();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_SETTING_REQUEST_CODE) {

        }
    }

    private static final int START_SETTING_REQUEST_CODE = 1;

    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("无法开启摄像头，请检查是否已经打开摄像头权限");
        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "权限未开启，无法使用照相机", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, START_SETTING_REQUEST_CODE);
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}

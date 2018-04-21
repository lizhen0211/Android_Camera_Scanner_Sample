package com.lz.scanner.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by lz on 2018/4/19.
 */

public class CameraUtil {
    private static final String TAG = CameraUtil.class.getSimpleName();

    /**
     * 获取相机最合适的预览窗口大小
     *
     * @param sizes
     * @param maxSupportSize
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public static int[] getAppropriateSize(List<Camera.Size> sizes, Camera.Size maxSupportSize, int screenWidth, int screenHeight) {
        int previewWidth = maxSupportSize.width;
        int previewHeight = maxSupportSize.height;
        //个别手机的最大预览窗口大小比手机屏幕大小还要大，如小米。这是预览窗口会充满屏幕
        //如果是这样，找到一组与屏幕尺寸相同的预览窗口大小
        if (maxSupportSize.height > screenWidth) {
            for (int i = 0; i < sizes.size(); i++) {
                Camera.Size size = sizes.get(i);
                if (size.height == screenWidth && size.width == screenHeight) {
                    previewWidth = screenHeight;
                    previewHeight = screenWidth;
                    break;
                }
            }
        }
        return new int[]{previewWidth, previewHeight};
    }

    /**
     * 获取相机最大支持的预览窗口大小
     *
     * @param sizes
     * @return
     */
    public static Camera.Size getMaxSupportedSize(List<Camera.Size> sizes) {
        Camera.Size maxValue = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size value = sizes.get(i);
            if (value.width > maxValue.width) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    /**
     * 设置相机显示方向
     *
     * @param context
     * @return
     */
    public static int getDisplayOrientation(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        Log.e(TAG, rotation + "");
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        Camera.CameraInfo camInfo =
                new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }
}

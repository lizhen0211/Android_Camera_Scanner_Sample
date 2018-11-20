package com.lz.scanner.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lz on 2018/8/13.
 */
public class PermissionsUtil {

    /**
     * 请求权限
     *
     * @param activity
     * @param permissions
     * @param reqCode
     * @param iPermission
     */
    public static void requestPermission(Activity activity, String[] permissions, int reqCode, IPermission iPermission) {
        if (PermissionsUtil.isSupportRequestPermission()) {
            List<String> deniedPermissions = PermissionsUtil.findDeniedPermissions(activity, permissions);
            if (deniedPermissions.size() > 0) {
                //请求权限
                ActivityCompat.requestPermissions(activity,
                        deniedPermissions.toArray(
                                new String[deniedPermissions.size()]),
                        reqCode);
                //返回结果 onRequestPermissionsResult
            } else {
                if (iPermission != null) {
                    iPermission.onPermissionsGranted();
                }
            }
        } else {
            if (iPermission != null) {
                iPermission.onLessThanMarshmallow();
            }
        }
    }

    public static void requestPermissionsResult(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, IPermission iPermission) {
        if (!PermissionsUtil.verifyPermissions(grantResults)) {
            if (PermissionsUtil.shouldShowRequestPermissionRationale(permissions, activity)) {
                if (iPermission != null) {
                    iPermission.onPermissionsDeniedAfterReqNoLongerAsk(requestCode, permissions);
                }
            } else {
                if (iPermission != null) {
                    iPermission.onPermissionsDeniedAfterReq(requestCode, permissions);
                }
            }
        } else {
            if (iPermission != null) {
                iPermission.onPermissionsGrantedAfterReq(requestCode, permissions);
            }
        }
    }

    /**
     * @param permissions
     * @since 2.5.0
     */
    public static boolean checkPermissions(Activity activity, int reqCode, String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(activity, permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    reqCode);
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    private static boolean isSupportRequestPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 获取权限需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private static List<String> findDeniedPermissions(Activity activity, String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity,
                    perm) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    /**
     * 是否权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    //第一次打开App时	false；
    //上次弹出权限点击了禁止（但没有勾选“下次不在询问”）true；
    //上次选择禁止并勾选：下次不在询问	false
    private static boolean shouldShowRequestPermissionRationale(String[] deniedPermissions, Activity activity) {
        boolean flag = false;
        for (String permission : deniedPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

}

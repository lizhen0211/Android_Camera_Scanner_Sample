package com.lz.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lz.scanner.permission.IPermission;
import com.lz.scanner.permission.PermissionGroup;
import com.lz.scanner.permission.PermissionsUtil;

public class MainActivity extends Activity {

    private TextView resultTV;

    public static final int OPEN_CAMERA_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTV = (TextView) findViewById(R.id.result_tv);
    }

    public void onOpenCameraBtnClick(View view) {
        PermissionsUtil.requestPermission(this, PermissionGroup.CAMERA_PERMISSIONS, PermissionGroup.CAMERA_REQUEST_CODE, cameraPermissionCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.requestPermissionsResult(this, PermissionGroup.CAMERA_REQUEST_CODE, permissions, grantResults, cameraPermissionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionGroup.CAMERA_REQUEST_CODE) {
            //请求必要权限
            PermissionsUtil.requestPermission(MainActivity.this, PermissionGroup.CAMERA_PERMISSIONS, PermissionGroup.CAMERA_REQUEST_CODE, cameraPermissionCallback);
        }

        if (requestCode == OPEN_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            resultTV.setText(data.getStringExtra("result"));
        }
    }

    private IPermission cameraPermissionCallback = new IPermission() {
        @Override
        public void onPermissionsGranted() {
            Log.e(MainActivity.class.getSimpleName(), "onPermissionsGranted");
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent, OPEN_CAMERA_REQUEST_CODE);
        }

        @Override
        public void onLessThanMarshmallow() {
            Log.e(MainActivity.class.getSimpleName(), "onLessThanMarshmallow");
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent, OPEN_CAMERA_REQUEST_CODE);
        }

        @Override
        public void onPermissionsGrantedAfterReq(int requestCode, String[] perms) {
            Log.e(MainActivity.class.getSimpleName(), "onPermissionsGrantedAfterReq");

        }

        @Override
        public void onPermissionsDeniedAfterReq(int requestCode, String[] perms) {
            Log.e(MainActivity.class.getSimpleName(), "onPermissionsDeniedAfterReq");
        }

        @Override
        public void onPermissionsDeniedAfterReqNoLongerAsk(int requestCode, String[] perms) {
            Log.e(MainActivity.class.getSimpleName(), "onPermissionsDeniedAfterReqNoLongerAsk");
            showMissingPermissionDialog();
        }
    };

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
                    }
                });
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}

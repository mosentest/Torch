package com.my.light;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Permission;
import com.yanzhenjie.permission.runtime.setting.SettingPage;
import com.yanzhenjie.permission.source.ActivitySource;

import java.util.List;

/**
 * Copyright (C), 2018-2019
 * Author: ziqimo
 * Date: 2019-09-04 10:21
 * Description: 权限申请工具类
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
public class PermissionUtil {

    private final static int REQ_CAMERA = 9000;

    public static void getCamera(final Activity activity, final CallBack callBack) {
        AndPermission.with(activity)
                .runtime()
                .permission(Permission.Group.CAMERA)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        callBack.granted(data);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        if (AndPermission.hasAlwaysDeniedPermission(activity, data)) {
                            // 这些权限被用户总是拒绝。
                            SettingPage settingPage = new SettingPage(new ActivitySource(activity));
                            settingPage.start(REQ_CAMERA);
                        }
                    }
                })
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(Context context, List<String> data, final RequestExecutor executor) {
                        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(activity, setThemeDialog());
                        mAlertDialog.setTitle(activity.getString(R.string.hello));
                        mAlertDialog.setMessage(activity.getString(R.string.first));
                        mAlertDialog.setPositiveButton(activity.getString(R.string.okay),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        executor.execute();
                                        dialog.dismiss();
                                    }
                                });
                        mAlertDialog.show();
                    }
                })
                .start();
    }


    public interface CallBack {
        public void granted(List<String> data);
    }


    private static int setThemeDialog() {
        int mTheme;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTheme = R.style.MaterialDialog;
        } else {
            mTheme = R.style.HoloDialog;
        }
        return mTheme;
    }
}

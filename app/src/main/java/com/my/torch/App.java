package com.my.torch;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.util.List;


/**
 * Copyright (C), 2018-2019
 * Author: ziqimo
 * Date: 2019-09-04 15:55
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
public class App extends Application {

    private final static String TAG = "App";


    public static App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        String sourceDir = AppUtils.getSourceDir(this);
        if (!TextUtils.isEmpty(sourceDir)) {
            String md5 = AppUtils.readZipFile(sourceDir, "META-INF/CERT.RSA");
            if (!TextUtils.isEmpty(md5) && !AppInfo.getApkCertMd5().equals(md5)) {
                //暂时用不了
            }
        }
        boolean b = AppUtils.checkSignHash();
        if (b) {
            //才执行
        }
    }
}

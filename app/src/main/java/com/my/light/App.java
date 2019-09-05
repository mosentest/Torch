package com.my.light;

import android.app.Application;
import android.text.TextUtils;

import com.my.light.utils.AppInfo;
import com.my.light.utils.AppUtils;
import com.my.light.utils.FileSign;
import com.squareup.leakcanary.LeakCanary;


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
        FileSign.isApp();
        super.onCreate();
        mApp = this;
        if (LeakCanary.isInAnalyzerProcess(mApp)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(mApp);
        String sourceDir = AppUtils.getSourceDir(mApp);
        if (!TextUtils.isEmpty(sourceDir)) {
            String md5 = AppUtils.readZipFile(sourceDir, "META-INF/CERT.RSA");
            if (!TextUtils.isEmpty(md5) && !AppInfo.getApkCertMd5().equals(md5)) {
                //暂时用不了
                FileSign.isApp();
            }
        }
        boolean b = AppUtils.checkSignHash();
        if (b) {
            //才执行
            FileSign.isApp();
        }
    }

}

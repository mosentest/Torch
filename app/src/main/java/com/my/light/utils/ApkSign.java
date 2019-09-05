package com.my.light.utils;

import android.text.TextUtils;

import com.my.light.App;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Copyright (C), 2018-2019
 * Author: ziqimo
 * Date: 2019-09-05 09:48
 * Description:
 * History: 通过获取apk的签名
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
public class ApkSign {

    /**
     * 校验这个
     *
     * @return
     */
    public static boolean checkManifestValue() {
        String sourceDir = AppUtils.getSourceDir(App.mApp);
        String signaturesFromApk = getSignaturesFromApk(new File(sourceDir), "AndroidManifest.xml");
        if (TextUtils.isEmpty(signaturesFromApk)) {
            return true;
        }
        return AppInfo.getManifestValue().equals(signaturesFromApk);
    }

    /**
     * https://m.jb51.net/article/79894.htm
     * 从APK中读取签名
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getSignaturesFromApk(File file, String jarName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            JarFile jarFile = new JarFile(file);
            JarEntry je = jarFile.getJarEntry(jarName);
            byte[] readBuffer = new byte[8192];
            Certificate[] certs = loadCertificates(jarFile, je, readBuffer);
            if (certs != null) {
                for (Certificate c : certs) {
                    String sig = toCharsString(c.getEncoded());
                    stringBuilder.append(sig);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 加载签名
     *
     * @param jarFile
     * @param je
     * @param readBuffer
     * @return
     */
    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        try {
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将签名转成转成可见字符串
     *
     * @param sigBytes
     * @return
     */
    private static String toCharsString(byte[] sigBytes) {
        byte[] sig = sigBytes;
        final int N = sig.length;
        final int N2 = N * 2;
        char[] text = new char[N2];
        for (int j = 0; j < N; j++) {
            byte v = sig[j];
            int d = (v >> 4) & 0xf;
            text[j * 2] = (char) (d >= 10 ? ('a' + d - 10) : ('0' + d));
            d = v & 0xf;
            text[j * 2 + 1] = (char) (d >= 10 ? ('a' + d - 10) : ('0' + d));
        }
        return new String(text);
    }
}

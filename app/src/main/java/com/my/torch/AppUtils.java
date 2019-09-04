package com.my.torch;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Copyright (C), 2018-2019
 * Author: ziqimo
 * Date: 2019-09-04 16:11
 * Description:    签名校验的类
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
public class AppUtils {

    private final static String pkgName = "com.my.torch";

    /**
     * 校验md5
     *
     * @return
     */
    public static boolean checkSignMd5() {
        String signMd5Str = getSignMd5Str(App.mApp);
        if (TextUtils.isEmpty(signMd5Str)) {
            return true;
        }
        return AppInfo.getMd5().equals(signMd5Str);
    }


    /**
     * 获取app签名md5值
     */
//    public static String getSignMd5Str() {
//        try {
//            PackageInfo packageInfo = App.mApp.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
//            Signature[] signs = packageInfo.signatures;
//            Signature sign = signs[0];
//            String signStr = encryptionMD5(sign.toByteArray());
//            return signStr;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
    public static String getSignMd5Str(Context context) {
        Method getPackageManagerMethod = null;
        try {
            getPackageManagerMethod = context.getClass().getMethod("getPackageManager");
            getPackageManagerMethod.setAccessible(true);
            Object getPackageManager = getPackageManagerMethod.invoke(context);
            Method getPackageInfoMethod = getPackageManager.getClass().getDeclaredMethod("getPackageInfo", String.class, int.class);
            getPackageInfoMethod.setAccessible(true);
            Object packageInfo = getPackageInfoMethod.invoke(getPackageManager, pkgName, PackageManager.GET_SIGNATURES);
            Field signatures = packageInfo.getClass().getDeclaredField("signatures");
            signatures.setAccessible(true);
            Signature[] signs = (Signature[]) signatures.get(packageInfo);
            Signature sign = signs[0];
            String signStr = encryptionMD5(sign.toByteArray());
            return signStr;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * MD5加密
     *
     * @param byteStr 需要加密的内容
     * @return 返回 byteStr的md5值
     */
    public static String encryptionMD5(byte[] byteStr) {
        MessageDigest messageDigest = null;
        StringBuffer md5StrBuff = new StringBuffer();
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(byteStr);
            byte[] byteArray = messageDigest.digest();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5StrBuff.toString();
    }


    /**
     * 校验hash
     *
     * @return
     */
    public static boolean checkSignHash() {
        int signature = getSignature();
        if (signature == 0) {
            return true;
        }
        return signature == AppInfo.getHash();
    }


    /**
     * hash值
     *
     * @return
     */
    public static int getSignature() {
        PackageManager pm = App.mApp.getPackageManager();
        PackageInfo pi;
        StringBuilder sb = new StringBuilder();

        try {
            pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            Signature[] signatures = pi.signatures;
            for (Signature signature : signatures) {
                sb.append(signature.toCharsString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return sb.toString().hashCode();
    }


    /**
     * 获取apk的签名
     */
//    public static String getSourceDir() {
//        try {
//            ApplicationInfo applicationInfo = App.mApp.getPackageManager().getApplicationInfo(pkgName, 0);
//            String sourceDir = applicationInfo.sourceDir;
//            return sourceDir;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    /**
     * 获取apk的签名
     */
    public static String getSourceDir(Context context) {
        Method getPackageManagerMethod = null;
        try {
            getPackageManagerMethod = context.getClass().getMethod("getPackageManager");
            getPackageManagerMethod.setAccessible(true);
            Object getPackageManager = getPackageManagerMethod.invoke(context);
            Method getApplicationInfoMethod = getPackageManager.getClass().getDeclaredMethod("getApplicationInfo", String.class, int.class);
            Object getApplicationInfo = getApplicationInfoMethod.invoke(getPackageManager, pkgName, 0);
            Field sourceDirField = getApplicationInfo.getClass().getDeclaredField("sourceDir");
            String sourceDir = (String) sourceDirField.get(getApplicationInfo);
            return sourceDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * https://blog.csdn.net/LOVE000520/article/details/52993782
     * 无需解压直接读取Zip文件和文件内容
     *
     * @param orginFile
     * @throws Exception
     */
    public static String readZipFile(String orginFile, String fileName) {
        byte[] bytes = null;
        ZipFile zf = null;
        InputStream in = null;
        ZipInputStream zin = null;
        try {
            zf = new ZipFile(orginFile);
            in = new BufferedInputStream(new FileInputStream(orginFile));
            zin = new ZipInputStream(in);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    //Do nothing
                } else {
                    //Log.e("aaa", "orginFile - " + ze.getName() + " : " + ze.getSize() + " bytes");
                    if (fileName.equals(ze.getName())) {
                        BufferedReader br = null;
                        InputStream inputStream = null;
                        try {
                            //Log.e("aaa", "------");
                            inputStream = zf.getInputStream(ze);
                            bytes = toByteArray(inputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (br != null) {
                                br.close();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zin != null) {
                try {
                    zin.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bytes != null && bytes.length > 0) {
            try {
//                String s = Base64.encodeToString(bytes, 0);
//                String encryptionMD5 = encryptionMD5(bytes);
//                Log.e("aaa", encryptionMD5);
//                Log.e("aaa", "aa" + s);
                return "";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }


    /**
     * 校验这个
     *
     * @return
     */
    public static boolean checkManifestValue() {
        String sourceDir = AppUtils.getSourceDir(App.mApp);
        String signaturesFromApk = AppUtils.getSignaturesFromApk(new File(sourceDir), "AndroidManifest.xml");
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

package com.my.torch.utils;

import android.text.TextUtils;

import com.my.torch.App;
import com.my.torch.xmlpull.content.res.AXmlResourceParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Copyright (C), 2018-2019
 * Author: ziqimo
 * Date: 2019-09-05 09:50
 * Description:
 * History: 读取application的名字是否是自己的名字
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
public class AnalysisApk {

    private static String enterActivityName = "";
    private static String actionName = "";
    private static String categoryName = "";
    private static String pkgName = "";

    private final static String CATE_MAIN = "android.intent.action.MAIN";
    private final static String CATE_LAUNCHER = "android.intent.category.LAUNCHER";
    private static boolean isLauncher = false;


    public static boolean checkApplication() {
        String name = App.class.getName();
        String sourceDir = AppUtils.getSourceDir(App.mApp);
        String appEnterApplication = getAppEnterApplication(sourceDir);
        if (TextUtils.isEmpty(appEnterApplication)) {
            return true;
        }
        return name.equals(appEnterApplication);
    }

    public static String getAppEnterApplication(String apkUrl) {
        isLauncher = false;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(new File(apkUrl));
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                if (!zipEntry.isDirectory() && "AndroidManifest.xml".equals(zipEntry.getName())) {
                    try {
                        AXmlResourceParser parser = new AXmlResourceParser();
                        parser.open(zipFile.getInputStream(zipEntry));
                        while (true) {
                            int type = parser.next();
                            if (type == XmlPullParser.END_DOCUMENT) {
                                break;
                            }
                            switch (type) {
                                case XmlPullParser.START_TAG: {
                                    String tagName = parser.getName();
                                    if ("manifest".equals(tagName)) {
                                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                                            String attrName = parser.getAttributeName(i);
                                            if ("package".equals(attrName)) {
                                                pkgName = parser.getAttributeValue(i);
                                            }
                                        }
                                    } else if ("application".equals(tagName)) {
                                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                                            String attrName = parser.getAttributeName(i);
                                            if ("name".equals(attrName)) {
                                                String appName = parser.getAttributeValue(i);
                                                if (appName.startsWith(".")) {
                                                    return pkgName + appName;
                                                }
                                                return appName;
                                            }
                                        }
                                    } else if ("activity".equals(tagName)) {
                                        isLauncher = false;
                                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                                            String attrName = parser.getAttributeName(i);
                                            if ("name".equals(attrName)) {
                                                enterActivityName = parser.getAttributeValue(i);
                                                break;
                                            }
                                        }
                                    } else if ("action".equals(tagName)) {
                                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                                            String attrName = parser.getAttributeName(i);
                                            if ("name".equals(attrName)) {
                                                actionName = parser.getAttributeValue(i);
                                                break;
                                            }
                                        }
                                    } else if ("category".equals(tagName)) {
                                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                                            String attrName = parser.getAttributeName(i);
                                            if ("name".equals(attrName)) {
                                                categoryName = parser.getAttributeValue(i);
                                                if (CATE_LAUNCHER.equals(categoryName)) {
                                                    isLauncher = true;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;

                                case XmlPullParser.END_TAG: {
                                    String tagName = parser.getName();
                                    if ("intent-filter".equals(tagName)) {
                                        if (CATE_MAIN.equals(actionName) && isLauncher) {
                                            if (enterActivityName.startsWith(".")) {
                                                return pkgName + enterActivityName;
                                            }
                                            return enterActivityName;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private final static String PACKAGE_NAME = "com.my.torch";

    public static boolean checkApp() {
        String name = App.class.getName();
        String sourceDir = AppUtils.getSourceDir(App.mApp);
        String appEnterApplication = getApplication(sourceDir);
        if (TextUtils.isEmpty(appEnterApplication)) {
            return true;
        }
        return name.equals(appEnterApplication);
    }

    public static String getApplication(String apkUrl) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(new File(apkUrl));
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                if (!zipEntry.isDirectory() && "AndroidManifest.xml".equals(zipEntry.getName())) {
                    InputStream inputStream = null;
                    try {
                        inputStream = zipFile.getInputStream(zipEntry);
                        //1首先利用DocumentBuilderFactory 创建一个DocumentBuilderFactory实例
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        //2利用DocumentBuilderFactory 创建一个DocumentBuilder实例
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        //3加载整个文档(Document)
                        Document document = builder.parse(inputStream);
                        //4获取文档的根节点(Element)
                        Element element = document.getDocumentElement();
                        //5获取根节点下所有标签为person的子节点
                        NodeList items = element.getElementsByTagName("application");
                        Element application = (Element) items.item(0);
                        String appName = application.getAttribute("name");
                        if (appName.startsWith(".")) {
                            return PACKAGE_NAME + appName;
                        }
                        return appName;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "com.my.torch.App";
    }
}

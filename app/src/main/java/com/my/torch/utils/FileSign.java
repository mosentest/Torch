package com.my.torch.utils;

import android.util.Log;

import com.my.torch.App;

import java.lang.reflect.Method;

/**
 * Copyright (C), 2018-2019
 * Author: ziqimo
 * Date: 2019-09-05 11:11
 * Description: 校验appliaction的完整性
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
public class FileSign {

    public static boolean isApp() {
        Method[] methods = App.class.getDeclaredMethods();
        return methods == null || methods.length == 1;
    }
}

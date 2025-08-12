package com.baidu.idl.face.main.utils;

import java.util.Calendar;

public class TimeUtils {

    // 判断当前时间是否在6:00-23:59之间
    public static boolean isInOperatingTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 获取24小时制的小时数
        // 6点到23点59分之间返回true
        return hour >= 6 && hour <= 12;
    }
}
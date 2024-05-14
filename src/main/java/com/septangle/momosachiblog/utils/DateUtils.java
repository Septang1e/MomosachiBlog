package com.septangle.momosachiblog.utils;

import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    @Value("${constant.blog.date.format.basic}")
    private static String basicFormat;

    public static String dateToString(Date date){
        SimpleDateFormat format = new SimpleDateFormat(basicFormat);
        return format.format(date);
    }
}
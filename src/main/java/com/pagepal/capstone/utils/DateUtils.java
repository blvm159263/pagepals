package com.pagepal.capstone.utils;

import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Component
public class DateUtils {
    private static final TimeZone VIETNAM_TIME_ZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    public Date getCurrentVietnamDate() {
        Calendar calendar = Calendar.getInstance(VIETNAM_TIME_ZONE);
        return calendar.getTime();
    }
}
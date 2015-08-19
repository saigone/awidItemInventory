package com.emmt.Utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Owner on 2015/3/20.
 */
public class ToolUtil {

    public static String getCurrentTime(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        String time = dateFormat.format(cal.getTime());

        return time;
    }
}

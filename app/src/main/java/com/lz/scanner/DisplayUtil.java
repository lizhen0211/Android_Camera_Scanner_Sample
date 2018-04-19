package com.lz.scanner;

import android.content.Context;

/**
 * Created by lz on 2018/4/19.
 */

public class DisplayUtil {

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density; // 设备的密度
        return (int) (dipValue * scale + 0.5f);
    }
}

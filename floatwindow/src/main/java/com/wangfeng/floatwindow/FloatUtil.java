package com.wangfeng.floatwindow;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;


/**
 */

public class FloatUtil {


    static View inflate(Context applicationContext, int layoutId) {
        LayoutInflater inflate = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflate.inflate(layoutId, null);
    }

    private static Point sPoint;
    private static int heightPixels;
    private static int widthPixels;

    /**
     * 获取屏幕 并非全部
     */
    public static int getScreenWidthSub(Context context) {
        sPoint = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(sPoint);
        return sPoint.x;
    }

    public static int getScreenHeightSub(Context context) {
        sPoint = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(sPoint);
        return sPoint.y;
    }

    public static int getScreenWidth(Context context) {
//        if (sPoint == null) {
//            sPoint = new Point();
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            wm.getDefaultDisplay().getSize(sPoint);
//        }
//        if (widthPixels < 1){
//
//        }
                    sPoint = new Point();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealSize(sPoint);
//        widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        return sPoint.x;
    }


    public static int getScreenHeight(Context context) {
//        if (sPoint == null) {
//            sPoint = new Point();
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            wm.getDefaultDisplay().getSize(sPoint);
//        }
//        if (heightPixels < 1){
//
//        }
//        heightPixels = context.getResources().getDisplayMetrics().heightPixels;
                    sPoint = new Point();
            WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealSize(sPoint);
        return sPoint.y;
    }

    static boolean isViewVisible(View view) {
        return view.getGlobalVisibleRect(new Rect());
    }
}

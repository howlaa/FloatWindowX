package com.wangfeng.floatwindow;

import android.view.View;

/**
 */

public abstract class IFloatWindow {
    public abstract int getWidth();
    public abstract void show();
    public abstract void hide();

    public abstract boolean isShowing();
    public abstract boolean isHiding();

    public abstract int getX();

    public abstract int getY();

    public abstract void updateX(int x);
    public abstract void updateOrientation(int orientation);
    public abstract void updateWH(int width,int height);
    /**
     * 设置是否可点击
     */
    public abstract void dragEnable(boolean enable);

    public abstract void updateX(@Screen.screenType int screenType,float ratio);

    public abstract void updateY(int y);

    public abstract void updateY(@Screen.screenType int screenType,float ratio);

    public abstract View getView();

    abstract void dismiss();
}

package com.wangfeng.floatwindow;

/**
 */
public interface ViewStateListener {
    void onPositionUpdate(int x, int y);

    void onShow();

    void onHide();

    void onDismiss();

    void onMoveAnimStart();

    void onMoveAnimEnd();

    void onBackToDesktop();

    //滑动删除
    void onDragRemoved();
}

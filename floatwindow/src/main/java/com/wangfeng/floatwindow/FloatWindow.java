package com.wangfeng.floatwindow;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 悬浮窗
 * @author 王丰
 */

public class FloatWindow {

    private FloatWindow() {

    }

    private static final String mDefaultTag = "default_float_window_tag";
    private static Map<String, IFloatWindow> mFloatWindowMap;

    public static IFloatWindow get() {
        return get(mDefaultTag);
    }

    public static IFloatWindow get(@NonNull String tag) {
        return mFloatWindowMap == null ? null : mFloatWindowMap.get(tag);
    }

    public static Boolean isShowing(@NonNull String tag){
        if (get(tag) == null) {
            return false;
        } else {
            return Objects.requireNonNull(get(tag)).isShowing();
        }
    }

    private static B mBuilder = null;

    @MainThread
    public static B with(@NonNull Context applicationContext) {
        return mBuilder = new B(applicationContext);
    }


    public static void destroy() {
        destroy(mDefaultTag);
    }

    public static void destroy(String tag) {
        if (mFloatWindowMap == null || !mFloatWindowMap.containsKey(tag)) {
            return;
        }
        mFloatWindowMap.get(tag).dismiss();
        mFloatWindowMap.remove(tag);
        if (mFloatWindowMap.containsKey( tag + "_REMOVE")) {
            mFloatWindowMap.get(tag).dismiss();
            mFloatWindowMap.remove(tag);
        }
    }

    public static class B {
        Context mApplicationContext;
        View mView;
        private int mLayoutId;
        int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.TOP | Gravity.START;
        int xOffset;
        int yOffset;
        boolean mShow = true;
        Class[] mActivities;
        int mMoveType = MoveType.slide;
        boolean mDragEnable = true;
        boolean mIsMoving = false;
        int mSlideLeftMargin;
        int mSlideRightMargin;
        long mDuration = 300;
        TimeInterpolator mInterpolator;
         String mTag = mDefaultTag;
        boolean mDesktopShow;
        long mHideTime = -1L;
        int mRemoveResId = -1;
        int mRemoveReplaceResId = -1;
        String mRemoveTag = "";
        PermissionListener mPermissionListener;
        ViewStateListener mViewStateListener;
        ViewTouchListener mViewTouchListener;

        private B() {

        }

        B(Context applicationContext) {
            mApplicationContext = applicationContext;
        }

        public B setView(@NonNull View view) {
            mView = view;
            return this;
        }

        public B setView(@LayoutRes int layoutId) {
            mLayoutId = layoutId;
            return this;
        }

        public B setWidth(int width) {
            mWidth = width;
            return this;
        }

        public B setHeight(int height) {
            mHeight = height;
            return this;
        }

        public B setWidth(@Screen.screenType int screenType, float ratio) {
            mWidth = (int) ((screenType == Screen.width ?
                    FloatUtil.getScreenWidth(mApplicationContext) :
                    FloatUtil.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }

        public B setWidthMachParent(boolean isMach) {
            if (isMach){
                mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            return this;
        }

        public B setHeightMachParent(boolean isMach) {
            if (isMach){
                mHeight = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            return this;
        }



        public B setHeight(@Screen.screenType int screenType, float ratio) {
            mHeight = (int) ((screenType == Screen.width ?
                    FloatUtil.getScreenWidth(mApplicationContext) :
                    FloatUtil.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }


        public B setX(int x) {
            xOffset = x;
            return this;
        }

        public B setY(int y) {
            yOffset = y;
            return this;
        }


        public B setX(@Screen.screenType int screenType, float ratio) {
            xOffset = (int) ((screenType == Screen.width ?
                    FloatUtil.getScreenWidth(mApplicationContext) :
                    FloatUtil.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }

        public B setY(@Screen.screenType int screenType, float ratio) {
            yOffset = (int) ((screenType == Screen.width ?
                    FloatUtil.getScreenWidth(mApplicationContext) :
                    FloatUtil.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }


        /**
         * 设置 Activity 过滤器，用于指定在哪些界面显示悬浮窗，默认全部界面都显示
         *
         * @param show       　过滤类型,子类类型也会生效
         * @param activities 　过滤界面
         */
        public B setFilter(boolean show, @NonNull Class... activities) {
            mShow = show;
            mActivities = activities;
            return this;
        }

        public B setMoveType(@MoveType.MOVE_TYPE int moveType) {
            return setMoveType(moveType, 0, 0);
        }


        /**
         * 设置带边距的贴边动画，只有 moveType 为 MoveType.slide，设置边距才有意义，这个方法不标准，后面调整
         *
         * @param moveType         贴边动画 MoveType.slide
         * @param slideLeftMargin  贴边动画左边距，默认为 0
         * @param slideRightMargin 贴边动画右边距，默认为 0
         */
        public B setMoveType(@MoveType.MOVE_TYPE int moveType, int slideLeftMargin, int slideRightMargin) {
            mMoveType = moveType;
            mSlideLeftMargin = slideLeftMargin;
            mSlideRightMargin = slideRightMargin;
            return this;
        }

        public B setMoveStyle(long duration, @Nullable TimeInterpolator interpolator) {
            mDuration = duration;
            mInterpolator = interpolator;
            return this;
        }

        public B setTag(@NonNull String tag) {
            mTag = tag;
            return this;
        }

        public B setDesktopShow(boolean show) {
            mDesktopShow = show;
            return this;
        }

        /**
         * 自动隐藏
         * @param hideTime 多久隐藏
         */
        public B setAutoHide(long hideTime) {
            mHideTime = hideTime;
            return this;
        }

        /**
         * 拖拽取消
         */
        public B setDragResId(int resId){
            mRemoveResId = resId;
            mRemoveTag = mTag + "_REMOVE";
            return this;
        }

        /**
         * 替换掉的View
         */
        public B setDragRemoveReplaceResId(int resId){
            mRemoveReplaceResId = resId;
            return this;
        }

        public B setPermissionListener(PermissionListener listener) {
            mPermissionListener = listener;
            return this;
        }

        public B setViewStateListener(ViewStateListener listener) {
            mViewStateListener = listener;
            return this;
        }
        public B setViewTouchListener(ViewTouchListener listener){
            mViewTouchListener = listener;
            return this;
        }

        public void build() {
            if (mFloatWindowMap == null) {
                mFloatWindowMap = new HashMap<>();
            }
            if (mFloatWindowMap.containsKey(mTag)) {
                throw new IllegalArgumentException("FloatWindow of this tag has been added, Please set a new tag for the new FloatWindow");
            }
            if (mView == null && mLayoutId == 0) {
                throw new IllegalArgumentException("View has not been set!");
            }
            if (mView == null) {
                mView = FloatUtil.inflate(mApplicationContext, mLayoutId);
            }
            IFloatWindow floatWindowImpl = new IFloatWindowImpl(this);
            mFloatWindowMap.put(mTag, floatWindowImpl);
        }

    }
}

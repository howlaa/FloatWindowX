package com.wangfeng.floatwindow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.coocent.fixedfloatwindow.R;


/**
 */

public class IFloatWindowImpl extends IFloatWindow {


    private FloatWindow.B mB;
    private FloatView mFloatView;
    private FloatLifecycle mFloatLifecycle;
    private boolean isShow;
    private boolean isHide;

    private boolean once = true;
    private ValueAnimator mAnimator;
    private ValueAnimator mAnimatorRemove;
    private ValueAnimator mAnimatorRemoveHide;
    private AnimatorSet mAnimatorSetHide;
    private TimeInterpolator mDecelerateInterpolator;
    private float downX;
    private float downY;
    private float upX;
    private float upY;
    private boolean mClick = false;
    private int mSlop;
    private float mXPosition = 0.1f;
    private float mYPosition = 0.1f;
    private int screenWidth;
    private int screenHeight;
    private boolean isTouching = false;
    private int mWhichSide = FloatConstant.LEFT;
    private boolean isRemoveRect = false;
    //移除View
    View mRemoveRootView;

    private IFloatWindowImpl() {

    }

    IFloatWindowImpl(FloatWindow.B b) {
        mB = b;
        if (mB.mMoveType == MoveType.fixed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mFloatView = new FloatPhone(b.mApplicationContext, mB.mPermissionListener);
            } else {
                mFloatView = new FloatToast(b.mApplicationContext);
            }
        } else {
            mFloatView = new FloatPhone(b.mApplicationContext, mB.mPermissionListener);
            initTouchEvent();
        }
        screenWidth = FloatUtil.getScreenWidth(mB.mApplicationContext);
        screenHeight = FloatUtil.getScreenHeight(mB.mApplicationContext);
        mFloatView.setSize(mB.mWidth, mB.mHeight);
        mFloatView.setGravity(mB.gravity, mB.xOffset, mB.yOffset);
        mFloatView.setView(mB.mView);
        if (screenWidth != 0 && screenHeight !=0){
            mXPosition = ((float) mB.xOffset) / screenWidth ;
            mYPosition = ((float) mB.yOffset) / screenHeight;
        }
        mFloatLifecycle = new FloatLifecycle(mB.mApplicationContext, mB.mShow, mB.mActivities, new LifecycleListener() {
            @Override
            public void onShow() {
                show();
            }

            @Override
            public void onHide() {
                hide();
            }

            @Override
            public void onBackToDesktop() {
                if (!mB.mDesktopShow) {
                    hide();
                }
                if (mB.mViewStateListener != null) {
                    mB.mViewStateListener.onBackToDesktop();
                }
            }
        });
    }





    @Override
    public void show() {
        isHide = false;
        if (once) {
            mFloatView.init();
            once = false;
            isShow = true;
        } else {
            if (isShow) {
                return;
            }
            getView().setVisibility(View.VISIBLE);
            isShow = true;
        }
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onShow();
        }

        int startX = mFloatView.getX();
        if (startX * 2 + mFloatView.getWidth() > FloatUtil.getScreenWidth(mB.mApplicationContext)){
            mWhichSide = FloatConstant.RIGHT;
        } else {
            mWhichSide = FloatConstant.LEFT;
        }

        autoHide();
    }

    @Override
    public void hide() {
        if (once || !isShow) {
            return;
        }
        getView().setVisibility(View.INVISIBLE);
        isHide = true;
        isShow = false;
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onHide();
        }
    }
    @Override
    public boolean isHiding(){
        return isHide;
    }

    @Override
    public boolean isShowing() {
        return isShow;
    }

    @Override
    void dismiss() {
        mFloatView.dismiss();
        isShow = false;
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onDismiss();
        }
    }

    @Override
    public void updateX(int x) {
        checkMoveType();
        mB.xOffset = x;
        mFloatView.updateX(x);
    }

    @Override
    public void updateOrientation(int orientation){
        screenHeight = FloatUtil.getScreenHeight(mB.mApplicationContext);
        screenWidth = FloatUtil.getScreenWidth(mB.mApplicationContext);
        int nowX = (int) (screenWidth * mXPosition);
        int nowY = (int) (screenHeight * mYPosition);
        if (nowX <0) {
            nowX = 0;
        } else if (nowX+mB.mWidth > screenWidth) {
            nowX = screenWidth - mB.mWidth;
        }
        if (nowY < 0){
            nowY = 0;
        } else if (nowY + mB.mHeight > screenHeight) {
            nowY = screenHeight - mB.mHeight;
        }
        Log.d("wangfeng","百分比   "+mXPosition+":"+mYPosition);
        if (mB.mMoveType == MoveType.slide) {
            if (mWhichSide == FloatConstant.RIGHT){
                nowX = screenWidth;
            } else {
                nowX = 0;
            }

        }
        mFloatView.updateXY(nowX,nowY);
    }

    @Override
    public int getWidth() {
        return mFloatView.getWidth();
    }


    @Override
    public void updateWH(int width,int height){
        checkMoveType();
        mB.mWidth = width;
        mB.mHeight = height;
        mFloatView.updateWH(width, height);
    }

    @Override
    public void dragEnable(boolean enable){
        mB.mDragEnable = enable;
    }

    @Override
    public void updateY(int y) {
        checkMoveType();
        mB.yOffset = y;
        mFloatView.updateY(y);
    }

    @Override
    public void updateX(int screenType, float ratio) {
        checkMoveType();
        mB.xOffset = (int) ((screenType == Screen.width ?
                FloatUtil.getScreenWidth(mB.mApplicationContext) :
                FloatUtil.getScreenHeight(mB.mApplicationContext)) * ratio);
        mFloatView.updateX(mB.xOffset);

    }

    @Override
    public void updateY(int screenType, float ratio) {
        checkMoveType();
        mB.yOffset = (int) ((screenType == Screen.width ?
                FloatUtil.getScreenWidth(mB.mApplicationContext) :
                FloatUtil.getScreenHeight(mB.mApplicationContext)) * ratio);
        mFloatView.updateY(mB.yOffset);

    }

    @Override
    public int getX() {
        return mFloatView.getX();
    }

    @Override
    public int getY() {
        return mFloatView.getY();
    }


    @Override
    public View getView() {
        mSlop = ViewConfiguration.get(mB.mApplicationContext).getScaledTouchSlop();
        return mB.mView;
    }


    private void checkMoveType() {
        if (mB.mMoveType == MoveType.fixed) {
            throw new IllegalArgumentException("FloatWindow of this tag is not allowed to move!");
        }
    }

    /**
     * 隐藏恢复
     */
    private void hideResume(){
        if (getView() != null){
            View it = getView();
            it.setTranslationX(0f);
            it.setAlpha(1.0f);
            it.setScaleX(1.0f);
            it.setScaleY(1.0f);
        }
    }

    /**
     * 判断是否进入范围
     */
    private void checkRemoveRect(){
        if (mB.mRemoveResId == -1 || !FloatWindow.isShowing(mB.mRemoveTag) ) return;
        if (Math.abs(FloatWindow.get(mB.mRemoveTag).getX() - getX()) < 100 && Math.abs(FloatWindow.get(mB.mRemoveTag).getY() - getY()) < 100) {
            if (!isRemoveRect) {
                Log.d("wangfeng","进入范围");
                Vibrator vib = (Vibrator) mB.mApplicationContext.getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(50);
                getView().setAlpha(0.0f);
                if (mB.mRemoveReplaceResId != -1) {
                    ImageView replaceIv = mRemoveRootView.findViewById(R.id.view_replace);
                    replaceIv.setImageResource(mB.mRemoveReplaceResId);
                    replaceIv.setVisibility(View.VISIBLE);
                }
            }
            isRemoveRect = true;
        } else {
            if (mB.mRemoveReplaceResId != -1 && mB.mRemoveResId != -1 && isRemoveRect) {
                Log.d("wangfeng","走出范围");
                getView().setAlpha(1.0f);
                ImageView replaceIv = mRemoveRootView.findViewById(R.id.view_replace);
                replaceIv.setImageResource(mB.mRemoveReplaceResId);
                replaceIv.setVisibility(View.GONE);
            }
            isRemoveRect = false;
        }
    }



    /**
     * 打开拖拽删除
     */
    private void dragRemove(){
        if (mB.mRemoveResId == -1 || FloatWindow.isShowing(mB.mRemoveTag)) return;
        try{
            if (FloatWindow.get(mB.mRemoveTag) == null) {
                 mRemoveRootView = View.inflate(mB.mApplicationContext, R.layout.layout_remove_ball, null);
                 ImageView contentIV = mRemoveRootView.findViewById(R.id.iv_content);
                contentIV.setImageResource(mB.mRemoveResId);
                FloatWindow.with(mB.mApplicationContext)
                        .setView(mRemoveRootView)
                        .setTag(mB.mRemoveTag)
                        .setX(Screen.width, 0.5f)
                        .setY(Screen.height, 1.0f)
                        .setDesktopShow(true)
                        .setMoveType(MoveType.inactive)
                        .build();
            }
            if (FloatWindow.get(mB.mRemoveTag) != null) {
                FloatWindow.get(mB.mRemoveTag).show();
                if (FloatWindow.get(mB.mRemoveTag).getView() != null) {
                    FloatWindow.get(mB.mRemoveTag).getView().post(new Runnable() {
                        @Override
                        public void run() {
                            if (FloatWindow.get(mB.mRemoveTag) != null) {
                                FloatWindow.get(mB.mRemoveTag).updateX(screenWidth/2 - FloatWindow.get(mB.mRemoveTag).getView().getWidth()/2);
                                startRemoveAnimator();
                            }
                        }
                    });
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            Log.d("wangfeng","dragRemove发生错误:"+e.getMessage());
        }
    }

    /**
     * 拖拽删除的动画
     */
    private void startRemoveAnimator(){
        mAnimatorRemove = new ValueAnimator();
        mAnimatorRemove.setIntValues(screenHeight, (int) (screenHeight * 0.55));
        mAnimatorRemove.addUpdateListener(animation -> {
            try{
                int y = (int) animation.getAnimatedValue();
                if (FloatWindow.get(mB.mRemoveTag) != null){
                    FloatWindow.get(mB.mRemoveTag).updateY(y);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        mAnimatorRemove.setDuration(100);
        mAnimatorRemove.start();
    }

    /**
     * 确定拖拽删除
     */
    private void sureDragRemove(){
        if (mAnimatorRemove != null) {
            mAnimatorRemove.removeAllUpdateListeners();
            mAnimatorRemove.removeAllListeners();
            mAnimatorRemove = null;
        }
        if (FloatWindow.isShowing(mB.mRemoveTag)) {
            FloatWindow.destroy(mB.mRemoveTag);
        }
        if (FloatWindow.isShowing(mB.mTag)){
            FloatWindow.destroy(mB.mTag);
        }
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onDragRemoved();
        }
    }

    /**
     * 取消拖拽删除
     */
    private void cancelDragRemove(){
        if (mAnimatorRemove != null) {
            mAnimatorRemove.removeAllUpdateListeners();
            mAnimatorRemove.removeAllListeners();
            mAnimatorRemove = null;
        }
        if (FloatWindow.get(mB.mRemoveTag) != null) {
            mAnimatorRemoveHide  = new ValueAnimator();
            mAnimatorRemoveHide.setIntValues((int) (screenHeight*0.6),screenHeight);
            mAnimatorRemoveHide.addUpdateListener(animation -> {
                try{
                    int y = (int) animation.getAnimatedValue();
                    if (FloatWindow.get(mB.mRemoveTag) != null){
                        FloatWindow.get(mB.mRemoveTag).updateY(y);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            });
            mAnimatorRemoveHide.setDuration(100);
            mAnimatorRemoveHide.start();
            mAnimatorRemoveHide.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (FloatWindow.get(mB.mRemoveTag) != null) {
                        if (!FloatWindow.get(mB.mRemoveTag).isShowing()) {
                            FloatWindow.get(mB.mRemoveTag).show();
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    try {
                        if (animation.isRunning()){
                            animation.cancel();
                        }
                        if (FloatWindow.get(mB.mRemoveTag) != null) {
                            FloatWindow.destroy(mB.mRemoveTag);
//                        FloatWindow.get(mB.mRemoveTag).updateY(screenHeight+100);
//                        FloatWindow.get(mB.mRemoveTag).hide();
                        }
                    } catch (Exception e){
                        Log.d("wangfeng","发生异常:"+e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnimatorRemoveHide.setDuration(100);
            mAnimatorRemoveHide.start();
        }
    }

    /**
     * 自动隐藏
     */
    private void autoHide(){
        if (mB.mHideTime < 0) return;
        if (getView() != null) {
            View it = getView();
            it.postDelayed(() -> {
                if (isTouching) {
                    return;
                }
                float translationX = mWhichSide == FloatConstant.LEFT ? -40.0f : 40.0f;
                mAnimatorSetHide = new AnimatorSet();
                ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(it,"alpha",0.5f);
                ObjectAnimator transXAnimation = ObjectAnimator.ofFloat(it,"translationX",translationX);
                ObjectAnimator objX = ObjectAnimator.ofFloat(it, "scaleX", 0.7f);
                ObjectAnimator objY = ObjectAnimator.ofFloat(it, "scaleY", 0.7f);
                mAnimatorSetHide.playTogether(objX, objY,alphaAnimation,transXAnimation);
                mAnimatorSetHide.setDuration(300L);
                mAnimatorSetHide.start();
            }, mB.mHideTime);
        }
    }



    private void initTouchEvent() {
        if (!mB.mDragEnable) {
            return;
        }
        switch (mB.mMoveType) {
            case MoveType.inactive:
                break;
            default:
                getView().setOnTouchListener(new View.OnTouchListener() {
                    float lastX, lastY, changeX, changeY;
                    int newX, newY;

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                isTouching = true;
                                downX = event.getRawX();
                                downY = event.getRawY();
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                cancelAnimator();

                                if (mB.mViewTouchListener != null) {
                                    mB.mViewTouchListener.touchDown();
                                }
                                hideResume();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                mB.mIsMoving = true;
                                changeX = event.getRawX() - lastX;
                                changeY = event.getRawY() - lastY;
//                                Log.d("wangfeng","移动距离:"+changeX+":"+changeY);
                                newX = (int) (mFloatView.getX() + changeX);
                                newY = (int) (mFloatView.getY() + changeY);
                                if (newX < 0 ) {
                                    newX = 0;
                                } else if (newX + mB.mView.getWidth() > screenWidth) {
                                    newX = screenWidth - mB.mView.getWidth();
                                }
                                if (newY < 0 ) {
                                    newY = 0;
                                } else if (newY + mB.mView.getHeight() > screenHeight) {
                                    newY = screenHeight - mB.mView.getHeight();
                                }
                                mFloatView.updateXY(newX, newY);
                                if (mB.mViewStateListener != null) {
                                    mB.mViewStateListener.onPositionUpdate(newX, newY);
                                }
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                if (screenWidth != 0 && screenHeight != 0){
                                    mXPosition = (float) newX/screenWidth;
                                    mYPosition = (float) newY/screenHeight;
                                }
                                if (Math.abs(event.getRawX() - downX) > 20 || Math.abs(event.getRawY() -downY) > 20) {
                                    dragRemove();
                                }
                                checkRemoveRect();
                                break;
                            case MotionEvent.ACTION_UP:
                                mB.mIsMoving = false;
                                if (isRemoveRect){
                                    sureDragRemove();
                                } else {
                                    cancelDragRemove();
                                }
                                isTouching = false;
                                upX = event.getRawX();
                                upY = event.getRawY();
                                mClick = (Math.abs(upX - downX) > mSlop) || (Math.abs(upY - downY) > mSlop);
                                switch (mB.mMoveType) {
                                    case MoveType.slide:
                                        int startX = mFloatView.getX();
                                        if (startX * 2 + v.getWidth() > FloatUtil.getScreenWidth(mB.mApplicationContext)){
                                            mWhichSide = FloatConstant.RIGHT;
                                        } else {
                                            mWhichSide = FloatConstant.LEFT;
                                        }
                                        int endX = (startX * 2 + v.getWidth() > FloatUtil.getScreenWidth(mB.mApplicationContext)) ?
                                                FloatUtil.getScreenWidth(mB.mApplicationContext) - v.getWidth() - mB.mSlideRightMargin :
                                                mB.mSlideLeftMargin;
                                        mAnimator = ObjectAnimator.ofInt(startX, endX);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (int) animation.getAnimatedValue();
                                                mFloatView.updateX(x);
                                                if (mB.mViewStateListener != null) {
                                                    mB.mViewStateListener.onPositionUpdate(x, (int) upY);
                                                }
                                            }
                                        });
                                        startAnimator();
                                        if (mB.mViewTouchListener != null){
                                            mB.mViewTouchListener.touchUp();
                                        }
                                        autoHide();
                                        break;
                                    case MoveType.back:
                                        PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", mFloatView.getX(), mB.xOffset);
                                        PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", mFloatView.getY(), mB.yOffset);
                                        mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (int) animation.getAnimatedValue("x");
                                                int y = (int) animation.getAnimatedValue("y");
                                                mFloatView.updateXY(x, y);
                                                if (mB.mViewStateListener != null) {
                                                    mB.mViewStateListener.onPositionUpdate(x, y);
                                                }
                                            }
                                        });
                                        startAnimator();
                                        mXPosition = ((float) newX) / screenWidth ;
                                        mYPosition = ((float) newY) / screenHeight;
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                        return mClick;
                    }
                });
        }
    }


    private void startAnimator() {
        if (mB.mInterpolator == null) {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = new DecelerateInterpolator();
            }
            mB.mInterpolator = mDecelerateInterpolator;
        }
        mAnimator.setInterpolator(mB.mInterpolator);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.removeAllUpdateListeners();
                mAnimator.removeAllListeners();
                mAnimator = null;
                if (mB.mViewStateListener != null) {
                    mB.mViewStateListener.onMoveAnimEnd();
                }
            }
        });
        mAnimator.setDuration(mB.mDuration).start();
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onMoveAnimStart();
        }
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        if (mAnimatorSetHide != null && mAnimatorSetHide.isRunning()) {
            mAnimatorSetHide.cancel();
        }
    }

}

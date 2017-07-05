package com.open.code.library.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ================================================
 * Created by zhaokai on 2017/4/27.
 * Email zhaokai1033@126.com
 * Describe :
 * 侧滑关闭的布局，使用方式
 * 在目标容器的onCreate里面创建本布局 {@link #SwipeCloseLayout(Context)}
 * 在目标容器的onPostCreate里面将本布局挂载到decorView下{@link #injectWindow()}
 * ================================================
 */
public class SwipeCloseLayout extends FrameLayout {
    private static final int ANIMATION_DURATION = 200;

    /**
     * 是否可以滑动关闭页面
     */
    private boolean mSwipeEnabled = false;
    private boolean mIsAnimationFinished = true;
    private boolean mCanSwipe = false;
    private boolean mIgnoreSwipe = false;
    private boolean mHasIgnoreFirstMove;

    private Fragment mFragment;
    private AppCompatActivity mActivity;
    private VelocityTracker tracker;
    private ObjectAnimator mAnimator;
    private Drawable mShadow;
    private View mContent;
    private int mScreenWidth;
    private int touchSlopLength;
    private float mDownX;
    private float mDownY;
    private float mLastX;
    private float mCurrentX;
    private int mPullMaxLength;
    private boolean mIsInjected;

    private HashMap<Integer, View> specialView = new HashMap<>();

    public SwipeCloseLayout(Context context) {
        this(context, null, 0);
    }

    public SwipeCloseLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeCloseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = (AppCompatActivity) context;
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
        mShadow = getShadowDrawable();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        touchSlopLength = (int) (20 * displayMetrics.density);
        touchSlopLength *= touchSlopLength;
        mScreenWidth = displayMetrics.widthPixels;
        mPullMaxLength = (int) (mScreenWidth * 0.33f);
        setClickable(true);
    }

    private int[] colors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK};

    public Drawable getShadowDrawable() {
        return new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
    }

    public static SwipeCloseLayout create(Activity activity, Fragment fragment) {
        SwipeCloseLayout swipeCloseLayout = new SwipeCloseLayout(activity, null, 0);
        swipeCloseLayout.mFragment = fragment;
        swipeCloseLayout.injectWindow();
        return swipeCloseLayout;
    }

    public static SwipeCloseLayout createFromActivity(Activity activity, Fragment fragment) {
        SwipeCloseLayout swipeCloseLayout = new SwipeCloseLayout(activity, null, 0);
        swipeCloseLayout.mFragment = fragment;
        swipeCloseLayout.injectWindow();
        return swipeCloseLayout;
    }

    public static SwipeCloseLayout createFromFragment(View mainView, Fragment fragment) {
        SwipeCloseLayout swipeCloseLayout = new SwipeCloseLayout(fragment.getContext(), null, 0);
        swipeCloseLayout.mFragment = fragment;
        swipeCloseLayout.injectWindow();
        return swipeCloseLayout;
    }

    /**
     * 将本view注入到decorView的子view上
     * 在{@link Activity#onPostCreate(Bundle)}里使用本方法注入
     */
    private void injectWindow() {
        if (mIsInjected)
            return;
        if (mFragment != null) {
            mContent = mFragment.getView();
            if (mContent == null) {
                throw new IllegalArgumentException("The fragment's root view is null");
            } else {
                ViewGroup viewGroup = ((ViewGroup) mContent.getParent());
                if (viewGroup != null) {
                    viewGroup.removeView(mContent);
                    addView(mContent);
                    viewGroup.addView(this);
                } else {
                    throw new IllegalArgumentException("The fragment's parent is null");
                }
            }
        } else {
            final ViewGroup root = (ViewGroup) mActivity.getWindow().getDecorView();
            mContent = root.getChildAt(0);
            root.removeView(mContent);
            this.addView(mContent, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            root.addView(this);
        }
        mIsInjected = true;
    }


    public boolean isSwipeEnabled() {
        return mSwipeEnabled;
    }

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.mSwipeEnabled = swipeEnabled;
    }

    @Override
    protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        final int shadowWidth = mShadow.getIntrinsicWidth();
        int left = (int) (getContentX()) - shadowWidth;
        mShadow.setBounds(left, child.getTop(), left + shadowWidth, child.getBottom());
        mShadow.draw(canvas);
        return result;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return shouldIntercept(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mCanSwipe || super.onInterceptTouchEvent(ev);
    }

    /**
     * 是否需要拦截
     */
    private boolean shouldIntercept(@NonNull MotionEvent ev) {
        if (!isTouchOnSpecialView(ev)) {
            if (mSwipeEnabled && !mCanSwipe && !mIgnoreSwipe) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = ev.getX();
                        mDownY = ev.getY();
                        mCurrentX = mDownX;
                        mLastX = mDownX;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = ev.getX() - mDownX;
                        float dy = ev.getY() - mDownY;
                        if (dx * dx + dy * dy > touchSlopLength) {
                            if ((dy == 0f || Math.abs(dx / dy) > 1) && (mCanSwipe || dx > 0)) {
                                mDownX = ev.getX();
                                mDownY = ev.getY();
                                mCurrentX = mDownX;
                                mLastX = mDownX;
                                mCanSwipe = true;
                                tracker = VelocityTracker.obtain();
                                return true;
                            } else {
                                mIgnoreSwipe = true;
                            }
                        }
                        break;
                }
            }
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                mIgnoreSwipe = false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mCanSwipe) {
            tracker.addMovement(event);
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    mCurrentX = mDownX;
                    mLastX = mDownX;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurrentX = event.getX();
                    float dx = mCurrentX - mLastX;
                    if (dx != 0f && !mHasIgnoreFirstMove) {
                        mHasIgnoreFirstMove = true;
                        dx = dx / dx;
                    }
                    if (getContentX() + dx < 0) {
                        setContentX(0);
                    } else {
                        setContentX(getContentX() + dx);
                    }
                    mLastX = mCurrentX;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    tracker.computeCurrentVelocity(10000);
                    tracker.computeCurrentVelocity(1000, 20000);
                    mCanSwipe = false;
                    mHasIgnoreFirstMove = false;
                    int mv = mScreenWidth * 3;
                    if (Math.abs(tracker.getXVelocity()) > mv) {
                        animateFromVelocity(tracker.getXVelocity());
                    } else {
                        if (getContentX() > mPullMaxLength) {
                            animateFinish(false);
                        } else {
                            animateBack(false);
                        }
                    }
                    tracker.recycle();
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }


    public void cancelPotentialAnimation() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
        }
    }

    public float getContentX() {
        return mContent.getX();
    }

    private void setContentX(float x) {
        mContent.setX(x);
        invalidate();
    }

    public boolean isAnimationFinished() {
        return mIsAnimationFinished;
    }

    /**
     * 弹回，不关闭，因为left是0，所以setX和setTranslationX效果是一样的
     *
     * @param withVel 使用计算出来的时间
     */
    private void animateBack(boolean withVel) {
        cancelPotentialAnimation();
        mAnimator = ObjectAnimator.ofFloat(this, "contentX", getContentX(), 0);
        int tmpDuration = withVel ? ((int) (ANIMATION_DURATION * getContentX() / mScreenWidth)) : ANIMATION_DURATION;
        if (tmpDuration < 100) {
            tmpDuration = 100;
        }
        mAnimator.setDuration(tmpDuration);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.start();
    }

    private void animateFinish(boolean withVel) {
        cancelPotentialAnimation();
        mAnimator = ObjectAnimator.ofFloat(this, "contentX", getContentX(), mScreenWidth);
        int tmpDuration = withVel ? ((int) (ANIMATION_DURATION * (mScreenWidth - getContentX()) / mScreenWidth)) : ANIMATION_DURATION;
        if (tmpDuration < 100) {
            tmpDuration = 100;
        }
        mAnimator.setDuration(tmpDuration);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimationFinished = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimationFinished = true;
                backImp();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimationFinished = true;
            }
        });
        mAnimator.start();
    }

    /**
     * 覆盖此方法 可重写最红关闭效果
     */
    public void backImp() {
        if (mFragment != null) {
            if (mActivity.getSupportFragmentManager().popBackStackImmediate()) {
                if (getParent() != null)
                    ((ViewGroup) getParent()).removeView(this);
            } else if (!mActivity.isFinishing()) {
                mActivity.finish();
                mActivity.overridePendingTransition(0, 0);//取消默认关闭动画
            }
        } else if (mActivity != null) {
            mActivity.finish();
            mActivity.overridePendingTransition(0, 0);//取消默认关闭动画
        }
    }


    private void animateFromVelocity(float v) {
        int currentX = (int) getContentX();
        if (v > 0) {
            if (currentX < mPullMaxLength && v * ANIMATION_DURATION / 1000 + currentX < mPullMaxLength) {
                animateBack(false);
            } else {
                animateFinish(true);
            }
        } else {
            if (currentX > mPullMaxLength / 3 && v * ANIMATION_DURATION / 1000 + currentX > mPullMaxLength) {
                animateFinish(false);
            } else {
                animateBack(true);
            }
        }
    }

    public void finish() {
        if (!isAnimationFinished()) {
            cancelPotentialAnimation();
        }
    }

    /**
     * 添加特殊view 防止错划
     */
    public void addSpecialView(View view) {
        if (view == null) return;
        specialView.put(view.hashCode(), view);
    }

    /**
     * 移出特殊view
     */
    public void removeSpecialView(View view) {
        if (view != null && specialView.containsKey(view.hashCode())) {
            specialView.remove(view.hashCode());
        }
    }

    private boolean isTouchOnSpecialView(MotionEvent ev) {
        if (MotionEvent.ACTION_DOWN == ev.getAction()) {
            View scrollTouchTarget = getScrollTouchTarget(this, ((int) ev.getRawX()), ((int) ev.getRawY()));
            if (scrollTouchTarget != null) {
                return true;
            }
        }
//        for (Map.Entry<Integer, View> entry : specialView.entrySet()) {
//            if (inRangeOfView(entry.getValue(), ev)) {
//                return true;
//            }
//        }
        return false;
    }

    //TODO
    @Override
    public boolean canScrollHorizontally(int direction) {
        return super.canScrollHorizontally(direction);
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getRawX() < x || ev.getRawX() > (x + view.getWidth()) || ev.getRawY() < y || ev.getRawY() > (y + view.getHeight()));
    }

    /**
     * 获取可以向 左侧滑的控件
     *
     * @param view 当前容器
     * @param x    落点 X轴
     * @param y    落点 Y轴
     * @return 可以向左侧滑的控件
     */
    private View getScrollTouchTarget(View view, int x, int y) {
        View target = null;
        ArrayList<View> TouchableViews = view.getTouchables();
        for (View child : TouchableViews) {

            if (target != null) {
                break;
            }
            //判断当前可触摸的控件是否在点击范围内
            //当前控件是否可以向右侧滑
            if (child.canScrollHorizontally(-1) && isTouchPointInView(child, x, y)) {
                target = child;
                break;
            } else {
                //检查父控件是否可以滑动
                //父控件不为空且父控件不等于view
                while (child.getParent() != null) {
                    ViewParent viewParent = child.getParent();
                    if (viewParent instanceof View) {
                        child = ((View) viewParent);
                        if (child.canScrollHorizontally(-1) && isTouchPointInView(child, x, y)) {
                            target = child;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return target;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (y >= top && y <= bottom
                && x >= left && x <= right) {
            return true;
        }
        return false;
    }
}
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * ================================================
 * Created by zhaokai on 2017/4/27.
 * Email zhaokai1033@126.com
 * Describe :
 * 侧滑关闭的布局，使用方式
 * 1、Fragment
 * 在{@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}调用
 * 并 return {@link #SwipeCloseLayout#createFromFragment(View, Fragment)}
 * 2、Activity
 * 在{@link Activity#onPostCreate(Bundle)}
 * 调用{@link #SwipeCloseLayout#createFromActivity(Activity)}
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
    private SwipeFinishCallBack finishCallBack;

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

    public static SwipeCloseLayout createFromActivity(Activity activity, SwipeFinishCallBack callBak) {
        SwipeCloseLayout swipeCloseLayout = new SwipeCloseLayout(activity, null, 0);
        swipeCloseLayout.injectWindowForActivity();
        swipeCloseLayout.finishCallBack = callBak;
        return swipeCloseLayout;
    }

    private void injectWindowForActivity() {
        if (mIsInjected) return;
        final ViewGroup root = (ViewGroup) mActivity.getWindow().getDecorView();
        mContent = root.getChildAt(0);
        root.removeView(mContent);
        this.addView(mContent, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        root.addView(this);
        mIsInjected = true;
    }

    public static SwipeCloseLayout createFromFragment(View mainView, Fragment fragment, SwipeFinishCallBack callBak) {
        SwipeCloseLayout swipeCloseLayout = new SwipeCloseLayout(fragment.getContext(), null, 0);
        swipeCloseLayout.mFragment = fragment;
        swipeCloseLayout.finishCallBack = callBak;
        swipeCloseLayout.injectWindowForFragment(mainView);
        return swipeCloseLayout;
    }

    /**
     * 将本view注入到decorView的子view上
     * 在{@link Activity#onPostCreate(Bundle)}里使用本方法注入
     */
    private void injectWindowForFragment(View view) {
        if (mIsInjected)
            return;
        if (mFragment == null || view == null) {
            throw new IllegalArgumentException("The fragment or view is null");
        }
        mContent = view;
        ViewGroup viewGroup = ((ViewGroup) mContent.getParent());
        if (viewGroup != null) {
            viewGroup.removeView(mContent);
            viewGroup.addView(this, mContent.getLayoutParams());
            try {
                changeFieldValue(mFragment, "mView", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addView(mContent);
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
                if (finishCallBack == null || !finishCallBack.onSwipeFinish(mActivity, mFragment)) {
                    backImp();
                }
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

    public boolean isTouchOnSpecialView(MotionEvent ev) {
        if (MotionEvent.ACTION_DOWN == ev.getAction()) {
            View scrollTouchTarget = getScrollTouchTarget(this, ev);
            if (scrollTouchTarget != null) {
                return true;
            }
        }
        return false;
    }

    //TODO
    @Override
    public boolean canScrollHorizontally(int direction) {
        return mCanSwipe || super.canScrollHorizontally(direction);
    }

    private View targetView;

    /**
     * 获取可以向 左侧滑的控件
     *
     * @param view 当前容器
     * @param ev   当前事件
     */
    public View getScrollTouchTarget(SwipeCloseLayout view, MotionEvent ev) {
        //当前点击位置与上次相同 则直接进入判断 适用于多个SwipeCloseLayout嵌套时，加速判断
        if (targetView != null && isTouchPointInView(targetView, ((int) ev.getRawX()), ((int) ev.getRawY()))) {

            if (targetView instanceof SwipeCloseLayout) {
                //第一种情况 触摸到 子SwipeCloseLayout 可滑动的子View，则原样返回，不在查找
                if (((SwipeCloseLayout) targetView).isTouchOnSpecialView(ev)) {
                    return targetView;
                }
                //否则未触摸到，没有可侧滑返回的控件，需要判断 targetView 是否可以滑动返回
            }
            //判断当前 targetView 是否可以滑动返回
            if (isScrollView(targetView) != null)
                return targetView;
        }
        // 继续查找对应位置 可滑动的View
        targetView = getChildViewWithLocation(view, ev);
        return targetView;
    }

    /**
     * 根据落点坐标获取可滑动的子View 不包含自己
     *
     * @param view 父容器
     * @param ev   当前触摸事件
     */
    private View getChildViewWithLocation(@NonNull ViewGroup view, MotionEvent ev) {
        View target = null;
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            if (isTouchPointInView(child, ((int) ev.getRawX()), ((int) ev.getRawY()))) {
                if (child instanceof SwipeCloseLayout) {
                    // 内部SwipeCloseLayout 可滑动
                    boolean flag = ((SwipeCloseLayout) child).isTouchOnSpecialView(ev);
//                    target = ((SwipeCloseLayout) child).getScrollTouchTarget((SwipeCloseLayout) child, ev);
                    target = flag ? null : child;
                }
                if (target == null && null == (target = isScrollView(child))) {
                    if (child instanceof ViewGroup) {
                        target = getChildViewWithLocation(((ViewGroup) child), ev);
                    }
                }
            }
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /**
     * 是否是可以滑动的view
     *
     * @param child 要判断的View
     */
    private View isScrollView(View child) {
        if (child.canScrollHorizontally(-1)) {
            return child;
        }
        return null;
    }

    /**
     * 落点是否在View 上
     */
    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        if (VISIBLE != view.getVisibility()) {
            return false;
        }
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        return y >= top && y <= bottom
                && x >= left && x <= right;
    }

    /**
     * 关闭回调
     */
    public interface SwipeFinishCallBack {
        boolean onSwipeFinish(Activity activity, Fragment fragment);
    }

    /**
     * 对给定对象obj的propertyName指定的成员变量进行赋值
     * 赋值为value所指定的值
     * <p>
     * 该方法可以访问私有成员
     */
    public static void changeFieldValue(Object obj, String propertyName, Object value) throws Exception {
        if (!(obj instanceof Fragment)) {
            throw new IllegalArgumentException("you should extends Fragment");
        }
        Class<?> clazz = Fragment.class;
        Field field = clazz.getDeclaredField(propertyName);
        //赋值前将该成员变量的访问权限打开
        field.setAccessible(true);
        field.set(obj, value);
        //赋值后将该成员变量的访问权限关闭
        field.setAccessible(false);
    }
}
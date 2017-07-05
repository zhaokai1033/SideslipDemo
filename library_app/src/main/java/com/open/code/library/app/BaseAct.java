package com.open.code.library.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.duoku.kklib.controller.FragmentController;
import com.duoku.kklib.permission.OnPermissionCallBack;
import com.duoku.kklib.permission.PermissionUtil;
import com.duoku.kklib.utils.StatusBarUtil;
import com.duoku.kklib.widget.SwipeCloseLayout;

/**
 * Created by zhaokai on 2016-05-10
 *
 * @ e-mail zhaokai1033@126.com
 * 将所有activity 进行统一管理
 * 增加公共状态页
 */
@SuppressWarnings("unused")
public abstract class BaseAct extends AppCompatActivity {

    private View[] mStateViews = new View[3];//状态页容器
    private View mStateView;//状态页
    private StatePage stateNeed = null;//初始化需要显示状态页
    private SwipeCloseLayout mSwipeBack;//侧滑返回容器

    public static final String CLOSE_ACTION = "CLOSE_ACTIVITY";
    //注册广播
    IntentFilter closeFilter = new IntentFilter(CLOSE_ACTION);
    //广播接受者
    BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    /**
     * 权限请求工具
     */
    private PermissionUtil.PermissionObject mPermissionUtil;
    private boolean mSwipeBackEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(closeReceiver, closeFilter);
        mSwipeBack = new SwipeCloseLayout(this);//侧滑控件
        //动画样式
//        getWindow().setWindowAnimations(R.style.activityAnim);
    }

    @Override
    protected final void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBack.injectWindow();
        if (getStateContentView() != 0) {
            addStateView();
        }
        mSwipeBack.setSwipeEnabled(mSwipeBackEnable);
        onPostCreated(savedInstanceState);   //初始化视图组件
    }


    /**
     * 获取 布局 资源
     *
     * @return 资源ID
     */
    protected abstract int getLayoutResID();

    /**
     * 试图创建完成时调用
     * 参数预留（savedInstanceState）
     */
    protected abstract void onPostCreated(Bundle savedInstanceState);

    /**
     * 返回状态页容器 为0则不加状态页
     *
     * @return 状态页容器
     */
    protected abstract int getStateContentView();

    /**
     * 初始化组件的方法
     * 创建过程中调用
     * 对于Activity的交互动画需要放在此方法中
     * 参数预留（savedInstanceState）
     */
    protected abstract void onCreated(Bundle savedInstanceState);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
    }

    /**
     * 关闭所有的Activity
     * 通过发送应用内广播关闭退出所有的Activity
     */
    @CallSuper
    public void exitApp() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CLOSE_ACTION));
    }

    /**
     * 是否开启侧滑关闭
     *
     * @param enable true/false
     */
//    @Override
    public void setSwipeBackEnable(boolean enable) {
        mSwipeBackEnable = enable;
        if (mSwipeBack != null) {
            mSwipeBack.setSwipeEnabled(mSwipeBackEnable);
        }
    }

    public SwipeCloseLayout getSwipeClose() {
        return mSwipeBack;
    }

    /**
     * 显示指定状态页
     *
     * @param stateCode {@link StatePage#EMPTY}    空白页
     *                  {@link StatePage#ERROR}    错误页
     *                  {@link StatePage#LOADING}  加载页
     */
    public void showStateView(StatePage stateCode) {
        if (stateCode == null) {
            hideStateView();
            return;
        }
        if (mStateView == null || mStateViews[stateCode.ordinal()] == null) {
            stateNeed = stateCode;
            return;
        }
        for (View view : mStateViews) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        mStateView.setVisibility(View.VISIBLE);
        mStateViews[stateCode.ordinal()].setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏所有状态页显示主页面
     */
    public void hideStateView() {
        if (mStateView == null) {
            return;
        }
        for (View view : mStateViews) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        mStateView.setVisibility(View.GONE);
    }

    /**
     * 隐藏指定状态页
     *
     * @param state {@link StatePage}
     */
    public void hideStateView(StatePage state) {
        if (mStateViews[state.ordinal()] != null)
            mStateViews[state.ordinal()].setVisibility(View.GONE);
    }

    /**
     * 隐藏指定状态页
     *
     * @param state {@link StatePage}
     */
    public View getStateView(StatePage state) {
        if (mStateViews[state.ordinal()] != null)
            return mStateViews[state.ordinal()];
        return null;
    }

    /**
     * 状态页点击事件
     *
     * @param state {@link StatePage#EMPTY}    空白页
     *              {@link StatePage#ERROR}    错误页
     *              {@link StatePage#LOADING}  加载页
     */
    protected void onStateClick(View view, StatePage state) {

    }

    /**
     * "空"状态页
     */
    protected View getEmptyView(ViewGroup parents) {
        return null;
    }

    /**
     * "错误"状态页
     */
    protected View getErrorView(ViewGroup parents) {
        return null;
    }

    /**
     * "加载中"状态页
     */
    protected View getLoadingView(ViewGroup parents) {
        return null;
    }

    /**
     * 提供公开方法设置 状态栏颜色
     *
     * @param color 状态栏颜色
     */
    public void setStatus(@ColorRes int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            StatusBarUtil.setTransparent(this);
        }
    }


    /**
     * 替换Fragment
     *
     * @param fragment      指定的Fragment 资源
     * @param layoutRes     需要替换的Fragment 位置
     * @param isNeedRefresh 是否需要刷新 You must override {@link BaseFra#refresh()} to refresh
     */
    public void replaceFragment(BaseFra fragment, int layoutRes, boolean isNeedRefresh, boolean hasAnim) {
        if (fragment == null) return;
        FragmentController.replaceFragment(this, layoutRes, fragment, hasAnim);
        if (isNeedRefresh) {
            fragment.refresh();
        }
    }

    /**
     * 切换Fragment
     *
     * @param current       当前显示的Fragment
     * @param target        需要显示的Fragment
     * @param layoutRes     位置
     * @param isNeedRefresh 切换后是否需要刷新 You must override {@link BaseFra#refresh()} to refresh
     * @param canBack       是否支持回退
     * @return 最终显示的Fragment
     */
    public BaseFra changeFragment(BaseFra current, BaseFra target, int layoutRes, boolean isNeedRefresh, boolean canBack) {
        FragmentController.changeFragment(this, current, target, layoutRes, canBack, true);
        if (isNeedRefresh)
            target.refresh();
        return target;
    }

    /**
     * 请求权限
     *
     * @param requestCode 权限请求码
     * @param callBack    处理结果
     * @param permission  请求的权限
     */
    public void getPermission(int requestCode, OnPermissionCallBack callBack, String... permission) {
        if (mPermissionUtil == null) {
            mPermissionUtil = PermissionUtil.with(this);
        }
        mPermissionUtil.createRequest(requestCode, callBack, permission).request(requestCode);
    }

    /**
     * 检测是否已有权限
     *
     * @param permission 权限名称
     * @return 是否权限是否
     */
    public boolean checkPermission(@NonNull String permission) {
        if (mPermissionUtil == null) {
            mPermissionUtil = PermissionUtil.with(this);
        }
        return mPermissionUtil.check(permission)[0];
    }

    /**
     * 检测是否已有权限
     *
     * @param permissions 权限名称
     * @return 是否权限是否允许的数组
     */
    public boolean[] checkPermission(@NonNull String... permissions) {
        if (mPermissionUtil == null) {
            mPermissionUtil = PermissionUtil.with(this);
        }
        return mPermissionUtil.check(permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionUtil != null) {
            mPermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //**********************************私有方法*********************************/

    /**
     * 添加状态页
     */
    private void addStateView() {
        mStateView = (findViewById(getStateContentView()));
//        mStateView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
        if (mStateView instanceof ViewGroup) {
            ((ViewGroup) mStateView).removeAllViews();
            mStateViews[StatePage.LOADING.ordinal()] = getLoadingView((ViewGroup) mStateView);
            if (mStateViews[StatePage.LOADING.ordinal()] != null) {
                ((ViewGroup) mStateView).addView(mStateViews[StatePage.LOADING.ordinal()], getDefaultParams());
                mStateViews[StatePage.LOADING.ordinal()].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStateClick(v, StatePage.LOADING);
                    }
                });
                mStateViews[StatePage.LOADING.ordinal()].setVisibility(View.GONE);
            }
            mStateViews[StatePage.ERROR.ordinal()] = getErrorView((ViewGroup) mStateView);
            if (mStateViews[StatePage.ERROR.ordinal()] != null) {
                ((ViewGroup) mStateView).addView(mStateViews[StatePage.ERROR.ordinal()], getDefaultParams());
                mStateViews[StatePage.ERROR.ordinal()].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStateClick(v, StatePage.ERROR);
                    }
                });
                mStateViews[StatePage.ERROR.ordinal()].setVisibility(View.GONE);
            }
            mStateViews[StatePage.EMPTY.ordinal()] = getEmptyView((ViewGroup) mStateView);
            if (mStateViews[StatePage.EMPTY.ordinal()] != null) {
                ((ViewGroup) mStateView).addView(mStateViews[StatePage.EMPTY.ordinal()], getDefaultParams());
                mStateViews[StatePage.EMPTY.ordinal()].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStateClick(v, StatePage.EMPTY);
                    }
                });
                mStateViews[StatePage.EMPTY.ordinal()].setVisibility(View.GONE);
            }
            mStateView.setVisibility(View.GONE);
            if (stateNeed != null) {
                showStateView(stateNeed);
            }
        } else {
            throw new IllegalArgumentException("StateView should be ViewGroup");
        }
    }

    private ViewGroup.LayoutParams getDefaultParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    //**********************************隐藏的方法*********************************/
    //============================
    // 修改时间：2017/5/2 下午1:40 修改人：zhaokai
    // 描述：隐藏以下方法
    //============================
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//    }

    @Override
    public final View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public final View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

//    @Override
//    public final void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onPostCreate(savedInstanceState, persistentState);
//    }
}

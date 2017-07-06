package com.open.code.library.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * ================================================
 * Describe：
 * Created by zhaokai on 2017/3/6.
 * Email zhaokai1033@126.com
 * 带状态页的Fragment
 * ================================================
 */

@SuppressWarnings("unused")
public abstract class BaseFra extends Fragment {
    private static final String TAG = "BaseFra";
    private View[] mStateViews = new View[3];//状态页容器
    private View mStateView;//状态页
    private StatePage stateNeed = null;//初始化需要显示状态页
    protected View mainView;
    //    private HashMap<String, View> stateView = new HashMap<>();
    protected LayoutInflater inflater;
    public static final String NORMAL = "normal";

    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater == null ? LayoutInflater.from(getContext()) : inflater;
        //创建之前做一些准备工作
        readyBeforeCreate();

        if (isAddToFrameLayout() || isShowTitle()) {
            FrameLayout frameLayout = null;
            LinearLayout linearLayout = null;
            if (isAddToFrameLayout()) {
                frameLayout = new FrameLayout(getContext());
                frameLayout.addView(onCreateView(frameLayout, savedInstanceState));
                mStateView = new FrameLayout(getContext());
                mStateView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mStateViews[StatePage.LOADING.ordinal()] = getLoadingView(((ViewGroup) mStateView));
                mStateViews[StatePage.EMPTY.ordinal()] = getEmptyView((ViewGroup) mStateView);
                mStateViews[StatePage.ERROR.ordinal()] = getErrorView((ViewGroup) mStateView);
                for (int i = 0; i < mStateViews.length; i++) {
                    ((ViewGroup) mStateView).addView(mStateViews[i]);
                    final StatePage state = StatePage.getState(i);
                    mStateViews[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onStateClick(v, state);
                        }
                    });
                }
                frameLayout.addView(mStateView);
                if (stateNeed != null) {
                    showStateView(stateNeed);
                }
            }

            if (isShowTitle() && getTitleView() != null) {
                linearLayout = new LinearLayout(getContext());
                linearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(getTitleView());
                if (frameLayout != null) {
                    linearLayout.addView(frameLayout);
                } else {
                    linearLayout.addView(onCreateView(linearLayout, savedInstanceState));
                }
            }
            if (linearLayout != null && frameLayout != null) {
                mainView = linearLayout;
            } else if (frameLayout != null) {
                mainView = frameLayout;
            }
        } else {
            mainView = onCreateView(container, savedInstanceState);
        }
        if (mainView == null) {
            throw new IllegalArgumentException("mainView Can not be null");
        }
//        方式 ①: 有状态页在此处接入
//        return SwipeCloseLayout.createFromFragment(mainView, this, null);//侧滑控件
        return mainView;
    }

    public abstract View onCreateView(ViewGroup container, Bundle savedInstanceState);


    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 状态页  建议静态页面
     * string : 对应页面所属状态
     */
    @Deprecated
    protected Map<String, View> getStatesViews() {
        return null;
    }

    /**
     * 是否分状态页
     */
    protected boolean isAddToFrameLayout() {
        return false;
    }

    /**
     * 自定义的标题栏
     */
    protected View getTitleView() {
        return null;
    }

    /**
     * 是否显示标题栏
     */
    public boolean isShowTitle() {
        return false;
    }

    /**
     * 获取布局资源
     *
     * @return 布局资源的ID
     */
    protected abstract int getLayoutRes();


    /**
     * 外部刷新整个页面
     */
    public void refresh() {
    }


    /**
     * 初始化View的数据
     */
    protected void readyBeforeCreate() {
    }

    /**
     * 屏蔽以下操作
     * 所属Activity 创建 此时应尽量不做操作
     */
    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 脱离所属Activity时调用，此时fragment 以不在使用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 在onDestroy 之前调用 此时Fragment 状态已经保存
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public View getMainView() {
        return mainView;
    }

    /**
     * 重新可见时调用 Activity配合
     */
    public void onFragmentResume() {

    }

    public void onFragmentPause() {

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
}

package com.open.code.library.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.duoku.kklib.widget.SwipeCloseLayout;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * ================================================
 * Describe：
 * Created by zhaokai on 2017/3/6.
 * Email zhaokai1033@126.com
 * ================================================
 */

@SuppressWarnings("unused")
public abstract class BaseFra extends Fragment {

    private View[] mStateViews = new View[3];//状态页容器
    private View mStateView;//状态页
    private StatePage stateNeed = null;//初始化需要显示状态页
    protected View mainView;
    //    private HashMap<String, View> stateView = new HashMap<>();
    protected LayoutInflater inflater;
    public static final String NORMAL = "normal";
    private SwipeCloseLayout mSwipeClose;

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
        mSwipeClose = new SwipeCloseLayout(getActivity(), this);
        mSwipeClose.injectWindow();
        return mSwipeClose;
    }

    public View onCreateView(ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    /**
     * 是否开启侧滑关闭
     *
     * @param enable true/false
     */
//    @Override
    public void setSwipeBackEnable(boolean enable) {
        if (mSwipeClose != null) {
            mSwipeClose.setSwipeEnabled(enable);
        }
    }

    /**
     * 增加特殊View 防止滑动冲突
     */
    public void addSwipeSpecialView(View view) {
        if (mSwipeClose != null) {
            mSwipeClose.addSpecialView(view);
        }
        if (getActivity() instanceof BaseAct)
            ((BaseAct) getActivity()).addSwipeSpecialView(view);
    }

//    /**
//     * 指定状态页到前端
//     */
//    public void bringStateToFront(String state) {
//        if (stateView.get(state) != null) {
//            stateView.get(state).bringToFront();
//        }
//    }

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
     * 页面被创建完成时调用   可用于对页面的初始化操作
     *
     * @param view               根视图
     * @param savedInstanceState 保存状态
     */
    @Override
    public abstract void onViewCreated(View view, @Nullable Bundle savedInstanceState);

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

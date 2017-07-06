package com.open.code.sideslipdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.open.code.library.app.BaseFra;
import com.open.code.library.widget.SwipeCloseLayout;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public abstract class BaseFragment extends BaseFra {

    private static final String TAG = "BaseFragment";
    private SwipeCloseLayout mSwipeClose;

    private boolean mCanSwipeClose;//是否可以侧滑关闭

    public SwipeCloseLayout getSwipeClose() {
        return mSwipeClose;
    }

    @Override
    public final View onCreateView(ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
//        方式 ①: 在此处接入 此处在添加状态页需注意
        view = mSwipeClose = SwipeCloseLayout.createFromFragment(view, this, null);//侧滑控件
        return view;
    }

    /**
     * 页面被创建完成时调用   可用于对页面的初始化操作
     *
     * @param view               根视图
     * @param savedInstanceState 保存状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        方式②
//        mSwipeClose = SwipeCloseLayout.createFromFragment(view, this, null);//侧滑控件
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
            mSwipeClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup viewGroup = ((ViewGroup) v.getParent());
                    if (viewGroup != null) {
                        for (int i = 0; i < viewGroup.getChildCount(); i++) {
                            Log.d(TAG, "child:" + viewGroup.getChildAt(i).getClass().getSimpleName());
                        }
                    }
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "点击Fragment返回控件", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mCanSwipeClose = enable;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        设置初始值
        setSwipeBackEnable(true);
    }
}

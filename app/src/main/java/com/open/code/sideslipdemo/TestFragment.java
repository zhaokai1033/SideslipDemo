package com.open.code.sideslipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class TestFragment extends BaseFragment {

    public static TestFragment newInstance() {

        Bundle args = new Bundle();

        TestFragment fragment = new TestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_test;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.root).setBackgroundColor(Util.randomColor());
        view.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getContext(), "点击Fragment");
            }
        });
        view.findViewById(R.id.bt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getContext(), "增加 Activity");
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        view.findViewById(R.id.bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getContext(), "增加 Fragment");
                ((BaseActivity) getActivity()).changeFragment(null, TestFragment.newInstance(), R.id.container, false, true);
            }
        });

        ViewPager vp = ((ViewPager) view.findViewById(R.id.view_pager));
        vp.setAdapter(new PagerViewAdapter());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSwipeBackEnable(true);
    }
}

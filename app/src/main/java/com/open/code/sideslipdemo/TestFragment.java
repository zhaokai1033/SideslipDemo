package com.open.code.sideslipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class TestFragment extends BaseFragment {
    private static final String TAG = "TestFragment";

    private static int index = 1;

    public static TestFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("INDEX", index++);
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
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            int i = getArguments().getInt("INDEX");
            ((TextView) view.findViewById(R.id.title)).setText("TestFragment" + i);
        }
        View root = view.findViewById(R.id.root);
        root.setTag(TAG);
        root.setBackgroundColor(Util.randomColor());
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getContext(), "点击 Fragment");
            }
        });
        View bt1 = view.findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getContext(), "增加 Activity");
                startActivity(new Intent(getActivity(), TestActivity.class));
            }
        });
        bt1.setTag(TAG);
        View bt2 = view.findViewById(R.id.bt2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getContext(), "增加 Fragment");
                ((BaseActivity) getActivity()).changeFragment(null, TestFragment.newInstance(), R.id.container, false, true);
            }
        });
        bt2.setTag(TAG);
        ViewPager vp = ((ViewPager) view.findViewById(R.id.view_pager));
        vp.setAdapter(new PagerViewAdapter());
        vp.setTag(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSwipeClose().setTag(TAG);
    }
}

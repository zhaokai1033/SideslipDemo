package com.open.code.sideslipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

public class TestActivity extends BaseActivity {

    private static final String TAG = "TestActivity";
    private static int index = 1;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_test;
    }

    @Override
    protected int getStateContentView() {
        return 0;
    }

    @Override
    protected void onCreated(Bundle savedInstanceState) {
        View root = findViewById(R.id.root);
        root.setTag(TAG);
        root.setBackgroundColor(Util.randomColor());
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "点击Activity_Main");
            }
        });
        View bt1 = findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "增加 Activity");
                startActivity(new Intent(TestActivity.this, TestActivity.class));
            }
        });
        bt1.setTag(TAG);
        View bt2 = findViewById(R.id.bt2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "增加 Fragment");
                changeFragment(null, TestFragment.newInstance(), R.id.container, false, true);
            }
        });
        bt2.setTag(TAG);
        ViewPager vp = ((ViewPager) findViewById(R.id.view_pager));
        vp.setAdapter(new PagerViewAdapter());
        vp.setTag(TAG);
        setTitle("TestActivity:" + ++index);
    }

    @Override
    protected void onPostCreated(Bundle savedInstanceState) {
        super.onPostCreated(savedInstanceState);
        setSwipeBackEnable(true);
        getSwipeClose().setTag(TAG);
    }
}

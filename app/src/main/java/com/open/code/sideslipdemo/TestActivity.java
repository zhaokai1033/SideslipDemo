package com.open.code.sideslipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

public class TestActivity extends BaseActivity {


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
        findViewById(R.id.root).setBackgroundColor(Util.randomColor());
        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "点击Activity_Test");
            }
        });
        findViewById(R.id.bt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "增加 Activity");
                startActivity(new Intent(TestActivity.this, TestActivity.class));
            }
        });
        findViewById(R.id.bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "增加 Fragment");
                changeFragment(null, TestFragment.newInstance(), R.id.container, false, true);
            }
        });

        ViewPager vp = ((ViewPager) findViewById(R.id.view_pager));
        vp.setAdapter(new PageFragmentAdapter(getSupportFragmentManager()));
    }

    @Override
    protected void onPostCreated(Bundle savedInstanceState) {
        super.onPostCreated(savedInstanceState);
        setSwipeBackEnable(true);
    }
}

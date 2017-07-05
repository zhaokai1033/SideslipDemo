package com.open.code.sideslipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
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
                Util.toast(getApplicationContext(), "点击Activity_Main");
            }
        });
        findViewById(R.id.bt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "增加 Activity");
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });
        findViewById(R.id.bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.toast(getApplicationContext(), "增加 Fragment");
                changeFragment(null, TestFragment.newInstance(), R.id.container, false, true);
            }
        });
    }

    @Override
    protected void onPostCreated(Bundle savedInstanceState) {
        super.onPostCreated(savedInstanceState);
        setSwipeBackEnable(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

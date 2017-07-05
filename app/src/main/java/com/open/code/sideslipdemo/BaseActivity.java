package com.open.code.sideslipdemo;

import android.os.Bundle;

import com.open.code.library.app.BaseAct;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public abstract class BaseActivity extends BaseAct {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        onCreated(savedInstanceState);
    }
}

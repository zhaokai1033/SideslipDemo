package com.open.code.sideslipdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.open.code.library.app.BaseFra;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public abstract class BaseFragment extends BaseFra {

    @Override
    public final View onCreateView(ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutRes(), container, false);
    }
}

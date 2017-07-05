package com.open.code.sideslipdemo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class PageFragmentAdapter extends FragmentPagerAdapter {


    public PageFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 5;
    }
}

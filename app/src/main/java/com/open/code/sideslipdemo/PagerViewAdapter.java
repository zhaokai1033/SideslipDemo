package com.open.code.sideslipdemo;

import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class PagerViewAdapter extends PagerAdapter {

    private int[] bgColors;

    public PagerViewAdapter() {
        super();
        bgColors = new int[getCount()];
        for (int i = 0; i < bgColors.length; i++) {
            bgColors[i] = Util.randomColor();
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TextView textView = new TextView(container.getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setText("Pager_" + position);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(56);
        textView.setBackgroundColor(bgColors[position]);
        textView.setClickable(true);
        container.addView(textView);
        return textView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((View) object));
    }
}

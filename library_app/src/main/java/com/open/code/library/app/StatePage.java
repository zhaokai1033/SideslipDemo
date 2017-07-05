package com.open.code.library.app;

/**
 * ========================================
 * Created by zhaokai on 2017/6/16.
 * Email zhaokai1033@126.com
 * des:
 * 状态
 * ========================================
 */

public enum StatePage {

    LOADING, EMPTY, ERROR;

    // 普通方法
    public static StatePage getState(int index) {
        for (StatePage c : StatePage.values()) {
            if (c.ordinal() == index) {
                return c;
            }
        }
        return null;
    }
}

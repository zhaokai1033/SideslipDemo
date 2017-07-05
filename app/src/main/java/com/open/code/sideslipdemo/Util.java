package com.open.code.sideslipdemo;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import java.util.Random;

/**
 * ========================================
 * Created by zhaokai on 2017/7/5.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class Util {

    private static Toast toast;

    public static int randomColor() {
        Random rnd = new Random();
        return Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public static void toast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }
}

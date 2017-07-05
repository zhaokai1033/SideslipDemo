package com.open.code.library.common;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ================================================
 * <p>
 * Created by zhaokai on 2017/3/3.
 * Email zhaokai1033@126.com
 * <p>
 * 日志控制-可打印log到本地文件或控制台
 * <p>
 * ================================================
 */

@SuppressWarnings("unused")
public class LogUtil {
    /**
     * 默认的文库日志Tag标签
     */
    private static final String DEFAULT_TAG = "_";

    //是否开启日志
    private static boolean mIsDebugOpen = true;
    //是否开启日志详情
    private static boolean mLogDetail = true;
    //日志保存位置
    private static String mLogFilePath;
    //日志是否需要保存
    private static boolean mIsNeedPrint = false;

    private static String[] logLevel = {"V", "D", "I", "W", "E", "A"};
    private static final Object mLogLock = new Object();

    public static void initLog(boolean isDebugOpen, boolean isNeedPrint, String logPath) {
        LogUtil.mIsDebugOpen = isDebugOpen;
        LogUtil.mIsNeedPrint = isNeedPrint;
        LogUtil.mLogFilePath = logPath;
    }

    private static String appendDetailLog(StackTraceElement[] sElements, String log) {
        return "[" +
                sElements[1].getMethodName() +
                ":" +
                sElements[1].getLineNumber() +
                "]" +
                log;
    }

    /**
     * 打印Verbose级别的log
     *
     * @param tag tag标签
     * @param msg 内容
     */
    public static void v(String tag, String msg) {

        if (mLogDetail || mIsNeedPrint) {
            StackTraceElement[] sElements = new Throwable().getStackTrace();
            msg = appendDetailLog(sElements, msg);
        }

        if (mIsDebugOpen) {
            Log.v(DEFAULT_TAG + tag, msg);
        }

        if (mIsNeedPrint) {
            print(0, tag, msg);
        }
    }


    /**
     * 打印debug级别的log
     *
     * @param tag tag标签
     * @param msg 内容
     */
    public static void d(String tag, String msg) {

        if (mLogDetail || mIsNeedPrint) {
            StackTraceElement[] sElements = new Throwable().getStackTrace();
            msg = appendDetailLog(sElements, msg);
        }

        if (mIsDebugOpen) {
            Log.d(DEFAULT_TAG + tag, msg);
        }

        if (mIsNeedPrint) {
            print(1, tag, msg);
        }
    }

    /**
     * 打印info级别的log
     *
     * @param msg 内容
     */
    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    /**
     * 打印info级别的log
     *
     * @param tag tag标签
     * @param msg 内容
     */
    public static void i(String tag, String msg) {
        if (mLogDetail || mIsNeedPrint) {
            StackTraceElement[] sElements = new Throwable().getStackTrace();
            msg = appendDetailLog(sElements, msg);
        }

        if (mIsDebugOpen) {
            Log.i(DEFAULT_TAG + tag, msg);
        }

        if (mIsNeedPrint) {
            print(2, tag, msg);
        }
    }

    /**
     * 打印warning级别的log
     *
     * @param tag tag标签
     * @param msg 内容
     */
    public static void w(String tag, String msg) {

        if (mLogDetail || mIsNeedPrint) {
            StackTraceElement[] sElements = new Throwable().getStackTrace();
            msg = appendDetailLog(sElements, msg);
        }

        if (mIsDebugOpen) {
            Log.w(DEFAULT_TAG + tag, msg);
        }

        if (mIsNeedPrint) {
            print(3, tag, msg);
        }
    }

    /**
     * 打印error级别的log
     *
     * @param tag tag标签
     */
    public static void e(String tag, String msg) {
        if (mLogDetail || mIsNeedPrint) {
            StackTraceElement[] sElements = new Throwable().getStackTrace();
            msg = appendDetailLog(sElements, msg);
        }

        if (mIsDebugOpen) {
            Log.e(DEFAULT_TAG + tag, msg);
        }

        if (mIsNeedPrint) {
            print(4, tag, msg);
        }
    }

    /**
     * 打印JSON
     */
    public static void json(String json) {
        if (TextUtils.isEmpty(json)) {
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(2);
                showLongCompletion(DEFAULT_TAG, message, 3000);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(2);
                showLongCompletion(DEFAULT_TAG, message, 3000);
                return;
            }
            e(DEFAULT_TAG, "Invalid Json");
        } catch (JSONException e) {
            e(DEFAULT_TAG, "Invalid Json");
        }
    }


    public static void showLongCompletion(String tag, String logContent, int max) {
        int index = 0;
        String sub;
        int segment = 1;
        while (index < logContent.length()) {
            if (logContent.length() <= index + max) {
                sub = logContent.substring(index);
            } else {
                sub = logContent.substring(index, index + max);
            }
            index += max;
            Log.d(tag + "-->", "|=====" + (segment++) + "=====|\n" + sub);
        }
    }


    /**
     * 打印日志到本地
     *
     * @param level 打印等级
     * @param tag   标记
     * @param msg   消息
     * @return 是否成功
     */
    private static int print(int level, String tag, String msg) {
        int pLevel = 4;
        if (level < pLevel) {
            return 0;
        }
        SimpleDateFormat df = new SimpleDateFormat("[MM-dd HH:mm:ss.SSS]",
                Locale.CHINESE);
        String time = df.format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(time);
        sb.append("\t");
        sb.append(logLevel[level]);
        sb.append("/");
        sb.append(tag);
        int pid = Process.myPid();
        sb.append("(");
        sb.append(pid);
        sb.append("):");
        sb.append(msg);
        sb.append("\n");
        FileWriter writer = null;

        synchronized (mLogLock) {
            try {
                File file;
                if (TextUtils.isEmpty(mLogFilePath)) {
                    file = new File(android.os.Environment.getExternalStorageDirectory(), "LogUtil");
                } else {
                    file = new File(mLogFilePath, "LogUtil");
                }

                if (!file.exists()) {
                    boolean ignored = file.createNewFile();
                }
                writer = new FileWriter(file, true);
                writer.write(sb.toString());
            } catch (FileNotFoundException e) {
                return -1;
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return 0;
    }
}



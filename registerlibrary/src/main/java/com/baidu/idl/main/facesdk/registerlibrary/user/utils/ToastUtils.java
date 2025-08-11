package com.baidu.idl.main.facesdk.registerlibrary.user.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {

    private static Toast mToast;
    private static Handler mHandler  = new Handler(Looper.getMainLooper());
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    public static void toast(final Context context, final String text) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
//            }
//        });
        mHandler.removeCallbacks(r);
        if (mToast != null) {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        } else {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        }
        mHandler.postDelayed(r, 100);
        mToast.show();
    }

    public static void toast(final Context context, final int resId) {
        mHandler .post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

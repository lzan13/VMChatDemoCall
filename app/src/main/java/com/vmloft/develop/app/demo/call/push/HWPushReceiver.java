package com.vmloft.develop.app.demo.call.push;

import android.content.Context;
import android.os.Bundle;

import com.hyphenate.chat.EMHWPushReceiver;
import com.vmloft.develop.library.tools.utils.VMLog;

/**
 * Created by lzan13 on 2017/7/19.
 * 华为推送广播接收器
 */
public class HWPushReceiver extends EMHWPushReceiver {
    private static final String TAG = EMHWPushReceiver.class.getSimpleName();

    @Override public void onToken(Context context, String token, Bundle extras) {
        super.onToken(context, token, extras);
        //没有失败回调，假定token失败时token为null
        if (token != null) {
            VMLog.d("register huawei push token success");
        } else {
            VMLog.e("register huawei push token fail");
        }
    }

    @Override
    public void onEvent(Context context, Event event, Bundle bundle) {
        super.onEvent(context, event, bundle);
        VMLog.d("huawei hms push notification event");
    }
}

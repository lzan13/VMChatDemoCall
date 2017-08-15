package com.vmloft.develop.app.demo.call.push;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.hyphenate.chat.EMHWPushReceiver;
import com.vmloft.develop.library.tools.utils.VMLog;

/**
 * Created by lzan13 on 2017/7/19.
 * 华为推送广播接收器
 */
public class HWPushReceiver extends EMHWPushReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override public void onToken(Context context, String token, Bundle bundle) {
        super.onToken(context, token, bundle);
        if (!TextUtils.isEmpty(token)) {
            VMLog.d("获取华为推送 token 成功！ token: %s", token);
        } else {
            VMLog.e("获取华为推送 token 失败 ~");
        }
        //if (token != null) {
        //    EMLog.d(TAG, "register huawei push token success");
        //    EMPushHelper.getInstance().onReceiveToken(token);
        //} else {
        //    EMLog.e(TAG, "register huawei push token fail");
        //    EMPushHelper.getInstance().onReceiveToken((String) null);
        //}
    }

    /**
     * 推送消息下来时会自动回调onPushMsg方法实现应用透传消息处理。本接口必须被实现。 在开发者网站上发送push消息分为通知和透传消息
     * 通知为直接在通知栏收到通知，通过点击可以打开网页，应用 或者富媒体，不会收到onPushMsg消息
     * 透传消息不会展示在通知栏，应用会收到onPushMsg
     */
    @Override public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            //可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            VMLog.i("华为推送透传消息: %s", content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return super.onPushMsg(context, bytes, bundle);
        // 消息处理之后这里要返回 false
        return false;
    }

    @Override public void onEvent(Context context, Event event, Bundle bundle) {
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = bundle.getInt(BOUND_KEY.pushNotifyId, 0);
            VMLog.i("华为推送通知栏点击事件，notifyId: %d", notifyId);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
        }

        String message = bundle.getString(BOUND_KEY.pushMsgKey);
        VMLog.i("华为推送通知内容: %s", message);
        super.onEvent(context, event, bundle);
    }
}

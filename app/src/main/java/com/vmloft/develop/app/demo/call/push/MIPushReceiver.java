package com.vmloft.develop.app.demo.call.push;

import android.content.Context;
import com.hyphenate.chat.EMMipushReceiver;
import com.vmloft.develop.library.tools.utils.VMLog;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import java.util.List;

/**
 * Created by lzan13 on 2017/5/5.
 * 集成小米推送相关推送广播接收器，处理推送相关数据信息，这里只是接受环信离线消息通知，不做任何处理
 */
public class MIPushReceiver extends EMMipushReceiver {
    // 当前账户的 regId
    private String regId = null;

    /**
     * 接收客户端向服务器发送注册命令消息后返回的响应
     *
     * @param context 上下文对象
     * @param miPushCommandMessage 注册结果
     */
    @Override public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onReceiveRegisterResult(context, miPushCommandMessage);
        String command = miPushCommandMessage.getCommand();
        List<String> arguments = miPushCommandMessage.getCommandArguments();
        String cmdArg1 = null;
        String cmdArg2 = null;
        if (arguments != null && arguments.size() > 0) {
            cmdArg1 = arguments.get(0);
        }
        if (arguments != null && arguments.size() > 1) {
            cmdArg2 = arguments.get(1);
        }
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                regId = cmdArg1;
            }
        }
        VMLog.d("onReceiveRegisterResult regId: %s", regId);
    }

    /**
     * 接收服务器推送的透传消息
     *
     * @param context 上下文对象
     * @param miPushMessage 推送消息对象
     */
    @Override public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);
    }

    /**
     * 接收服务器推送的通知栏消息（消息到达客户端时触发，并且可以接收应用在前台时不弹出通知的通知消息）
     *
     * @param context 上下文
     * @param miPushMessage 推送消息对象
     */
    @Override public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        miPushMessage.setTitle("这里是客户端设置 title");
        super.onNotificationMessageArrived(context, miPushMessage);
    }

    /**
     * 接收服务器发来的通知栏消息（用户点击通知栏时触发）
     *
     * @param context 上下文对象
     * @param miPushMessage 消息对象
     */
    @Override public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageClicked(context, miPushMessage);
    }

    /**
     * 接收客户端向服务器发送命令消息后返回的响应
     *
     * @param context 上下文对象
     * @param miPushCommandMessage 服务器响应的命令消息对象
     */
    @Override public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onCommandResult(context, miPushCommandMessage);
    }
}

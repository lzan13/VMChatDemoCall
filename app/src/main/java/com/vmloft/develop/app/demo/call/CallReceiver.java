package com.vmloft.develop.app.demo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hyphenate.chat.EMClient;

/**
 * Created by lzan13 on 2016/10/18.
 *
 * 通话呼叫监听广播实现，用来监听其他账户对自己的呼叫
 */
public class CallReceiver extends BroadcastReceiver {

    public CallReceiver() {
    }

    @Override public void onReceive(Context context, Intent intent) {
        // 判断环信是否登录成功
        if (!EMClient.getInstance().isLoggedInBefore()) {
            return;
        }

        // 呼叫方的usernmae
        String callFrom = intent.getStringExtra("from");
        // 呼叫类型，有语音和视频两种
        String callType = intent.getStringExtra("type");
        // 呼叫接收方，
        String callTo = intent.getStringExtra("to");
        // 获取通话时的扩展字段
        String callExt = EMClient.getInstance().callManager().getCurrentCallSession().getExt();

        Intent callIntent = new Intent();
        // 根据通话类型跳转到语音通话或视频通话界面
        if (callType.equals("video")) {
            // 设置当前通话类型为视频通话
            CallManager.getInstance().setCallType(CallManager.CallType.VIDEO);
            callIntent.setClass(context, VideoCallActivity.class);
        } else if (callType.equals("voice")) {
            // 设置当前通话类型为语音通话
            CallManager.getInstance().setCallType(CallManager.CallType.VOICE);
            callIntent.setClass(context, VoiceCallActivity.class);
        }
        // 初始化通化管理类的一些属性
        CallManager.getInstance().setChatId(callFrom);
        CallManager.getInstance().setInComingCall(true);

        // 设置 activity 启动方式
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }
}

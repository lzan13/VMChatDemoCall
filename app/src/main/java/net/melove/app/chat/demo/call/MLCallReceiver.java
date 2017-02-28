package net.melove.app.chat.demo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hyphenate.chat.EMClient;

/**
 * 通话呼叫监听广播实现，用来监听其他账户对自己的呼叫
 */
public class MLCallReceiver extends BroadcastReceiver {
    private String TYPE_VIDEO = "video";
    private String TYPE_VOICE = "voice";

    public MLCallReceiver() {
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
        // 呼叫接收方
        String callTo = intent.getStringExtra("to");

        // 判断下当前被呼叫的为自己的时候才启动通话界面 TODO 这个当不同appkey下相同的username时就无效了
        if (callTo.equals(EMClient.getInstance().getCurrentUser())) {
            Intent callIntent = new Intent();
            // 根据通话类型跳转到语音通话或视频通话界面
            if (callType.equals(TYPE_VIDEO)) {
                // 设置当前通话类型为视频通话
                MLCallManager.getInstance().setCallType(MLCallManager.CallType.VIDEO);
                callIntent.setClass(context, MLVideoCallActivity.class);
            } else if (callType.equals(TYPE_VOICE)) {
                // 设置当前通话类型为语音通话
                MLCallManager.getInstance().setCallType(MLCallManager.CallType.VOICE);
                callIntent.setClass(context, MLVoiceCallActivity.class);
            }
            // 初始化通化管理类的一些属性
            MLCallManager.getInstance().setChatId(callFrom);
            MLCallManager.getInstance().setInComingCall(true);

            // 设置 activity 启动方式
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);
        }
    }
}

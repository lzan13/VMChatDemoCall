package net.melove.app.chat.demo.call;

import com.hyphenate.chat.EMCallStateChangeListener;
import net.melove.app.chat.demo.call.utils.MLLog;

/**
 * Created by lzan13 on 2016/10/18.
 * 通话状态监听类，用来监听通话过程中状态的变化
 */

public class MLCallStateListener implements EMCallStateChangeListener {

    @Override public void onCallStateChanged(CallState callState, CallError callError) {

        switch (callState) {
            case CONNECTING: // 正在呼叫对方
                MLLog.i("正在呼叫对方" + callError);
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.CONNECTING);
                break;
            case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
                MLLog.i("正在等待对方接受呼叫申请" + callError);
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.CONNECTED);
                break;
            case ACCEPTED: // 通话已接通
                MLLog.i("通话已接通");
                MLCallManager.getInstance().stopCallSound();
                MLCallManager.getInstance().setEndType(MLCallManager.EndType.NORMAL);
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.ACCEPTED);
                break;
            case DISCONNECTED: // 通话已中断
                MLLog.i("通话已结束" + callError);
                // 通话结束，重置通话状态
                if (callError == CallError.ERROR_UNAVAILABLE) {
                    MLLog.i("对方不在线" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.OFFLINE);
                } else if (callError == CallError.ERROR_BUSY) {
                    MLLog.i("对方正忙" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.BUSY);
                } else if (callError == CallError.REJECTED) {
                    MLLog.i("对方已拒绝" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.REJECTED);
                } else if (callError == CallError.ERROR_NORESPONSE) {
                    MLLog.i("对方未响应，可能手机不在身边" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.NORESPONSE);
                } else if (callError == CallError.ERROR_TRANSPORT) {
                    MLLog.i("连接建立失败" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.TRANSPORT);
                } else if (callError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED) {
                    MLLog.i("双方通讯协议不同" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.DIFFERENT);
                } else if (callError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
                    MLLog.i("双方通讯协议不同" + callError);
                    MLCallManager.getInstance().setEndType(MLCallManager.EndType.DIFFERENT);
                } else {
                    MLLog.i("通话已结束 %s", callError);
                    if (MLCallManager.getInstance().getEndType() == MLCallManager.EndType.CANCEL) {
                        MLCallManager.getInstance().setEndType(MLCallManager.EndType.CANCELLED);
                    }
                }
                // 通话结束，保存消息
                MLCallManager.getInstance().saveCallMessage();
                break;
            case NETWORK_UNSTABLE:
                if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                    MLLog.i("没有通话数据" + callError);
                    MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.NO_DATA);
                } else {
                    MLLog.i("网络不稳定" + callError);
                    MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.UNSTABLE);
                }
                break;
            case NETWORK_NORMAL:
                MLLog.i("网络正常");
                break;
            case VIDEO_PAUSE:
                MLLog.i("视频传输已暂停");
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.VIDEO_PAUSE);
                break;
            case VIDEO_RESUME:
                MLLog.i("视频传输已恢复");
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.NORMAL);
                break;
            case VOICE_PAUSE:
                MLLog.i("语音传输已暂停");
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.VOICE_PAUSE);
                break;
            case VOICE_RESUME:
                MLLog.i("语音传输已恢复");
                MLCallManager.getInstance().setCallStatus(MLCallManager.CallStatus.NORMAL);
                break;
            default:
                break;
        }
    }
}

package com.vmloft.develop.app.demo.call;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lzan13 on 2017/5/5.
 * 通话推送信息回掉接口，主要是用来实现当对方不在线时，发送一条消息，推送给对方，让对方上线后能继续收到呼叫
 */
public class CallPushProvider implements EMCallManager.EMCallPushProvider {
    @Override public void onRemoteOffline(String username) {
        EMMessage message = EMMessage.createTxtSendMessage("有人呼叫你，开启 APP 接听吧", username);
        if (CallManager.getInstance().getCallType() == CallManager.CallType.VIDEO) {
            message.setAttribute("attr_call_video", true);
        } else {
            message.setAttribute("attr_call_voice", true);
        }
        // 设置强制推送
        message.setAttribute("em_force_notification", "true");
        // 设置自定义推送提示
        JSONObject extObj = new JSONObject();
        try {
            extObj.put("em_push_title", "有人呼叫你，开启 APP 接听吧");
            extObj.put("extern", "定义推送扩展内容");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        message.setAttribute("em_apns_ext", extObj);
        message.setMessageStatusCallback(new EMCallBack() {
            @Override public void onSuccess() {

            }

            @Override public void onError(int i, String s) {

            }

            @Override public void onProgress(int i, String s) {

            }
        });
        EMClient.getInstance().chatManager().sendMessage(message);
    }
}

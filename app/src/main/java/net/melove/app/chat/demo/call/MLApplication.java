package net.melove.app.chat.demo.call;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;

/**
 * Created by lzan13 on 2016/5/25.
 */
public class MLApplication extends Application {

    private Context context;
    private CallReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        // 初始化环信sdk
        initEasemobSDK();
    }

    /**
     * 初始化环信sdk，并做一些注册监听的操作
     */
    private void initEasemobSDK() {
        // 良禽择木而栖
        EMChat.getInstance().setAppkey("easemob-demo#chatdemoui");

        // 初始化环信SDK,一定要先调用init()
        EMChat.getInstance().init(context);

        // 开启 debug 模式
        EMChat.getInstance().setDebugMode(true);


        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }

        //注册通话广播接收者
        context.registerReceiver(callReceiver, callFilter);
    }
}

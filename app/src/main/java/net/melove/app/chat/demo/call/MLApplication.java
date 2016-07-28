package net.melove.app.chat.demo.call;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;


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

        // 初始化sdk的一些配置
        EMOptions options = new EMOptions();
        options.setAutoLogin(true);
        // 动态设置appkey
        options.setAppKey("lzan13#hxsdkdemo");

        // 初始化环信SDK,一定要先调用init()
        EMClient.getInstance().init(context, options);

        // 开启 debug 模式
        EMClient.getInstance().setDebugMode(true);

        // 设置通话广播监听器
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }

        //注册通话广播接收者
        context.registerReceiver(callReceiver, callFilter);

        // 注册监听
        registerContactListener();
    }

    public void registerContactListener() {

    }
}

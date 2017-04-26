package com.vmloft.develop.app.demo.call;

import android.content.IntentFilter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.vmloft.develop.library.tools.VMApplication;

/**
 * Created by lzan13 on 2016/5/25.
 *
 * 程序入口，做一些必要的初始化操作
 */
public class AppApplication extends VMApplication {

    private CallReceiver callReceiver;

    @Override public void onCreate() {
        super.onCreate();

        // 初始化环信sdk
        initHyphenate();
    }

    /**
     * 初始化环信sdk，并做一些注册监听的操作
     */
    private void initHyphenate() {

        // 初始化sdk的一些配置
        EMOptions options = new EMOptions();
        options.setAutoLogin(true);
        // 动态设置appkey，如果清单配置文件设置了 appkey，这里可以不用设置
        //options.setAppKey("15697321312#mya2017");

        options.setSortMessageByServerTime(false);

        // 初始化环信SDK,一定要先调用init()
        EMClient.getInstance().init(context, options);

        // 开启 debug 模式
        EMClient.getInstance().setDebugMode(true);

        // 设置通话广播监听器
        IntentFilter callFilter = new IntentFilter(
                EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
        //注册通话广播接收者
        context.registerReceiver(callReceiver, callFilter);

        // 通话管理类的初始化
        CallManager.getInstance().init(context);
    }

}

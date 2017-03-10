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

    private static Context context;
    private MLCallReceiver callReceiver;

    @Override public void onCreate() {
        super.onCreate();
        context = this;

        // 初始化环信sdk
        initSDK();
    }

    /**
     * 初始化环信sdk，并做一些注册监听的操作
     */
    private void initSDK() {

        // 初始化sdk的一些配置
        EMOptions options = new EMOptions();
        options.setAutoLogin(true);
        // 动态设置appkey，如果清单配置文件设置了 appkey，这里可以不用设置
        //options.setAppKey("ziroom#ziroom");

        options.setSortMessageByServerTime(false);

        // 初始化环信SDK,一定要先调用init()
        EMClient.getInstance().init(context, options);

        // 开启 debug 模式
        EMClient.getInstance().setDebugMode(true);

        // 设置通话广播监听器
        IntentFilter callFilter = new IntentFilter(
                EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new MLCallReceiver();
        }
        //注册通话广播接收者
        context.registerReceiver(callReceiver, callFilter);

        // 通话管理类的初始化
        MLCallManager.getInstance().init(context);
    }

    public static Context getContext() {
        return context;
    }
}

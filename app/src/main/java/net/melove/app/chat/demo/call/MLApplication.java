package net.melove.app.chat.demo.call;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.hyphenate.EMGroupChangeListener;
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

        // 设置群组监听
        setGroupListener();
    }

    public void registerContactListener() {

    }

    public void setGroupListener(){
        EMGroupChangeListener groupChangeListener = new EMGroupChangeListener() {
            @Override
            public void onInvitationReceived(String s, String s1, String s2, String s3) {
                Log.i("lzan13", "onInvitationReceived");
            }

            @Override
            public void onApplicationReceived(String s, String s1, String s2, String s3) {
                Log.i("lzan13", "onApplicationReceived");

            }

            @Override
            public void onApplicationAccept(String s, String s1, String s2) {
                Log.i("lzan13", "onApplicationAccept");

            }

            @Override
            public void onApplicationDeclined(String s, String s1, String s2, String s3) {
                Log.i("lzan13", "onApplicationDeclined");

            }

            @Override
            public void onInvitationAccepted(String s, String s1, String s2) {
                Log.i("lzan13", "onInvitationAccepted");
            }


            @Override
            public void onInvitationDeclined(String s, String s1, String s2) {
                Log.i("lzan13", "onInvitationDeclined");

            }

            @Override
            public void onUserRemoved(String s, String s1) {
                Log.i("lzan13", "onUserRemoved");

            }

            @Override
            public void onGroupDestroyed(String s, String s1) {
                Log.i("lzan13", "onGroupDestroyed");
            }


            @Override
            public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
                Log.i("lzan13", "onAutoAcceptInvitationFromGroup");

            }
        };
        EMClient.getInstance().groupManager().addGroupChangeListener(groupChangeListener);
    }
}

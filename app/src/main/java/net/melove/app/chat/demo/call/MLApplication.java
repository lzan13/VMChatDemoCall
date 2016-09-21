package net.melove.app.chat.demo.call;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;

import java.util.Iterator;
import java.util.List;

/**
 * Created by lzan13 on 2016/5/25.
 */
public class MLApplication extends Application {

    private static String TAG = "lzan13";

    public static Context context;
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

        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        options.setAcceptInvitationAlways(false);


        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        Log.d(TAG, "process app name : " + processAppName);

        // if there is application has remote service, application:onCreate() maybe called twice
        // this check is to make sure SDK will initialized only once
        // return if process name is not application's name since the package name is the default process name
        if (processAppName == null || !processAppName.equalsIgnoreCase(getPackageName())) {
            Log.e(TAG, "enter the service process!");
            return;
        }

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

        // 注册联系人变化监听
        registerContactListener();

//        registerContactListener();
        EMChat.getInstance().setAppInited();
    }

    public void registerContactListener() {
        Log.i(TAG, "registerContactListener");
        EMContactManager.getInstance().setContactListener(new EMContactListener() {
            @Override
            public void onContactAdded(List<String> list) {
                Log.i(TAG, String.format("onContactAdded - user count %d, username %s", list.size(), list.get(0)));
            }

            @Override
            public void onContactDeleted(List<String> list) {
                Log.i(TAG, String.format("onContactDeleted - username %d, reason %s", list.size(), list.get(0)));
            }

            @Override
            public void onContactInvited(String s, String s1) {
                Log.i(TAG, String.format("onContactInvited - username %s, reason %s", s, s1));
            }

            @Override
            public void onContactAgreed(String s) {
                Log.i(TAG, String.format("onContactAgreed - username %s", s));
            }

            @Override
            public void onContactRefused(String s) {
                Log.i(TAG, String.format("onContactRefused - username %s", s));
            }
        });
    }


    /**
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     * @param pID
     * @return
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}

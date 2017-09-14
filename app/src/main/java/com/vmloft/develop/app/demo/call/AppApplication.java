package com.vmloft.develop.app.demo.call;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.conference.EMConferenceListener;
import com.hyphenate.chat.conference.EMConferenceStream;
import com.vmloft.develop.app.demo.call.conference.ConferenceActivity;
import com.vmloft.develop.library.tools.VMApplication;
import com.vmloft.develop.library.tools.utils.VMLog;
import java.util.Iterator;
import java.util.List;

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
        // 获取当前进程 id 并取得进程名
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        /**
         * 如果app启用了远程的service，此application:onCreate会被调用2次
         * 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
         * 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
         */
        if (processAppName == null || !processAppName.equalsIgnoreCase(context.getPackageName())) {
            // 则此application的onCreate 是被service 调用的，直接返回
            return;
        }

        // 初始化sdk的一些配置
        EMOptions options = new EMOptions();
        //options.enableDNSConfig(false);
        //options.setIMServer("118.193.28.212");
        //options.setImPort(31097);
        //options.setRestServer("118.193.28.212:31080");
        //options.setAppKey("easemob-demo#chatuidemo");

        options.setAutoLogin(true);
        options.setGCMNumber("127088616464");
        // 设置小米推送 appID 和 appKey
        options.setMipushConfig("2882303761517573806", "5981757315806");

        // 设置消息是否按照服务器时间排序
        options.setSortMessageByServerTime(false);

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

        // 通话管理类的初始化
        CallManager.getInstance().init(context);

        EMClient.getInstance().conferenceManager().addConferenceListener(new EMConferenceListener() {
            @Override public void onMemberJoined(String username) {
                VMLog.i("Joined username: %s", username);
            }

            @Override public void onMemberExited(String username) {
                VMLog.i("Exited username: %s", username);
            }

            @Override public void onStreamAdded(EMConferenceStream stream) {
                VMLog.i("Stream added streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b",
                        stream.getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(),
                        stream.getExtension(), stream.isVideoOff(), stream.isAudioOff());
                VMLog.i("Conference stream subscribable: %d, subscribed: %d",
                        EMClient.getInstance().conferenceManager().getSubscribableStreamMap().size(),
                        EMClient.getInstance().conferenceManager().getSubscribedStreamMap().size());
            }

            @Override public void onStreamRemoved(EMConferenceStream stream) {
                VMLog.i("Stream removed streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b",
                        stream.getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(),
                        stream.getExtension(), stream.isVideoOff(), stream.isAudioOff());
                VMLog.i("Conference stream subscribable: %d, subscribed: %d",
                        EMClient.getInstance().conferenceManager().getSubscribableStreamMap().size(),
                        EMClient.getInstance().conferenceManager().getSubscribedStreamMap().size());
            }

            @Override public void onStreamUpdate(EMConferenceStream stream) {
                VMLog.i("Stream added streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b",
                        stream.getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(),
                        stream.getExtension(), stream.isVideoOff(), stream.isAudioOff());
                VMLog.i("Conference stream subscribable: %d, subscribed: %d",
                        EMClient.getInstance().conferenceManager().getSubscribableStreamMap().size(),
                        EMClient.getInstance().conferenceManager().getSubscribedStreamMap().size());
            }

            @Override public void onPassiveLeave(int error, String message) {
                VMLog.i("passive leave code: %d, message: %s", error, message);
            }

            @Override public void onState(ConferenceState state, String confId, Object object) {
                VMLog.i("State code=%d, confId=%s, object=" + object, state.ordinal(), confId);
            }

            @Override public void onReceiveInvite(String confId, String password, String extension) {
                VMLog.i("Receive conference invite confId: %s, password: %s, extension: %s", confId, password, extension);

                Intent conferenceIntent = new Intent(context, ConferenceActivity.class);
                conferenceIntent.putExtra("isCreator", false);
                conferenceIntent.putExtra("confId", confId);
                conferenceIntent.putExtra("password", password);
                conferenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(conferenceIntent);
            }
        });
    }

    /**
     * 根据Pid获取当前进程的名字，一般就是当前app的包名
     *
     * @param pid 进程的id
     * @return 返回进程的名字
     */
    private String getAppName(int pid) {
        String processName = null;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List list = activityManager.getRunningAppProcesses();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pid) {
                    // 根据进程的信息获取当前进程的名字
                    processName = info.processName;
                    // 返回当前进程名
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 没有匹配的项，返回为null
        return processName;
    }
}

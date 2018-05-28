package com.vmloft.develop.app.demo.call;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMStreamStatistics;
import com.vmloft.develop.app.demo.call.conference.ConferenceActivity;
import com.vmloft.develop.app.demo.call.single.CallManager;
import com.vmloft.develop.app.demo.call.single.CallReceiver;
import com.vmloft.develop.library.tools.VMApp;
import com.vmloft.develop.library.tools.VMTools;
import com.vmloft.develop.library.tools.utils.VMLog;

import java.util.Iterator;
import java.util.List;

/**
 * Created by lzan13 on 2016/5/25.
 * <p>
 * 程序入口，做一些必要的初始化操作
 */
public class App extends VMApp {

    private CallReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        VMTools.init(context);
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
        //        options.enableDNSConfig(false);
        //        options.setIMServer("im1.easemob.cm");
        //        options.setImPort(443);
        //        options.setRestServer("a1.easemob.com:80");
        //        options.setAppKey("easemob-demo#chatdemoui");
        //        options.setAppKey("easemob-demo#chatuidemo");
        //        options.setAppKey("hx-ps#api4vip6");
        //        options.setAppKey("cx-dev#cxstudy");

        options.setAutoLogin(true);
        // 设置小米推送 appID 和 appKey
        //        options.setMipushConfig("2882303761517573806", "5981757315806");

        // 设置消息是否按照服务器时间排序
        options.setSortMessageByServerTime(false);

        // 初始化环信SDK,一定要先调用init()
        EMClient.getInstance().init(context, options);

        // 开启 debug 模式
        EMClient.getInstance().setDebugMode(true);

        // 设置通话广播监听器
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance()
            .callManager()
            .getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
        //注册通话广播接收者
        context.registerReceiver(callReceiver, callFilter);

        CallManager.getInstance().setExternalInputData(false);
        // 通话管理类的初始化
        CallManager.getInstance().init(context);

        setConnectionListener();

        setConferenceListener();

        setMessageListener();
    }

    /**
     * 设置连接监听
     */
    private void setConnectionListener() {
        EMConnectionListener connectionListener = new EMConnectionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected(int i) {
                String str = "" + i;
                switch (i) {
                case EMError.USER_REMOVED:
                    str = "账户被移除";
                    break;
                case EMError.USER_LOGIN_ANOTHER_DEVICE:
                    str = "其他设备登录";
                    break;
                case EMError.USER_KICKED_BY_OTHER_DEVICE:
                    str = "其他设备强制下线";
                    break;
                case EMError.USER_KICKED_BY_CHANGE_PASSWORD:
                    str = "密码修改";
                    break;
                case EMError.SERVER_SERVICE_RESTRICTED:
                    str = "被后台限制";
                    break;
                }
                VMLog.i("onDisconnected %s", str);
            }
        };
        EMClient.getInstance().addConnectionListener(connectionListener);
    }

    /**
     * 设置多人会议监听
     */
    private void setConferenceListener() {
        EMClient.getInstance()
            .conferenceManager()
            .addConferenceListener(new EMConferenceListener() {
                @Override
                public void onMemberJoined(String username) {
                    VMLog.i("Joined username: %s", username);
                }

                @Override
                public void onMemberExited(String username) {
                    VMLog.i("Exited username: %s", username);
                }

                @Override
                public void onStreamAdded(EMConferenceStream stream) {
                    VMLog.i("Stream added streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b", stream
                        .getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(), stream
                        .getExtension(), stream.isVideoOff(), stream.isAudioOff());
                    VMLog.i("Conference stream subscribable: %d, subscribed: %d", EMClient.getInstance()
                        .conferenceManager()
                        .getAvailableStreamMap()
                        .size(), EMClient.getInstance()
                        .conferenceManager()
                        .getSubscribedStreamMap()
                        .size());
                }

                @Override
                public void onStreamRemoved(EMConferenceStream stream) {
                    VMLog.i("Stream removed streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b", stream
                        .getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(), stream
                        .getExtension(), stream.isVideoOff(), stream.isAudioOff());
                    VMLog.i("Conference stream subscribable: %d, subscribed: %d", EMClient.getInstance()
                        .conferenceManager()
                        .getAvailableStreamMap()
                        .size(), EMClient.getInstance()
                        .conferenceManager()
                        .getSubscribedStreamMap()
                        .size());
                }

                @Override
                public void onStreamUpdate(EMConferenceStream stream) {
                    VMLog.i("Stream added streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b", stream
                        .getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(), stream
                        .getExtension(), stream.isVideoOff(), stream.isAudioOff());
                    VMLog.i("Conference stream subscribable: %d, subscribed: %d", EMClient.getInstance()
                        .conferenceManager()
                        .getAvailableStreamMap()
                        .size(), EMClient.getInstance()
                        .conferenceManager()
                        .getSubscribedStreamMap()
                        .size());
                }

                @Override
                public void onPassiveLeave(int error, String message) {
                    VMLog.i("passive leave code: %d, message: %s", error, message);
                }

                @Override
                public void onConferenceState(ConferenceState state) {
                    VMLog.i("State " + state);
                }

                @Override
                public void onStreamStatistics(EMStreamStatistics emStreamStatistics) {
                    VMLog.i(emStreamStatistics.toString());
                }

                @Override
                public void onStreamSetup(String streamId) {
                    VMLog.i("Stream id %s", streamId);
                }

                @Override
                public void onSpeakers(List<String> list) {

                }

                @Override
                public void onReceiveInvite(String confId, String password, String extension) {
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

    private void setMessageListener() {
        EMClient.getInstance().chatManager().addMessageListener(new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> list) {
                Message msg = handler.obtainMessage(0);
                msg.obj = list.get(0).toString();
                handler.sendMessage(msg);
                for (EMMessage message : list) {
                    VMLog.i("收到新消息" + message);
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> list) {

            }

            @Override
            public void onMessageRead(List<EMMessage> list) {

            }

            @Override
            public void onMessageDelivered(List<EMMessage> list) {

            }

            @Override
            public void onMessageRecalled(List<EMMessage> list) {

            }

            @Override
            public void onMessageChanged(EMMessage emMessage, Object o) {

            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            switch (msg.what) {
            case 0:
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

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

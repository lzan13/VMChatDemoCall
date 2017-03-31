package com.vmloft.develop.app.demo.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import android.support.v7.app.NotificationCompat;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;

import com.vmloft.develop.library.tools.utils.VMLog;
import java.util.Timer;
import java.util.TimerTask;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lzan13 on 2017/2/8.
 *
 * 实时音视频通话管理类，这是一个单例类，用来管理 app 通话操作
 */
public class VMCallManager {

    // 上下文菜单
    private Context context;

    // 单例类实例
    private static VMCallManager instance;

    // 通知栏提醒管理类
    private NotificationManager notificationManager;
    private int callNotificationId = 0526;

    // 音频管理器
    private AudioManager audioManager;
    // 音频池
    private SoundPool soundPool;
    // 声音资源 id
    private int streamID;
    private int loadId;
    private boolean isLoaded = false;

    // 通话状态监听
    private VMCallStateListener callStateListener;

    // 记录通话方向，是呼出还是呼入
    private boolean isInComingCall = true;
    // 设备相关开关
    private boolean isOpenCamera = true;
    private boolean isOpenMic = true;
    private boolean isOpenSpeaker = true;
    private boolean isOpenRecord = false;

    // 计时器
    private Timer timer;
    // 通话时间
    private int callTime = 0;

    // 当前通话对象 id
    private String chatId;
    private CallState callState = CallState.DISCONNECTED;
    private CallType callType = CallType.VIDEO;
    private EndType endType = EndType.CANCEL;

    /**
     * 私有化构造函数
     */
    private VMCallManager() {
    }

    /**
     * 获取单例对象实例方法
     */
    public static VMCallManager getInstance() {
        if (instance == null) {
            instance = new VMCallManager();
        }
        return instance;
    }

    /**
     * 通话管理类的初始化
     */
    public void init(Context context) {
        this.context = context;

        // 初始化音频池
        initSoundPool();
        // 音频管理器
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        /**
         * SDK 3.2.x 版本后通话相关设置，一定要在初始化后，开始音视频功能前设置，否则设置无效
         */
        // 设置自动调节分辨率，默认为 true
        EMClient.getInstance().callManager().getCallOptions().enableFixedVideoResolution(true);
        // 设置视频通话最大和最小比特率，可以不用设置，比特率会根据分辨率进行计算，默认最大(800)， 默认最小(80)
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoKbps(2500);
        EMClient.getInstance().callManager().getCallOptions().setMinVideoKbps(150);
        // 设置视频通话分辨率 默认是(640, 480)
        EMClient.getInstance().callManager().getCallOptions().setVideoResolution(1280, 720);
        // 设置通话最大帧率，SDK 最大支持(30)，默认(20)
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoFrameRate(30);
        // 设置通话过程中对方如果离线是否发送离线推送通知
        EMClient.getInstance().callManager().getCallOptions().setIsSendPushIfOffline(false);
        // 设置音视频通话采样率，一般不需要设置，除非采集声音有问题才需要手动设置
        EMClient.getInstance().callManager().getCallOptions().setAudioSampleRate(48000);
        // 设置录制视频采用 mov 编码 TODO 后期这个而接口需要移动到 EMCallOptions 中
        EMClient.getInstance().callManager().getVideoCallHelper().setPreferMovFormatEnable(true);
    }

    /**
     * 通话结束，保存一条记录通话的消息
     */
    public void saveCallMessage() {
        VMLog.d("The call ends and the call log message is saved! " + endType);
        EMMessage message = null;
        EMTextMessageBody body = null;
        String content = null;
        if (isInComingCall) {
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            message.setFrom(chatId);
        } else {
            message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            message.setTo(chatId);
        }
        switch (endType) {
            case NORMAL: // 正常结束通话
                content = String.valueOf(getCallTime());
                break;
            case CANCEL: // 取消
                content = context.getString(R.string.call_cancel);
                break;
            case CANCELLED: // 被取消
                content = context.getString(R.string.call_cancel_is_incoming);
                break;
            case BUSY: // 对方忙碌
                content = context.getString(R.string.call_busy);
                break;
            case OFFLINE: // 对方不在线
                content = context.getString(R.string.call_offline);
                break;
            case REJECT: // 拒绝的
                content = context.getString(R.string.call_reject_is_incoming);
                break;
            case REJECTED: // 被拒绝的
                content = context.getString(R.string.call_reject);
                break;
            case NORESPONSE: // 未响应
                content = context.getString(R.string.call_no_response);
                break;
            case TRANSPORT: // 建立连接失败
                content = context.getString(R.string.call_connection_fail);
                break;
            case DIFFERENT: // 通讯协议不同
                content = context.getString(R.string.call_offline);
                break;
            default:
                // 默认取消
                content = context.getString(R.string.call_cancel);
                break;
        }
        body = new EMTextMessageBody(content);
        message.addBody(body);
        message.setStatus(EMMessage.Status.SUCCESS);
        if (callType == CallType.VIDEO) {
            message.setAttribute("attr_call_video", true);
        } else {
            message.setAttribute("attr_call_voice", true);
        }
        message.setUnread(false);
        // 调用sdk的保存消息方法
        EMClient.getInstance().chatManager().saveMessage(message);
    }

    /**
     * 设置通话图像回调处理器
     */
    public void setCallCameraDataProcessor() {
        // 初始化视频数据处理器
        VMCameraDataProcessor cameraDataProcessor = new VMCameraDataProcessor();
        // 设置视频通话数据处理类
        EMClient.getInstance().callManager().setCameraDataProcessor(cameraDataProcessor);
    }

    /**
     * 开始呼叫对方
     */
    public void makeCall() {
        try {
            if (callType == CallType.VIDEO) {
                EMClient.getInstance().callManager().makeVideoCall(chatId);
            } else {
                EMClient.getInstance().callManager().makeVoiceCall(chatId);
            }
            setEndType(EndType.CANCEL);
        } catch (EMServiceNotReadyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拒绝通话
     */
    public void rejectCall() {
        try {
            // 调用 SDK 的拒绝通话方法
            EMClient.getInstance().callManager().rejectCall();
            // 设置结束原因为拒绝
            setEndType(EndType.REJECT);
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
        // 保存一条通话消息
        saveCallMessage();
        // 通话结束，重置通话状态
        reset();
    }

    /**
     * 结束通话
     */
    public void endCall() {
        try {
            // 调用 SDK 的结束通话方法
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            VMLog.e("结束通话失败：error %d - %s", e.getErrorCode(), e.getMessage());
        }
        // 挂断电话调用保存消息方法
        saveCallMessage();
        // 通话结束，重置通话状态
        reset();
    }

    /**
     * 接听通话
     */
    public boolean answerCall() {
        // 接听通话后关闭通知铃音
        stopCallSound();
        // 调用接通通话方法
        try {
            EMClient.getInstance().callManager().answerCall();
            return true;
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 打开扬声器
     * 主要是通过扬声器的开关以及设置音频播放模式来实现
     * 1、MODE_NORMAL：是正常模式，一般用于外放音频
     * 2、MODE_IN_CALL：
     * 3、MODE_IN_COMMUNICATION：这个和 CALL 都表示通讯模式，不过 CALL 在华为上不好使，故使用 COMMUNICATION
     * 4、MODE_RINGTONE：铃声模式
     */
    public void openSpeaker() {
        // 检查是否已经开启扬声器
        if (!audioManager.isSpeakerphoneOn()) {
            // 打开扬声器
            audioManager.setSpeakerphoneOn(true);
        }
        // 设置声音模式为正常模式
        audioManager.setMode(AudioManager.MODE_NORMAL);
        setOpenSpeaker(true);
    }

    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    public void closeSpeaker() {
        // 检查是否已经开启扬声器
        if (audioManager.isSpeakerphoneOn()) {
            // 关闭扬声器
            audioManager.setSpeakerphoneOn(false);
        }
        // 设置声音模式为通讯模式，即使用听筒播放
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    /**
     * 注册通话状态监听，监听音视频通话状态
     * 状态监听详细实现在 {@link VMCallStateListener} 类中
     */
    public void registerCallStateListener() {
        if (callStateListener == null) {
            callStateListener = new VMCallStateListener();
        }
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }

    /**
     * 删除通话状态监听
     */
    private void unregisterCallStateListener() {
        if (callStateListener != null) {
            EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
            callStateListener = null;
        }
    }

    /**
     * ----------------------------- Sound start -----------------------------
     * 初始化 SoundPool
     */
    private void initSoundPool() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    // 设置音频要用在什么地方，这里选择电话通知铃音
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            // 当系统的 SDK 版本高于21时，使用 build 的方式实例化 SoundPool
            soundPool =
                    new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();
        } else {
            // 老版本使用构造函数方式实例化 SoundPool，MODE 设置为铃音 MODE_RINGTONE
            soundPool = new SoundPool(1, AudioManager.MODE_RINGTONE, 0);
        }
    }

    /**
     * 加载音效资源
     */
    private void loadSound() {
        if (isInComingCall) {
            loadId = soundPool.load(context, R.raw.sound_call_incoming, 1);
        } else {
            loadId = soundPool.load(context, R.raw.sound_calling, 1);
        }
    }

    /**
     * 尝试播放呼叫通话提示音
     */
    public void attemptPlayCallSound() {
        // 检查音频资源是否已经加载完毕
        if (isLoaded) {
            playCallSound();
        } else {
            // 播放之前先去加载音效
            loadSound();
            // 设置资源加载监听，也因为加载资源在单独的进程，需要时间，所以等监听到加载完成才能播放
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    VMLog.d("SoundPool load complete! loadId: %d", loadId);
                    isLoaded = true;
                    // 首次监听到加载完毕，开始播放音频
                    playCallSound();
                }
            });
        }
    }

    /**
     * 播放音频
     */
    private void playCallSound() {
        // 打开扬声器
        openSpeaker();
        // 设置音频管理器音频模式为铃音模式
        audioManager.setMode(AudioManager.MODE_RINGTONE);
        // 播放提示音，返回一个播放的音频id，等下停止播放需要用到
        if (soundPool != null) {
            streamID = soundPool.play(loadId, // 播放资源id；就是加载到SoundPool里的音频资源顺序
                    0.5f,   // 左声道音量
                    0.5f,   // 右声道音量
                    1,      // 优先级，数值越高，优先级越大
                    -1,     // 是否循环；0 不循环，-1 循环，N 表示循环次数
                    1);     // 播放速率；从0.5-2，一般设置为1，表示正常播放
        }
    }

    /**
     * 关闭音效的播放，并释放资源
     */
    protected void stopCallSound() {
        if (soundPool != null) {
            // 停止播放音效
            soundPool.stop(streamID);
            // 卸载音效
            //soundPool.unload(loadId);
            // 释放资源
            //soundPool.release();
        }
    }// --------------------------------- Sound end ---------------------------------

    /**
     * 添加通话悬浮窗并发送通知栏提醒
     */
    public void addFloatWindow() {
        // 发送通知栏提醒
        addCallNotification();
        // 开启悬浮窗
        VMFloatWindow.getInstance(context).addFloatWindow();
    }

    /**
     * 移除通话悬浮窗和通知栏提醒
     */
    public void removeFloatWindow() {
        // 取消通知栏提醒
        cancelCallNotification();
        // 关闭悬浮窗
        VMFloatWindow.getInstance(context).removeFloatWindow();
    }

    /**
     * 发送通知栏提醒，告知用户通话继续进行中
     */
    private void addCallNotification() {
        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        builder.setContentText("通话进行中，点击恢复");

        builder.setContentTitle(context.getString(R.string.app_name));
        Intent intent = new Intent();
        if (callType == CallType.VIDEO) {
            intent.setClass(context, VMVideoCallActivity.class);
        } else {
            intent.setClass(context, VMVoiceCallActivity.class);
        }
        PendingIntent pIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        builder.setOngoing(true);

        builder.setWhen(System.currentTimeMillis());

        notificationManager.notify(callNotificationId, builder.build());
    }

    /**
     * 取消通话状态通知栏提醒
     */
    public void cancelCallNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(callNotificationId);
        }
    }

    /**
     * 开始通话计时，这里在全局管理器中开启一个定时器进行计时，可以做到最小化，以及后台时进行计时
     */
    public void startCallTime() {
        final VMCallEvent event = new VMCallEvent();
        EventBus.getDefault().post(event);
        event.setTime(true);
        if (timer == null) {
            timer = new Timer();
        }
        timer.purge();
        TimerTask task = new TimerTask() {
            @Override public void run() {
                callTime++;
                EventBus.getDefault().post(event);
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    /**
     * 停止计时
     */
    public void stopCallTime() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        callTime = 0;
    }

    /**
     * 释放资源
     */
    public void reset() {
        isOpenCamera = true;
        isOpenMic = true;
        isOpenSpeaker = true;
        isOpenRecord = false;
        // 设置通话状态为已断开
        setCallState(CallState.DISCONNECTED);
        // 停止计时
        stopCallTime();
        // 取消注册通话状态的监听
        unregisterCallStateListener();
        // 释放音频资源
        if (soundPool != null) {
            // 停止播放音效
            soundPool.stop(streamID);
        }
        // 重置音频管理器
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    /**
     * 相关的 get 以及 set 方法
     */
    public CallState getCallState() {
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isInComingCall() {
        return isInComingCall;
    }

    public void setInComingCall(boolean isInComingCall) {
        this.isInComingCall = isInComingCall;
    }

    public int getCallTime() {
        return callTime;
    }

    public void setEndType(EndType endType) {
        this.endType = endType;
    }

    public EndType getEndType() {
        return endType;
    }

    public boolean isOpenCamera() {
        return isOpenCamera;
    }

    public void setOpenCamera(boolean openCamera) {
        isOpenCamera = openCamera;
    }

    public boolean isOpenMic() {
        return isOpenMic;
    }

    public void setOpenMic(boolean openMic) {
        isOpenMic = openMic;
    }

    public boolean isOpenSpeaker() {
        return isOpenSpeaker;
    }

    public void setOpenSpeaker(boolean openSpeaker) {
        isOpenSpeaker = openSpeaker;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }

    public void setOpenRecord(boolean openRecord) {
        isOpenRecord = openRecord;
    }

    /**
     * 通话类型
     */
    public enum CallType {
        VIDEO,  // 视频通话
        VOICE   // 音频通话
    }

    /**
     * 通话状态枚举值
     */
    public enum CallState {
        CONNECTING,     // 连接中
        CONNECTED,      // 连接成功，等待接受
        ACCEPTED,       // 通话中
        DISCONNECTED    // 通话中断

    }

    /**
     * 通话结束状态类型
     */
    public enum EndType {
        NORMAL,     // 正常结束通话
        CANCEL,     // 取消
        CANCELLED,  // 被取消
        BUSY,       // 对方忙碌
        OFFLINE,    // 对方不在线
        REJECT,     // 拒绝的
        REJECTED,   // 被拒绝的
        NORESPONSE, // 未响应
        TRANSPORT,  // 建立连接失败
        DIFFERENT   // 通讯协议不同
    }
}

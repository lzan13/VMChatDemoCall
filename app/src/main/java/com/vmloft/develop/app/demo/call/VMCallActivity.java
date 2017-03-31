package com.vmloft.develop.app.demo.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.view.WindowManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.vmloft.develop.library.tools.VMBaseActivity;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lzan13 on 2016/8/8.
 *
 * 通话界面的父类，做一些音视频通话的通用操作
 */
public class VMCallActivity extends VMBaseActivity {

    // 呼叫方名字
    protected String chatId;

    // 通知栏提醒管理类
    protected NotificationManager notificationManager;
    protected int callNotificationId = 0526;

    // 震动器
    private Vibrator vibrator;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置通话界面属性，保持屏幕常亮，关闭输入法，以及解锁
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    /**
     * 初始化界面方法，做一些界面的初始化操作
     */
    protected void initView() {
        activity = this;

        // 初始化振动器
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        if (VMCallManager.getInstance().getCallState() == VMCallManager.CallState.DISCONNECTED) {
            // 收到呼叫或者呼叫对方时初始化通话状态监听
            VMCallManager.getInstance().setCallState(VMCallManager.CallState.CONNECTING);
            VMCallManager.getInstance().registerCallStateListener();
            VMCallManager.getInstance().attemptPlayCallSound();

            // 如果不是对方打来的，就主动呼叫
            if (!VMCallManager.getInstance().isInComingCall()) {
                VMCallManager.getInstance().makeCall();
            }
        } else {
            VMCallManager.getInstance().removeFloatWindow();
        }
    }

    /**
     * 挂断通话
     */
    protected void endCall() {
        VMCallManager.getInstance().endCall();
        onFinish();
    }

    /**
     * 拒绝通话
     */
    protected void rejectCall() {
        VMCallManager.getInstance().rejectCall();
        onFinish();
    }

    /**
     * 接听通话
     */
    protected void answerCall() {
        VMCallManager.getInstance().answerCall();
    }

    /**
     * 调用系统振动，触发按钮的震动反馈
     */
    protected void vibrate() {
        vibrator.vibrate(88);
    }

    /**
     * 销毁界面时做一些自己的操作
     */
    @Override protected void onFinish() {
        super.onFinish();
    }

    /**
     * 重载返回键
     */
    @Override public void onBackPressed() {
        // super.onBackPressed();

    }

    @Override protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     *
     */
    @Override protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override protected void onResume() {
        super.onResume();
        // 判断当前通话状态，如果已经挂断，则关闭通话界面
        if (VMCallManager.getInstance().getCallState() == VMCallManager.CallState.DISCONNECTED) {
            onFinish();
            return;
        }
        // 取消通知栏提醒
        if (notificationManager != null) {
            notificationManager.cancel(callNotificationId);
        }
    }

    /**
     * 发送通知栏提醒
     */
    private void sendCallNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        builder.setContentText("通话进行中，点击恢复");

        builder.setContentTitle(getString(R.string.app_name));
        Intent intent = new Intent(activity, activity.getClass());
        PendingIntent pIntent =
                PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        builder.setOngoing(true);

        builder.setWhen(System.currentTimeMillis());

        notificationManager.notify(callNotificationId, builder.build());
    }
}

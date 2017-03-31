package com.vmloft.develop.app.demo.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;
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

    @Override protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 通话界面拦截 Back 按键，不能返回
     */
    @Override public void onBackPressed() {
        //super.onBackPressed();
        VMCallManager.getInstance().addFloatWindow();
        onFinish();
    }

    /**
     * 监听通话界面是否隐藏，处理悬浮窗
     */
    @Override protected void onUserLeaveHint() {
        //super.onUserLeaveHint();
        VMCallManager.getInstance().addFloatWindow();
        onFinish();
    }

    @Override protected void onResume() {
        super.onResume();
        // 判断当前通话状态，如果已经挂断，则关闭通话界面
        if (VMCallManager.getInstance().getCallState() == VMCallManager.CallState.DISCONNECTED) {
            onFinish();
            return;
        } else {
            VMCallManager.getInstance().removeFloatWindow();
        }
    }
}

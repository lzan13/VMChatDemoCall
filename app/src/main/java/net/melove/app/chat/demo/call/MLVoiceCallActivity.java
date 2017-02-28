package net.melove.app.chat.demo.call;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import net.melove.app.chat.demo.call.utils.MLLog;

public class MLVoiceCallActivity extends MLCallActivity {

    // 通话状态监听回调接口
    private EMCallStateChangeListener callStateChangeListener;

    // 使用 ButterKnife 注解的方式获取控件
    @BindView(R.id.img_call_background) ImageView backgroundView;
    @BindView(R.id.text_call_status) TextView callStatusView;
    @BindView(R.id.img_call_avatar) ImageView avatarView;
    @BindView(R.id.text_call_username) TextView usernameView;
    @BindView(R.id.btn_exit_full_screen) ImageButton exitFullScreenBtn;
    @BindView(R.id.btn_mic_switch) ImageButton micSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton speakerSwitch;
    @BindView(R.id.btn_record_switch) ImageButton recordSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton rejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton endCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton answerCallFab;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 重载父类方法,实现一些当前通话的操作，
     */
    @Override protected void initView() {
        super.initView();
        if (MLCallManager.getInstance().isInComingCall()) {
            endCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.VISIBLE);
        } else {
            endCallFab.setVisibility(View.VISIBLE);
            rejectCallFab.setVisibility(View.GONE);
            answerCallFab.setVisibility(View.GONE);
        }
    }

    /**
     * 界面控件点击监听器
     */
    @OnClick({
            R.id.btn_exit_full_screen, R.id.btn_mic_switch, R.id.btn_speaker_switch,
            R.id.btn_record_switch, R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit_full_screen:
                // 最小化通话界面
                exitFullScreen();
                break;
            case R.id.btn_mic_switch:
                // 麦克风开关
                onMicrophone();
                break;
            case R.id.btn_speaker_switch:
                // 扬声器开关
                onSpeaker();
                break;
            case R.id.btn_record_switch:
                // 录制开关
                recordCall();
                break;
            case R.id.fab_end_call:
                // 结束通话
                endCall();
                break;
            case R.id.fab_reject_call:
                // 拒绝接听通话
                rejectCall();
                break;
            case R.id.fab_answer_call:
                // 接听通话
                answerCall();
                break;
        }
    }

    /**
     * 接听通话
     */
    @Override protected void answerCall() {
        super.answerCall();

        endCallFab.setVisibility(View.VISIBLE);
        rejectCallFab.setVisibility(View.GONE);
        answerCallFab.setVisibility(View.GONE);
    }

    /**
     * 退出全屏通话界面
     */
    private void exitFullScreen() {
        // 振动反馈
        vibrate();
        // 让应用回到桌面
        //        activity.moveTaskToBack(true);
        activity.finish();
    }

    /**
     * 麦克风开关，主要调用环信语音数据传输方法
     */
    private void onMicrophone() {
        // 振动反馈
        vibrate();
        try {
            // 根据麦克风开关是否被激活来进行判断麦克风状态，然后进行下一步操作
            if (micSwitch.isActivated()) {
                // 暂停语音数据的传输
                EMClient.getInstance().callManager().pauseVoiceTransfer();
                // 设置按钮状态
                micSwitch.setActivated(false);
            } else {
                // 恢复语音数据的传输
                EMClient.getInstance().callManager().resumeVoiceTransfer();
                // 设置按钮状态
                micSwitch.setActivated(true);
            }
        } catch (HyphenateException e) {
            MLLog.e("exception code: %d, %s", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 扬声器开关
     */
    private void onSpeaker() {
        // 振动反馈
        vibrate();
        // 根据按钮状态决定打开还是关闭扬声器
        if (speakerSwitch.isActivated()) {
            speakerSwitch.setActivated(false);
            MLCallManager.getInstance().closeSpeaker();
        } else {
            speakerSwitch.setActivated(true);
            MLCallManager.getInstance().openSpeaker();
        }
    }

    /**
     * 录制通话内容 TODO 后期实现
     */
    private void recordCall() {
        // 振动反馈
        vibrate();
        // 根据开关状态决定是否开启录制
        if (recordSwitch.isActivated()) {
            // 设置按钮状态
            recordSwitch.setActivated(false);
        } else {
            // 设置按钮状态
            recordSwitch.setActivated(true);
        }
    }

    /**
     * 添加当前界面的通话状态监听
     */
    private void addCallStateListener() {
        callStateChangeListener = new EMCallStateChangeListener() {
            @Override public void onCallStateChanged(EMCallStateChangeListener.CallState callState,
                    EMCallStateChangeListener.CallError callError) {
                switch (callState) {
                    case CONNECTING: // 正在呼叫对方
                        break;
                    case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
                        break;
                    case ACCEPTED: // 通话已接通
                        break;
                    case DISCONNECTED: // 通话已中断
                        onFinish();
                        break;
                    case NETWORK_UNSTABLE:
                        if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                            MLLog.i("没有通话数据" + callError);
                            MLCallManager.getInstance()
                                    .setCallStatus(MLCallManager.CallStatus.NO_DATA);
                        } else {
                            MLLog.i("网络不稳定" + callError);
                            MLCallManager.getInstance()
                                    .setCallStatus(MLCallManager.CallStatus.UNSTABLE);
                        }
                        break;
                    case NETWORK_NORMAL:
                        break;
                    case VIDEO_PAUSE:
                        break;
                    case VIDEO_RESUME:
                        break;
                    case VOICE_PAUSE:
                        break;
                    case VOICE_RESUME:
                        break;
                    default:
                        break;
                }
            }
        };
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateChangeListener);
    }

    /**
     * 移除当前界面的通话状态监听
     */
    private void removeCallStateListener() {
        EMClient.getInstance().callManager().removeCallStateChangeListener(callStateChangeListener);
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override protected void onStart() {
        super.onStart();
        addCallStateListener();
    }

    @Override protected void onStop() {
        super.onStop();
        removeCallStateListener();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 屏幕方向改变回调方法
     */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

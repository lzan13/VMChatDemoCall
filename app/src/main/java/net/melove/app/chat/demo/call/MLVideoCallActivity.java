package net.melove.app.chat.demo.call;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;
import com.superrtc.sdk.VideoView;
import java.io.File;
import net.melove.app.chat.demo.call.utils.MLLog;

public class MLVideoCallActivity extends MLCallActivity {

    // 通话状态监听回调接口
    private EMCallStateChangeListener callStateChangeListener;

    // 视频通话帮助类
    private EMCallManager.EMVideoCallHelper videoCallHelper;
    // 摄像头数据处理器
    private MLCameraDataProcessor cameraDataProcessor = null;

    // 当前显示画面状态，0 正常，1 本地为大图
    private int surfaceViewState = 1;

    // 使用 ButterKnife 注解的方式获取控件
    @BindView(R.id.layout_call_control) View controlLayout;
    @BindView(R.id.surface_view_local) EMLocalSurfaceView localSurfaceView;
    @BindView(R.id.surface_view_opposite) EMOppositeSurfaceView oppositeSurfaceView;

    @BindView(R.id.img_call_background) ImageView backgroundView;
    @BindView(R.id.text_call_status) TextView callStatusView;
    @BindView(R.id.btn_exit_full_screen) ImageButton exitFullScreenBtn;
    @BindView(R.id.btn_change_camera_switch) ImageButton changeCameraSwitch;
    @BindView(R.id.btn_camera_switch) ImageButton cameraSwitch;
    @BindView(R.id.btn_mic_switch) ImageButton micSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton speakerSwitch;
    @BindView(R.id.btn_record_switch) ImageButton recordSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton rejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton endCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton answerCallFab;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

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
            answerCallFab.setVisibility(View.VISIBLE);
            rejectCallFab.setVisibility(View.VISIBLE);
        } else {
            endCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.GONE);
        }

        // 初始化视频通话帮助类
        videoCallHelper = EMClient.getInstance().callManager().getVideoCallHelper();
        // 设置本地预览图像显示在最上层，一定要提前设置，否则无效
        //localSurfaceView.setZOrderMediaOverlay(true);
        //localSurfaceView.setZOrderOnTop(true);

        localSurfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        oppositeSurfaceView.setScaleMode(
                VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);

        try {
            // 设置默认摄像头为前置
            EMClient.getInstance()
                    .callManager()
                    .setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }

        // 设置本地以及对方显示画面控件，这个要设置在上边几个方法之后，不然会概率出现接收方无画面
        EMClient.getInstance().callManager().setSurfaceView(localSurfaceView, oppositeSurfaceView);
        // 初始化视频数据处理器
        cameraDataProcessor = new MLCameraDataProcessor();
        // 设置视频通话数据处理类
        EMClient.getInstance().callManager().setCameraDataProcessor(cameraDataProcessor);
    }

    /**
     * 界面控件点击监听器
     */
    @OnClick({
            R.id.img_call_background, R.id.layout_call_control, R.id.surface_view_local,
            R.id.surface_view_opposite, R.id.btn_exit_full_screen, R.id.btn_change_camera_switch,
            R.id.btn_mic_switch, R.id.btn_camera_switch, R.id.btn_speaker_switch,
            R.id.btn_record_switch, R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_call_control:
            case R.id.img_call_background:
                onControlLayout();
                break;
            case R.id.surface_view_local:
                changeSurfaceViewSize();
                break;
            case R.id.surface_view_opposite:
                onControlLayout();
                break;
            case R.id.btn_exit_full_screen:
                // 最小化通话界面
                exitFullScreen();
                break;
            case R.id.btn_change_camera_switch:
                // 切换摄像头
                changeCamera();
                break;
            case R.id.btn_mic_switch:
                // 麦克风开关
                onMicrophone();
                break;
            case R.id.btn_camera_switch:
                // 摄像头开关
                onCamera();
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
     * 控制界面的显示与隐藏
     */
    private void onControlLayout() {
        if (controlLayout.isShown()) {
            controlLayout.setVisibility(View.GONE);
        } else {
            controlLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 改变通话界面大小显示
     */
    private void changeSurfaceViewSize() {
        RelativeLayout.LayoutParams localLayoutParams =
                (RelativeLayout.LayoutParams) localSurfaceView.getLayoutParams();
        RelativeLayout.LayoutParams oppositeLayoutParams =
                (RelativeLayout.LayoutParams) oppositeSurfaceView.getLayoutParams();
        if (surfaceViewState == 1) {
            surfaceViewState = 0;
            localLayoutParams.width = 240;
            localLayoutParams.height = 320;
            oppositeLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            oppositeLayoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        } else {
            surfaceViewState = 1;
            localLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            oppositeLayoutParams.width = 240;
            oppositeLayoutParams.height = 320;
        }

        localSurfaceView.setLayoutParams(localLayoutParams);
        oppositeSurfaceView.setLayoutParams(oppositeLayoutParams);
    }

    /**
     * 退出全屏通话界面
     */
    private void exitFullScreen() {
        // 让应用回到桌面
        //        activity.moveTaskToBack(true);
        activity.finish();
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        // 根据切换摄像头开关是否被激活确定当前是前置还是后置摄像头
        if (changeCameraSwitch.isActivated()) {
            EMClient.getInstance().callManager().switchCamera();
            // 设置按钮状态
            changeCameraSwitch.setActivated(false);
        } else {
            EMClient.getInstance().callManager().switchCamera();
            // 设置按钮状态
            changeCameraSwitch.setActivated(true);
        }
    }

    /**
     * 麦克风开关，主要调用环信语音数据传输方法
     */
    private void onMicrophone() {
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
     * 摄像头开关
     */
    private void onCamera() {
        try {
            // 根据摄像头开关按钮状态判断摄像头状态，然后进行下一步操作
            if (cameraSwitch.isActivated()) {
                // 暂停视频数据的传输
                EMClient.getInstance().callManager().pauseVideoTransfer();
                // 设置按钮状态
                cameraSwitch.setActivated(false);
            } else {
                // 恢复视频数据的传输
                EMClient.getInstance().callManager().resumeVideoTransfer();
                // 设置按钮状态
                cameraSwitch.setActivated(true);
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
        // 根据开关状态决定是否开启录制
        if (recordSwitch.isActivated()) {
            // 设置按钮状态
            recordSwitch.setActivated(false);
            String path = videoCallHelper.stopVideoRecord();
            MLLog.d("录制视频完成 " + path);
            Toast.makeText(activity, "录制完成" + path, Toast.LENGTH_LONG).show();
        } else {
            // 设置按钮状态
            recordSwitch.setActivated(true);
            // 先创建文件夹
            String dirPath = getExternalFilesDir("").getAbsolutePath() + "/videos";
            File dir = new File(dirPath);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            videoCallHelper.startVideoRecord(dirPath);
            MLLog.d("开始录制视频");
            Toast.makeText(activity, "开始录制", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 结束通话时关闭界面
     */
    @Override protected void onFinish() {
        // 结束通话要把 SurfaceView 释放 重置为 null
        localSurfaceView = null;
        oppositeSurfaceView = null;
        super.onFinish();
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
                    case ACCEPTED: // 通话已接通:
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                changeSurfaceViewSize();
                            }
                        });
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

    /**
     * 屏幕方向改变回调方法
     */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

package com.vmloft.develop.app.demo.call;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMVideoCallHelper;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.superrtc.sdk.VideoView;
import com.vmloft.develop.app.demo.call.camera.PreviewManager;
import com.vmloft.develop.library.tools.utils.VMDimenUtil;
import com.vmloft.develop.library.tools.utils.VMFileUtil;
import com.vmloft.develop.library.tools.utils.VMLog;
import com.vmloft.develop.library.tools.utils.VMViewUtil;

import java.io.File;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lzan13 on 2016/10/18.
 * 视频通话界面处理
 */
public class VideoCallActivity extends CallActivity {

    // 视频通话帮助类
    private EMVideoCallHelper videoCallHelper;
    // SurfaceView 控件状态，-1 表示通话未接通，0 表示本小远大，1 表示远小本大
    private int surfaceState = -1;
    private boolean isMonitor = false;

    private int littleWidth;
    private int littleHeight;
    private int rightMargin;
    private int topMargin;


    private EMCallSurfaceView localSurface = null;
    private EMCallSurfaceView oppositeSurface = null;
    private RelativeLayout.LayoutParams localParams = null;
    private RelativeLayout.LayoutParams oppositeParams = null;

    // 使用 ButterKnife 注解的方式获取控件
    @BindView(R.id.layout_root) View rootView;
    @BindView(R.id.layout_call_control) View controlLayout;
    @BindView(R.id.layout_surface_container) RelativeLayout surfaceLayout;
    @BindView(R.id.surface_view) SurfaceView surfaceView;

    @BindView(R.id.btn_exit_full_screen) ImageButton exitFullScreenBtn;
    @BindView(R.id.text_call_state) TextView callStateView;
    @BindView(R.id.text_call_time) TextView callTimeView;
    @BindView(R.id.btn_call_info) ImageButton callInfoBtn;
    @BindView(R.id.text_call_info) TextView callInfoView;
    @BindView(R.id.btn_mic_switch) ImageButton micSwitch;
    @BindView(R.id.btn_camera_switch) ImageButton cameraSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton speakerSwitch;
    @BindView(R.id.btn_record_switch) ImageButton recordSwitch;
    @BindView(R.id.btn_screenshot) ImageButton screenshotSwitch;
    @BindView(R.id.btn_change_camera_switch) ImageButton changeCameraSwitch;
    @BindView(R.id.fab_reject_call) FloatingActionButton rejectCallFab;
    @BindView(R.id.fab_end_call) FloatingActionButton endCallFab;
    @BindView(R.id.fab_answer_call) FloatingActionButton answerCallFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 重载父类方法,实现一些当前通话的操作，
     */
    @Override
    protected void initView() {
        VMViewUtil.getAllChildViews(activity.getWindow()
                                            .getDecorView(), 1);

        littleWidth = VMDimenUtil.dp2px(96);
        littleHeight = VMDimenUtil.dp2px(128);
        rightMargin = VMDimenUtil.dp2px(16);
        topMargin = VMDimenUtil.dp2px(96);

        super.initView();
        if (CallManager.getInstance()
                       .isInComingCall()) {
            endCallFab.setVisibility(View.GONE);
            answerCallFab.setVisibility(View.VISIBLE);
            rejectCallFab.setVisibility(View.VISIBLE);
            callStateView.setText(R.string.call_connected_is_incoming);
        } else {
            endCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.GONE);
            callStateView.setText(R.string.call_connecting);
        }

        micSwitch.setActivated(!CallManager.getInstance()
                                           .isOpenMic());
        cameraSwitch.setActivated(!CallManager.getInstance()
                                              .isOpenCamera());
        speakerSwitch.setActivated(CallManager.getInstance()
                                              .isOpenSpeaker());
        recordSwitch.setActivated(CallManager.getInstance()
                                             .isOpenRecord());

        // 初始化视频通话帮助类
        videoCallHelper = EMClient.getInstance()
                                  .callManager()
                                  .getVideoCallHelper();

        // 初始化显示通话画面
        initCallSurface();
        // 判断当前通话时刚开始，还是从后台恢复已经存在的通话
        if (CallManager.getInstance()
                       .getCallState() == CallManager.CallState.ACCEPTED) {
            endCallFab.setVisibility(View.VISIBLE);
            answerCallFab.setVisibility(View.GONE);
            rejectCallFab.setVisibility(View.GONE);
            callStateView.setText(R.string.call_accepted);
            refreshCallTime();
            // 通话已接通，修改画面显示
            onCallSurface();
        }

        try {
            // 设置默认摄像头为前置
            EMClient.getInstance()
                    .callManager()
                    .setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        if (CallManager.getInstance()
                       .isExternalInputData()) {
            new PreviewManager(surfaceView);
        }
    }

    /**
     * 界面控件点击监听器
     */
    @OnClick({R.id.layout_call_control, R.id.btn_exit_full_screen, R.id.btn_call_info,
                     R.id.btn_mic_switch, R.id.btn_camera_switch, R.id.btn_speaker_switch,
                     R.id.btn_record_switch, R.id.btn_screenshot, R.id.btn_change_camera_switch,
                     R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call})
    void onClick(View v) {
        switch (v.getId()) {
        case R.id.layout_call_control:
            onControlLayout();
            break;
        case R.id.btn_exit_full_screen:
            // 最小化通话界面
            exitFullScreen();
            break;
        case R.id.btn_call_info:
            callInfoMonitor();
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
        case R.id.btn_screenshot:
            // 保存通话截图
            onScreenShot();
            break;
        case R.id.btn_record_switch:
            // 录制开关
            onRecordCall();
            break;
        case R.id.btn_change_camera_switch:
            // 切换摄像头
            changeCamera();
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
     * 退出全屏通话界面
     */
    private void exitFullScreen() {
        CallManager.getInstance()
                   .addFloatWindow();
        // 结束当前界面
        onFinish();
    }

    /**
     * 通话信息监听器
     */
    private void callInfoMonitor() {
        if (isMonitor) {
            isMonitor = false;
            callInfoView.setVisibility(View.GONE);
            callInfoBtn.setActivated(isMonitor);
        } else {
            isMonitor = true;
            callInfoView.setVisibility(View.VISIBLE);
            callInfoBtn.setActivated(isMonitor);
            new Thread(new Runnable() {
                public void run() {
                    while (isMonitor) {
                        final String info = String.format("分辨率: %d*%d, \n延迟: %d, \n帧率: %d, \n丢失: %d, \n本地码率: %d, \n远端码率: %d, \n直连: %b", videoCallHelper.getVideoWidth(), videoCallHelper.getVideoHeight(), videoCallHelper.getVideoLatency(), videoCallHelper.getVideoFrameRate(), videoCallHelper.getVideoLostRate(), videoCallHelper.getLocalBitrate(), videoCallHelper.getRemoteBitrate(), EMClient.getInstance()
                                                                                                                                                                                                                                                                                                                                                                                                      .callManager()
                                                                                                                                                                                                                                                                                                                                                                                                      .isDirectCall());
                        runOnUiThread(new Runnable() {
                            public void run() {
                                callInfoView.setText(info);
                            }
                        });
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 麦克风开关，主要调用环信语音数据传输方法
     */
    private void onMicrophone() {
        try {
            // 根据麦克风开关是否被激活来进行判断麦克风状态，然后进行下一步操作
            if (micSwitch.isActivated()) {
                // 设置按钮状态
                micSwitch.setActivated(false);
                // 暂停语音数据的传输
                EMClient.getInstance()
                        .callManager()
                        .resumeVoiceTransfer();
                CallManager.getInstance()
                           .setOpenMic(true);
            } else {
                // 设置按钮状态
                micSwitch.setActivated(true);
                // 恢复语音数据的传输
                EMClient.getInstance()
                        .callManager()
                        .pauseVoiceTransfer();
                CallManager.getInstance()
                           .setOpenMic(false);
            }
        } catch (HyphenateException e) {
            VMLog.e("exception code: %d, %s", e.getErrorCode(), e.getMessage());
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
                // 设置按钮状态
                cameraSwitch.setActivated(false);
                // 暂停视频数据的传输
                EMClient.getInstance()
                        .callManager()
                        .resumeVideoTransfer();
                CallManager.getInstance()
                           .setOpenCamera(true);
            } else {
                // 设置按钮状态
                cameraSwitch.setActivated(true);
                // 恢复视频数据的传输
                EMClient.getInstance()
                        .callManager()
                        .pauseVideoTransfer();
                CallManager.getInstance()
                           .setOpenCamera(false);
            }
        } catch (HyphenateException e) {
            VMLog.e("exception code: %d, %s", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 扬声器开关
     */
    private void onSpeaker() {
        // 根据按钮状态决定打开还是关闭扬声器
        if (speakerSwitch.isActivated()) {
            // 设置按钮状态
            speakerSwitch.setActivated(false);
            CallManager.getInstance()
                       .closeSpeaker();
            CallManager.getInstance()
                       .setOpenSpeaker(false);
        } else {
            // 设置按钮状态
            speakerSwitch.setActivated(true);
            CallManager.getInstance()
                       .openSpeaker();
            CallManager.getInstance()
                       .setOpenSpeaker(true);
        }
    }

    /**
     * 录制视屏通话内容
     */
    private void onRecordCall() {
        // 根据开关状态决定是否开启录制
        if (recordSwitch.isActivated()) {
            // 设置按钮状态
            recordSwitch.setActivated(false);
            String path = videoCallHelper.stopVideoRecord();
            CallManager.getInstance()
                       .setOpenRecord(false);
            File file = new File(path);
            if (file.exists()) {
                Toast.makeText(activity, "录制视频成功 " + path, Toast.LENGTH_LONG)
                     .show();
            } else {
                Toast.makeText(activity, "录制失败/(ㄒoㄒ)/~~", Toast.LENGTH_LONG)
                     .show();
            }
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
            VMLog.d("开始录制视频");
            Toast.makeText(activity, "开始录制", Toast.LENGTH_LONG)
                 .show();
            CallManager.getInstance()
                       .setOpenRecord(true);
        }
    }

    /**
     * 保存通话截图
     */
    private void onScreenShot() {
        String dirPath = VMFileUtil.getFilesFromSDCard() + "videos/";
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        String path = dirPath + "IMG_" + System.currentTimeMillis() + ".jpg";
        videoCallHelper.takePicture(path);
        Toast.makeText(activity, "拍照保存成功 " + path, Toast.LENGTH_LONG)
             .show();
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        //        testImgView.setImageBitmap(bitmap);
        //        testImgView.setVisibility(View.VISIBLE);
        //        testImgView.setOnClickListener(new View.OnClickListener() {
        //            @Override public void onClick(View v) {
        //                testImgView.setVisibility(View.GONE);
        //            }
        //        });
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        // 根据切换摄像头开关是否被激活确定当前是前置还是后置摄像头
        try {
            if (EMClient.getInstance()
                        .callManager()
                        .getCameraFacing() == 1) {
                EMClient.getInstance()
                        .callManager()
                        .switchCamera();
                EMClient.getInstance()
                        .callManager()
                        .setCameraFacing(0);
            } else {
                EMClient.getInstance()
                        .callManager()
                        .switchCamera();
                EMClient.getInstance()
                        .callManager()
                        .setCameraFacing(1);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接听通话
     */
    @Override
    protected void answerCall() {
        super.answerCall();
        endCallFab.setVisibility(View.VISIBLE);
        rejectCallFab.setVisibility(View.GONE);
        answerCallFab.setVisibility(View.GONE);
    }

    /**
     * 初始化通话界面控件
     */
    private void initCallSurface() {
        // 初始化显示远端画面控件
        oppositeSurface = new EMCallSurfaceView(activity);
        oppositeParams = new RelativeLayout.LayoutParams(0, 0);
        oppositeParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        oppositeParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        oppositeSurface.setLayoutParams(oppositeParams);
        surfaceLayout.addView(oppositeSurface);

        // 初始化显示本地画面控件
        localSurface = new EMCallSurfaceView(activity);
        localParams = new RelativeLayout.LayoutParams(0, 0);
        localParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        localParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        localParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        localSurface.setLayoutParams(localParams);
        surfaceLayout.addView(localSurface);

        localSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onControlLayout();
            }
        });

        localSurface.setZOrderOnTop(false);
        localSurface.setZOrderMediaOverlay(true);

        // 设置本地和远端画面的显示方式，是填充，还是居中
        localSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
        oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
        // 设置通话画面显示控件
        EMClient.getInstance()
                .callManager()
                .setSurfaceView(localSurface, oppositeSurface);
    }

    /**
     * 接通通话，这个时候要做的只是改变本地画面 view 大小，不需要做其他操作
     */
    private void onCallSurface() {
        // 更新通话界面控件状态
        surfaceState = 0;

        localParams = new RelativeLayout.LayoutParams(littleWidth, littleHeight);
        localParams.width = littleWidth;
        localParams.height = littleHeight;
        localParams.rightMargin = rightMargin;
        localParams.topMargin = topMargin;
        localParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        localSurface.setLayoutParams(localParams);

        localSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCallSurface();
            }
        });

        oppositeSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onControlLayout();
            }
        });
    }

    /**
     * 切换通话界面，这里就是交换本地和远端画面控件设置，以达到通话大小画面的切换
     */
    private void changeCallSurface() {
        if (surfaceState == 0) {
            surfaceState = 1;
            EMClient.getInstance()
                    .callManager()
                    .setSurfaceView(oppositeSurface, localSurface);
        } else {
            surfaceState = 0;
            EMClient.getInstance()
                    .callManager()
                    .setSurfaceView(localSurface, oppositeSurface);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBus(CallEvent event) {
        if (event.isState()) {
            refreshCallView(event);
        }
        if (event.isTime()) {
            // 不论什么情况都检查下当前时间
            refreshCallTime();
        }
    }

    /**
     * 刷新通话界面
     */
    private void refreshCallView(CallEvent event) {
        EMCallStateChangeListener.CallError callError = event.getCallError();
        EMCallStateChangeListener.CallState callState = event.getCallState();
        switch (callState) {
        case CONNECTING: // 正在呼叫对方，TODO 没见回调过
            VMLog.i("正在呼叫对方" + callError);
            break;
        case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
            VMLog.i("正在连接" + callError);
            if (CallManager.getInstance()
                           .isInComingCall()) {
                callStateView.setText(R.string.call_connected_is_incoming);
            } else {
                callStateView.setText(R.string.call_connected);
            }
            break;
        case ACCEPTED: // 通话已接通
            VMLog.i("通话已接通");
            callStateView.setText(R.string.call_accepted);
            // 通话接通，更新界面 UI 显示
            onCallSurface();
            break;
        case DISCONNECTED: // 通话已中断
            VMLog.i("通话已结束" + callError);
            onFinish();
            break;
        case NETWORK_DISCONNECTED:
            Toast.makeText(activity, "对方网络断开", Toast.LENGTH_SHORT)
                 .show();
            VMLog.i("对方网络断开");
            break;
        case NETWORK_NORMAL:
            VMLog.i("网络正常");
            break;
        case NETWORK_UNSTABLE:
            if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                VMLog.i("没有通话数据" + callError);
            } else {
                VMLog.i("网络不稳定" + callError);
            }
            break;
        case VIDEO_PAUSE:
            Toast.makeText(activity, "对方已暂停视频传输", Toast.LENGTH_SHORT)
                 .show();
            VMLog.i("对方已暂停视频传输");
            break;
        case VIDEO_RESUME:
            Toast.makeText(activity, "对方已恢复视频传输", Toast.LENGTH_SHORT)
                 .show();
            VMLog.i("对方已恢复视频传输");
            break;
        case VOICE_PAUSE:
            Toast.makeText(activity, "对方已暂停语音传输", Toast.LENGTH_SHORT)
                 .show();
            VMLog.i("对方已暂停语音传输");
            break;
        case VOICE_RESUME:
            Toast.makeText(activity, "对方已恢复语音传输", Toast.LENGTH_SHORT)
                 .show();
            VMLog.i("对方已恢复语音传输");
            break;
        default:
            break;
        }
    }

    /**
     * 刷新通话时间显示
     */
    private void refreshCallTime() {
        int t = CallManager.getInstance()
                           .getCallTime();
        int h = t / 60 / 60;
        int m = t / 60 % 60;
        int s = t % 60 % 60;
        String time = "";
        if (h > 9) {
            time = "" + h;
        } else {
            time = "0" + h;
        }
        if (m > 9) {
            time += ":" + m;
        } else {
            time += ":0" + m;
        }
        if (s > 9) {
            time += ":" + s;
        } else {
            time += ":0" + s;
        }
        if (!callTimeView.isShown()) {
            callTimeView.setVisibility(View.VISIBLE);
        }
        callTimeView.setText(time);
    }

    /**
     * 屏幕方向改变回调方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onUserLeaveHint() {
        //super.onUserLeaveHint();
        exitFullScreen();
    }

    /**
     * 通话界面拦截 Back 按键，不能返回
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        exitFullScreen();
    }

    @Override
    public void onFinish() {
        // release surface view
        if (localSurface != null) {
            if (localSurface.getRenderer() != null) {
                localSurface.getRenderer()
                            .dispose();
            }
            localSurface.release();
            localSurface = null;
        }
        if (oppositeSurface != null) {
            if (oppositeSurface.getRenderer() != null) {
                oppositeSurface.getRenderer()
                               .dispose();
            }
            oppositeSurface.release();
            oppositeSurface = null;
        }
        super.onFinish();
    }
}

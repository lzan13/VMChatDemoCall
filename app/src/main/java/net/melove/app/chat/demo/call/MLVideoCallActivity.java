package net.melove.app.chat.demo.call;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;

/**
 * Created by lzan13 on 2016/7/8.
 */
public class MLVideoCallActivity extends AppCompatActivity {


    private EMCallManager.EMVideoCallHelper mVideoCallHelper;
    // 呼叫方名字
    private String username;
    // 是否是拨打进来的电话
    private boolean isInComingCall;
    // 通话状态监听器
    private EMCallStateChangeListener callStateListener;

    // 显示对方画面控件
    private EMOppositeSurfaceView oppositeSurfaceView;
    // 显示自己方画面控件
    private EMLocalSurfaceView localSurfaceView;

    private boolean isDebug = true;
    private TextView mTestView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_video_call);

        mTestView = (TextView) findViewById(R.id.ml_text_test);

        findViewById(R.id.ml_btn_answer_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_reject_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_end_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_mute).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_change_camera).setOnClickListener(viewListener);

        // 自己 surfaceview
        localSurfaceView = (EMLocalSurfaceView) findViewById(R.id.ml_surface_local);
        localSurfaceView.setZOrderMediaOverlay(true);
        localSurfaceView.setZOrderOnTop(true);

        // 对方 surfaceview
        oppositeSurfaceView = (EMOppositeSurfaceView) findViewById(R.id.ml_surface_opposite);

        // 设置视频通话双方显示画面控件
        EMClient.getInstance().callManager().setSurfaceView(localSurfaceView, oppositeSurfaceView);

        mVideoCallHelper = EMClient.getInstance().callManager().getVideoCallHelper();

        // 设置视频通话分辨率 默认是320，240
        mVideoCallHelper.setResolution(640, 480);
        // 设置比特率 默认是150
        mVideoCallHelper.setVideoBitrate(300);


        username = getIntent().getStringExtra("username");
        // 语音电话是否为接收的
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);

        if (!isInComingCall) {
            // 呼叫对方
            try {
                // 拨打语音电话
                EMClient.getInstance().callManager().makeVideoCall(username);
            } catch (HyphenateException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MLVideoCallActivity.this, "尚未连接至服务器", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // 自己被呼叫
        }

        // 设置摄像头数据处理
        EMClient.getInstance().callManager().setCameraDataProcessor(new MLCameraDataProcessor());
        // 设置通话状态监听
        setCallStateListener();
    }


    /**
     * 设置电话监听
     */
    void setCallStateListener() {
        callStateListener = new EMCallStateChangeListener() {

            @Override
            public void onCallStateChanged(CallState callState, final CallError error) {
                // Message msg = handler.obtainMessage();
                switch (callState) {

                    case CONNECTING: // 正在连接对方
                        Log.i("lzna13", "正在连接对方");
                        break;
                    case CONNECTED: // 双方已经建立连接
                        Log.i("lzna13", "双方已经建立连接");
                        break;
                    case ACCEPTED: // 电话接通成功
                        Log.i("lzna13", "电话接通成功");
                        startMonitor();
                        break;
                    case DISCONNNECTED: // 电话断了
                        Log.i("lzna13", "电话断了" + error);
                        finish();
                        break;
                    case NETWORK_UNSTABLE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (error == CallError.ERROR_NO_DATA) {
                                    Log.i("lzna13", "没有通话数据" + error);
                                } else {
                                    Log.i("lzna13", "网络不稳定" + error);
                                }
                            }
                        });
                        break;
                    case NETWORK_NORMAL:
                        Log.i("lzna13", "网络正常");
                        break;
                    default:
                        break;
                }

            }
        };
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }

    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ml_btn_answer_call:
                    // 接听对方的呼叫
                    try {
                        Log.i("lzna13", "正在接听...");
                        EMClient.getInstance().callManager().answerCall();
                        Log.i("lzna13", "接听成功");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i("lzna13", "接听失败");
                        finish();
                        return;
                    }
                    break;
                case R.id.ml_btn_reject_call:
                    // 拒绝接听对方的呼叫
                    try {
                        EMClient.getInstance().callManager().rejectCall();
                        Log.i("lzan13", "拒绝接听呼叫");
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("lzan13", "拒绝接听失败");
                        finish();
                    }
                    break;
                case R.id.ml_btn_end_call:
                    // 挂断通话
                    try {
                        EMClient.getInstance().callManager().endCall();
                        Log.i("lzan13", "挂断电话");
                    } catch (EMNoActiveCallException e) {
                        Log.i("lzan13", "挂断电话失败");
                        finish();
                        e.printStackTrace();
                    }
                    break;
                case R.id.ml_btn_change_camera:
                    // 切换摄像头
                    EMClient.getInstance().callManager().switchCamera();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * for debug & testing, you can remove this when releasekak
     */
    void startMonitor() {
        new Thread(new Runnable() {
            public void run() {
                while (isDebug) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mTestView.setText("WidthxHeight：" + mVideoCallHelper.getVideoWidth()
                                    + "x" + mVideoCallHelper.getVideoHeight()
                                    + "\nDelay：" + mVideoCallHelper.getVideoLatency()
                                    + "\nFramerate：" + mVideoCallHelper.getVideoFrameRate()
                                    + "\nLost：" + mVideoCallHelper.getVideoLostRate()
                                    + "\nLocalBitrate：" + mVideoCallHelper.getLocalBitrate()
                                    + "\nRemoteBitrate：" + mVideoCallHelper.getRemoteBitrate());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDebug = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

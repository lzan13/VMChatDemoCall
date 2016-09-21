package net.melove.app.chat.demo.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMVideoCallHelper;
import com.easemob.exceptions.EMServiceNotReadyException;

/**
 * Created by lzan13 on 2016/7/8.
 */
public class MLVideoCallActivity extends AppCompatActivity {

    private String username;
    private boolean isInComingCall;
    private EMCallStateChangeListener callStateListener;

    private CameraHelper cameraHelper;

    private SurfaceView localSurface;
    private SurfaceHolder localSurfaceHolder;
    private SurfaceView oppositeSurface;
    private SurfaceHolder oppositeSurfaceHolder;

    private EMVideoCallHelper callHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // 注册语音电话的状态的监听
        addCallStateListener();

        username = getIntent().getStringExtra("username");
        // 语音电话是否为接收的
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);

        if (!isInComingCall) {// 拨打电话
            try {
                // 拨打语音电话
                EMChatManager.getInstance().makeVideoCall(username);
            } catch (EMServiceNotReadyException e) {
                e.printStackTrace();
                final String st2 = "尚未连接至服务器";
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MLVideoCallActivity.this, st2, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // 有电话进来

        }
        findViewById(R.id.ml_btn_mic_switch).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_speaker_switch).setOnClickListener(viewListener);

        findViewById(R.id.ml_btn_answer_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_reject_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_end_call).setOnClickListener(viewListener);

        // 显示本地图像的surfaceview
        localSurface = (SurfaceView) findViewById(R.id.ml_surface_view_local);
        localSurface.setZOrderMediaOverlay(true);
        localSurface.setZOrderOnTop(true);
        localSurfaceHolder = localSurface.getHolder();

        // 获取callHelper,cameraHelper
        callHelper = EMVideoCallHelper.getInstance();
        cameraHelper = new CameraHelper(callHelper, localSurfaceHolder);

        // 显示对方图像的surfaceview
        oppositeSurface = (SurfaceView) findViewById(R.id.ml_surface_view_opposite);
        oppositeSurfaceHolder = oppositeSurface.getHolder();
        // 设置显示对方图像的surfaceview
        callHelper.setSurfaceView(oppositeSurface);

        localSurfaceHolder.addCallback(new LocalCallback());
        oppositeSurfaceHolder.addCallback(new OppositeCallback());

    }

    /**
     * 设置电话监听
     */
    void addCallStateListener() {
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
        EMChatManager.getInstance().addCallStateChangeListener(callStateListener);
    }

    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.ml_btn_answer_call: // 接听通话
                    try {
                        Log.i("lzna13", "正在接听...");
                        EMChatManager.getInstance().answerCall();
                        Log.i("lzna13", "接听成功");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i("lzna13", "接听失败");
                        finish();
                        return;
                    }
                    break;

                case R.id.ml_btn_reject_call: // 拒绝通话
                    try {
                        Log.i("lzan13", "开始拒绝电话");
                        EMChatManager.getInstance().rejectCall();
                        Log.i("lzan13", "拒绝电话成功");
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("lzan13", "拒绝电话失败");
                        finish();
                    }
                    break;
                case R.id.ml_btn_end_call:  // 挂断通话
                    try {
                        Log.i("lzan13", "开始挂断电话");
                        EMChatManager.getInstance().endCall();
                        Log.i("lzan13", "挂断电话成功");
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("lzan13", "挂断电话失败");
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 本地SurfaceHolder callback
     */
    class LocalCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            cameraHelper.startCapture();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    /**
     * 对方SurfaceHolder callback
     */
    class OppositeCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
            callHelper.setRenderFlag(true);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            callHelper.onWindowResize(width, height, format);
            if (!cameraHelper.isStarted()) {
                if (!isInComingCall) {
                    try {
                        // 拨打视频通话
                        EMChatManager.getInstance().makeVideoCall(username);
                        // 通知cameraHelper可以写入数据
                        cameraHelper.setStartFlag(true);
                    } catch (EMServiceNotReadyException e) {
                        Toast.makeText(MLVideoCallActivity.this, "没有连接到服务器", Toast.LENGTH_LONG).show();
                    }
                }

            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            callHelper.setRenderFlag(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMChatManager.getInstance().removeCallStateChangeListener(callStateListener);
        callHelper.setSurfaceView(null);
        cameraHelper.stopCapture();
        oppositeSurface = null;
        cameraHelper = null;
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

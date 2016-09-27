package net.melove.app.chat.demo.call;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.HyphenateException;


public class MLVoiceCallActivity extends AppCompatActivity {


    // 呼叫方名字
    private String username;
    // 是否是拨打进来的电话
    private boolean isInComingCall;
    // 通话状态监听器
    private EMCallStateChangeListener callStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_voice_call);

        // 注册语音电话的状态的监听
        addCallStateListener();

        username = getIntent().getStringExtra("username");
        // 语音电话是否为接收的
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);

        if (!isInComingCall) {// 拨打电话
            try {
                // 拨打语音电话
                EMClient.getInstance().callManager().makeVoiceCall(username);
            } catch (HyphenateException e) {
                e.printStackTrace();
                final String st2 = "尚未连接至服务器";
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MLVoiceCallActivity.this, st2, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // 有电话进来

        }
        findViewById(R.id.ml_btn_answer_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_reject_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_end_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_mute).setOnClickListener(viewListener);
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
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
        finish();
    }
}

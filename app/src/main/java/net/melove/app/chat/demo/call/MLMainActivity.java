package net.melove.app.chat.demo.call;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;

import java.io.File;

/**
 * 音视频项目主类
 */
public class MLMainActivity extends AppCompatActivity {

    private static final String TAG = "MLMainActivity";

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mContactsView;

    private String username;
    private String password;
    private String constactUser;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsernameView = (EditText) findViewById(R.id.ml_edit_login_username);
        mPasswordView = (EditText) findViewById(R.id.ml_edit_login_password);
        mContactsView = (EditText) findViewById(R.id.ml_edit_contacts_username);

        findViewById(R.id.ml_btn_signin).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_signup).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_signout).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_send).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_call_voice).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_call_video).setOnClickListener(viewListener);
    }

    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ml_btn_signin:
                    signinEasemob();
                    break;
                case R.id.ml_btn_signup:
                    signupEasemob();
                    break;
                case R.id.ml_btn_signout:
                    signoutEasemob();
                    break;
                case R.id.ml_btn_send:
                    sendTextMessage();
                    break;
                case R.id.ml_btn_call_voice:
                    callVoice();
                    break;
                case R.id.ml_btn_call_video:
                    callVideo();
                    break;
            }
        }
    };

    /**
     * 视频呼叫
     */
    private void callVideo() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MLMainActivity.this, MLVideoCallActivity.class);
        MLCallManager.getInstance().setChatId(constactUser);
        MLCallManager.getInstance().setInComingCall(false);
        MLCallManager.getInstance().setCallType(MLCallManager.CallType.VIDEO);
        startActivity(intent);
    }

    /**
     * 语音呼叫
     */
    private void callVoice() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MLMainActivity.this, MLVoiceCallActivity.class);
        MLCallManager.getInstance().setChatId(constactUser);
        MLCallManager.getInstance().setInComingCall(false);
        MLCallManager.getInstance().setCallType(MLCallManager.CallType.VOICE);
        startActivity(intent);
    }

    /**
     * 发送消息
     */
    private void sendTextMessage() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        final EMMessage message = EMMessage.createTxtSendMessage("test text message", constactUser);
        sendMessage(message);
    }

    /**
     * 最终调用发送信息方法
     *
     * @param message 需要发送的消息
     */
    private void sendMessage(final EMMessage message) {
        /**
         *  调用sdk的消息发送方法发送消息，发送消息时要尽早的设置消息监听，防止消息状态已经回调，
         *  但是自己没有注册监听，导致检测不到消息状态的变化
         *  所以这里在发送之前先设置消息的状态回调
         */
        message.setMessageStatusCallback(new EMCallBack() {
            @Override public void onSuccess() {
                Log.i("lzan13", String.format("消息发送成功 msgId %s, content %s", message.getMsgId(),
                        message.getBody()));
            }

            @Override public void onError(final int i, final String s) {
                Log.i("lzan13", String.format("消息发送失败 code: %d, error: %s", i, s));
            }

            @Override public void onProgress(int i, String s) {
                // TODO 消息发送进度，这里不处理，留给消息Item自己去更新
                Log.i("lzan13", String.format("消息发送中 progress: %d, %s", i, s));
            }
        });
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * 退出登录
     */
    private void signoutEasemob() {
        EMClient.getInstance().logout(true, new EMCallBack() {
            @Override public void onSuccess() {
                Log.i(TAG, "logout easemob success");
            }

            @Override public void onError(int i, String s) {
                Log.i(TAG, "logout easemob error: " + i + "; " + s);
            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 注册账户
     */
    private void signupEasemob() {
        username = mUsernameView.getText().toString().trim();
        password = mPasswordView.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "username or password not null", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMClient.getInstance().createAccount(username, password);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.d(TAG, "signup error " + e.getErrorCode() + "; " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 登录Easemob
     */
    private void signinEasemob() {
        username = mUsernameView.getText().toString().trim();
        password = mPasswordView.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "username or password not null", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override public void onSuccess() {
                Log.i(TAG, "login easemob success");
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Toast.makeText(MLMainActivity.this, "login easemob success",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onError(final int i, final String s) {
                Log.i(TAG, "login easemob error: " + i + "; " + s);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Toast.makeText(MLMainActivity.this, "login easemob error: " + i + "; " + s,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

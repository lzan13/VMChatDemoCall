package net.melove.app.chat.demo.call;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        @Override
        public void onClick(View v) {
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
                sendMessage();
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
        intent.putExtra("username", constactUser);
        intent.putExtra("isComingCall", false);
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
        intent.putExtra("username", constactUser);
        intent.putExtra("isComingCall", false);
        startActivity(intent);
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        final EMMessage message = EMMessage.createTxtSendMessage("test text message", "lz1");
//        message.setReceipt(username);
        EMClient.getInstance().chatManager().sendMessage(message);
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "message send success " + message.getMsgId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MLMainActivity.this, "message send success " + message.getMsgId(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final int i, final String s) {
                Log.i(TAG, "message send error " + i + "; " + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MLMainActivity.this, "message send error " + i + "; " + s, Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 退出登录
     */
    private void signoutEasemob() {
        EMClient.getInstance().logout(true, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "logout easemob success");
            }

            @Override
            public void onError(int i, String s) {
                Log.i(TAG, "logout easemob error: " + i + "; " + s);
            }

            @Override
            public void onProgress(int i, String s) {

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
            Toast.makeText(MLMainActivity.this, "username or password not null", Toast.LENGTH_LONG).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            Toast.makeText(MLMainActivity.this, "username or password not null", Toast.LENGTH_LONG).show();
            return;
        }
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "login easemob success");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MLMainActivity.this, "login easemob success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final int i, final String s) {
                Log.i(TAG, "login easemob error: " + i + "; " + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MLMainActivity.this, "login easemob error: " + i + "; " + s, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

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

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;

import java.util.List;

/**
 * 音视频项目主类
 */
public class MLMainActivity extends AppCompatActivity {

    private static final String TAG = "lzan13";

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
        findViewById(R.id.ml_btn_add_contact).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_add_contact_agree).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_add_contact_refuse).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_delete_contact).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_get_contact).setOnClickListener(viewListener);

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
            case R.id.ml_btn_add_contact:
                // 发送好友申请
                addContact();
                break;
            case R.id.ml_btn_add_contact_agree:
                // 同意好友申请
                agreeContactInvite();
                break;
            case R.id.ml_btn_add_contact_refuse:
                // 拒绝好友申请
                refuseContactInvite();
                break;
            case R.id.ml_btn_delete_contact:
                deleteContact();
                break;
            case R.id.ml_btn_get_contact:
                getContact();
                break;
            }
        }
    };

    /**
     * 从服务器获取联系人并输出
     */
    private void getContact() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> contacts = EMContactManager.getInstance().getContactUserNames();
                    if (contacts.size() == 0) {
                        Log.i(TAG, "contacts is null");
                    }
                    for (String username : contacts) {
                        Log.i(TAG, "my contact " + username);
                    }
                } catch (EaseMobException e) {
                    Log.i(TAG, "get contacts exception");
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 删除好友
     */
    private void deleteContact() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            EMContactManager.getInstance().deleteContact(constactUser);
            Toast.makeText(MLMainActivity.this, "deleteContact success", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "deleteContact success");
        } catch (EaseMobException e) {
            Toast.makeText(MLMainActivity.this, "deleteContact failed", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "deleteContact failed");
            e.printStackTrace();
        }
    }

    /**
     * 拒绝好友申请
     */
    private void refuseContactInvite() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            EMChatManager.getInstance().refuseInvitation(constactUser);
            Toast.makeText(MLMainActivity.this, "refuseInvitation success", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "refuseInvitation success");
        } catch (EaseMobException e) {
            Toast.makeText(MLMainActivity.this, "refuseInvitation failed", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "refuseInvitation failed");
            e.printStackTrace();
        }
    }

    /**
     * 同意好友申请
     */
    private void agreeContactInvite() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            EMChatManager.getInstance().acceptInvitation(constactUser);
            Toast.makeText(MLMainActivity.this, "acceptInvitation success", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "acceptInvitation success");
        } catch (EaseMobException e) {
            Toast.makeText(MLMainActivity.this, "acceptInvitation failed", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "acceptInvitation failed");
            e.printStackTrace();
        }
    }

    /**
     * 发送好友申请
     */
    private void addContact() {
        constactUser = mContactsView.getText().toString().trim();
        if (constactUser.isEmpty()) {
            Toast.makeText(MLMainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            // 调用环信添加好友方法
            EMContactManager.getInstance().addContact(constactUser, "hi!");
            Toast.makeText(MLMainActivity.this, "addContact success", Toast.LENGTH_SHORT).show();
        } catch (EaseMobException e) {
            Toast.makeText(MLMainActivity.this, "addContact failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

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
        final EMMessage message = EMMessage.createTxtSendMessage("test text message", constactUser);
        //        message.setReceipt(username);
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
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
        EMChatManager.getInstance().logout(true, new EMCallBack() {
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
                    EMChatManager.getInstance().createAccountOnServer(username, password);
                } catch (EaseMobException e) {
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
        EMChatManager.getInstance().login(username, password, new EMCallBack() {
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

package com.vmloft.develop.app.demo.call;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.vmloft.develop.library.tools.VMBaseActivity;
import com.vmloft.develop.library.tools.utils.VMLog;
import com.vmloft.develop.library.tools.utils.VMSPUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 音视频项目主类
 */
public class MainActivity extends VMBaseActivity {

    private final String TAG = this.getClass().getSimpleName();
    private VMBaseActivity activity;

    @BindView(R.id.layout_root) View rootView;

    @BindView(R.id.edit_username) EditText usernameView;
    @BindView(R.id.edit_password) EditText passwordView;
    @BindView(R.id.edit_contacts_username) EditText contactsView;

    private String username;
    private String password;
    private String contacts;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        ButterKnife.bind(activity);

        init();
    }

    private void init() {
        username = (String) VMSPUtil.get(activity, "username", "");
        password = (String) VMSPUtil.get(activity, "password", "");
        contacts = (String) VMSPUtil.get(activity, "contacts", "");
        usernameView.setText(username);
        passwordView.setText(password);
        contactsView.setText(contacts);
    }

    @OnClick({
            R.id.btn_sign_in, R.id.btn_sign_up, R.id.btn_sign_out, R.id.btn_send,
            R.id.btn_call_voice, R.id.btn_call_video
    }) void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                signIn();
                break;
            case R.id.btn_sign_up:
                signUp();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
            case R.id.btn_send:
                sendTextMessage();
                break;
            case R.id.btn_call_voice:
                callVoice();
                break;
            case R.id.btn_call_video:
                callVideo();
                break;
        }
    }

    /**
     * 发送消息
     */
    private void sendTextMessage() {
        checkContacts();
        EMMessage message = EMMessage.createTxtSendMessage("测试发送消息，主要是为了测试是否在线", contacts);
        // 设置强制推送
        message.setAttribute("em_force_notification", "true");
        // 设置自定义推送提示
        JSONObject extObj = new JSONObject();
        try {
            extObj.put("em_push_title", "测试消息推送，这里是推送内容，一般这里直接写上消息详情");
            extObj.put("extern", "定义推送扩展内容");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        message.setAttribute("em_apns_ext", extObj);
        sendMessage(message);
    }

    /**
     * 视频呼叫
     */
    private void callVideo() {
        checkContacts();
        Intent intent = new Intent(MainActivity.this, VideoCallActivity.class);
        CallManager.getInstance().setChatId(contacts);
        CallManager.getInstance().setInComingCall(false);
        CallManager.getInstance().setCallType(CallManager.CallType.VIDEO);
        startActivity(intent);
    }

    /**
     * 语音呼叫
     */
    private void callVoice() {
        checkContacts();
        Intent intent = new Intent(MainActivity.this, VoiceCallActivity.class);
        CallManager.getInstance().setChatId(contacts);
        CallManager.getInstance().setInComingCall(false);
        CallManager.getInstance().setCallType(CallManager.CallType.VOICE);
        startActivity(intent);
    }

    private void checkContacts() {
        contacts = contactsView.getText().toString().trim();
        if (contacts.isEmpty()) {
            Toast.makeText(MainActivity.this, "constact user not null", Toast.LENGTH_LONG).show();
            return;
        }
        VMSPUtil.put(activity, "contacts", contacts);
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
                String str = String.format("消息发送成功 msgId %s, content %s", message.getMsgId(),
                        message.getBody());
                Snackbar.make(rootView, str, Snackbar.LENGTH_INDEFINITE).show();
                VMLog.i(str);
            }

            @Override public void onError(final int i, final String s) {
                String str = String.format("消息发送失败 code: %d, error: %s", i, s);
                Snackbar.make(rootView, str, Snackbar.LENGTH_INDEFINITE).show();
                VMLog.i(str);
            }

            @Override public void onProgress(int i, String s) {
                // TODO 消息发送进度，这里不处理，留给消息Item自己去更新
                VMLog.i("消息发送中 progress: %d, %s", i, s);
            }
        });
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);

        testGetGroupDetails();
    }

    /**
     * 登录
     */
    private void signIn() {
        username = usernameView.getText().toString().trim();
        password = passwordView.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Snackbar.make(rootView, "username or password null", Snackbar.LENGTH_INDEFINITE).show();
            return;
        }
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override public void onSuccess() {
                VMLog.i("login success");

                try {
                    EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }

                VMSPUtil.put(activity, "username", username);
                VMSPUtil.put(activity, "password", password);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Snackbar.make(rootView, "login success", Snackbar.LENGTH_INDEFINITE).show();
                    }
                });
            }

            @Override public void onError(final int i, final String s) {
                final String str = "login error: " + i + "; " + s;
                VMLog.i(str);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Snackbar.make(rootView, str, Snackbar.LENGTH_INDEFINITE).show();
                    }
                });
            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 注册账户
     */
    private void signUp() {
        username = usernameView.getText().toString().trim();
        password = passwordView.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Snackbar.make(rootView, "username or password null", Snackbar.LENGTH_INDEFINITE).show();
            return;
        }
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMClient.getInstance().createAccount(username, password);
                } catch (HyphenateException e) {
                    final String str = "sign up error " + e.getErrorCode() + "; " + e.getMessage();
                    VMLog.d(str);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Snackbar.make(rootView, str, Snackbar.LENGTH_INDEFINITE).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 退出登录
     */
    private void signOut() {
        EMClient.getInstance().logout(true, new EMCallBack() {
            @Override public void onSuccess() {
                VMLog.i("logout success");
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Snackbar.make(rootView, "logout success", Snackbar.LENGTH_INDEFINITE)
                                .show();
                    }
                });
            }

            @Override public void onError(int i, String s) {
                final String str = "logout error: " + i + "; " + s;
                VMLog.i(str);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Snackbar.make(rootView, str, Snackbar.LENGTH_INDEFINITE).show();
                    }
                });
            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 获取群组详情
     */
    private void testGetGroupDetails() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    //EMGroup group = EMClient.getInstance().groupManager().getGroupFromServer("16364170444803");
                    // 当前群里总人数
                    int groupCount = 16;
                    int count = 0;
                    // 测试分页获取群成员列表
                    EMCursorResult<String> result = null;
                    do {
                        result = EMClient.getInstance()
                                .groupManager()
                                .fetchGroupMembers("16364170444803", result != null ? result.getCursor() : "", 5);
                        count += result.getData().size();
                        VMLog.d("group members result: count: %d, %s", result.getData().size(), result.getData().toString());
                    } while (count < groupCount - 1);
                } catch (HyphenateException e) {
                    VMLog.e("group details error: code - %d, msg - %s", e.getErrorCode(), e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

package com.vmloft.develop.app.demo.call;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.vmloft.develop.library.tools.VMBaseActivity;
import com.vmloft.develop.library.tools.utils.VMLog;
import com.vmloft.develop.library.tools.utils.VMSPUtil;
import com.vmloft.develop.library.tools.widget.VMViewGroup;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 音视频项目主类
 */
public class MainActivity extends VMBaseActivity {

    private final String TAG = this.getClass().getSimpleName();
    private VMBaseActivity activity;

    @BindView(R.id.layout_root) View rootView;
    @BindView(R.id.view_group) VMViewGroup viewGroup;

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

        String[] btnTitle = {
                "登录", "注册", "退出登录", "发送消息", "语音呼叫", "视频呼叫", "导入消息", "加载消息", "保存消息"
        };

        for (int i = 0; i < btnTitle.length; i++) {
            Button btn = new Button(new ContextThemeWrapper(activity, R.style.VMBtn_Green), null, 0);
            btn.setText(btnTitle[i]);
            btn.setId(100 + i);
            btn.setOnClickListener(viewListener);
            viewGroup.addView(btn);
        }
    }

    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            switch (v.getId()) {
                case 100:
                    signIn();
                    break;
                case 101:
                    signUp();
                    break;
                case 102:
                    signOut();
                    break;
                case 103:
                    sendMessage();
                    break;
                case 104:
                    callVoice();
                    break;
                case 105:
                    callVideo();
                    break;
                case 106:
                    importMessages();
                    break;
                case 107:
                    loadMoreMessage();
                    break;
                case 108:
                    saveMessage();
                    break;
            }
        }
    };

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

                EMClient.getInstance().chatManager().loadAllConversations();
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
                        Snackbar.make(rootView, "logout success", Snackbar.LENGTH_INDEFINITE).show();
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
     * 发送消息
     */
    private void sendMessage() {
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
        message.setAttribute("test_boolean", true);
        message.setAttribute("isBOOL", true);
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
                String str = String.format("消息发送成功 msgId %s, content %s", message.getMsgId(), message.getBody());
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
    }

    /**
     * 导入多条消息
     */
    private void importMessages() {
        String msgJson = "[{\n"
                + "    \"from\": \"lz1\",\n"
                + "    \"msg_id\": \"350475701050148824\",\n"
                + "    \"payload\": {\n"
                + "        \"ext\": {},\n"
                + "        \"bodies\": [{\n"
                + "            \"secret\": \"lRjEWmCoEee9vfnPereH2W_JZYRVXhhnyCYX7ejQ5Y2Va6SN\",\n"
                + "            \"size\": {\n"
                + "                \"width\": 288,\n"
                + "                \"height\": 384\n"
                + "            },\n"
                + "            \"filename\": \"1499166285637\",\n"
                + "            \"url\": \"https://a1.easemob.com/1188170313178478/youhaodongxi/chatfiles/9518c450-60a8-11e7-8229-e3a51fcf21a3\",\n"
                + "            \"type\": \"img\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"direction\": \"\",\n"
                + "    \"timestamp\": \"1499166285638\",\n"
                + "    \"to\": \"20814677213185\",\n"
                + "    \"chat_type\": \"GroupChat\"\n"
                + "},\n"
                + "{\n"
                + "    \"from\": \"lz2\",\n"
                + "    \"msg_id\": \"350475800664868824\",\n"
                + "    \"payload\": {\n"
                + "        \"ext\": {},\n"
                + "        \"bodies\": [{\n"
                + "            \"secret\": \"ouuV2mCoEeeZx7_YUlvSbZggsYQGkVkok97rpEyJDKPm5j3P\",\n"
                + "            \"length\": 2,\n"
                + "            \"thumb_secret\": \"oqzfKmCoEeebQ9fCtwzh9qz80EMC-5Aa4SIzlVf1yFGHox4o\",\n"
                + "            \"size\": {\n"
                + "                \"width\": 360,\n"
                + "                \"height\": 480\n"
                + "            },\n"
                + "            \"file_length\": 258008,\n"
                + "            \"filename\": \"149916630777674.mp4\",\n"
                + "            \"thumb\": \"https://a1.easemob.com/1188170313178478/youhaodongxi/chatfiles/a2acdf20-60a8-11e7-b726-ab0d6e300107\",\n"
                + "            \"type\": \"video\",\n"
                + "            \"url\": \"https://a1.easemob.com/1188170313178478/youhaodongxi/chatfiles/a2eb95d0-60a8-11e7-b6a5-073d17c5638d\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"timestamp\": \"1499166307402\",\n"
                + "    \"to\": \"20814677213185\",\n"
                + "    \"chat_type\": \"GroupChat\"\n"
                + "},\n"
                + "{\n"
                + "    \"chat_type\": \"GroupChat\",\n"
                + "    \"direction\": \"SEND\",\n"
                + "    \"from\": \"lz3\",\n"
                + "    \"msg_id\": \"350476658324539380\",\n"
                + "    \"payload\": {\n"
                + "        \"bodies\": [{\n"
                + "            \"file_length\": \"1.94KB\",\n"
                + "            \"filename\": \"yhdx-4620170704T190822.amr\",\n"
                + "            \"length\": \"2\",\n"
                + "            \"secret\": \"GfcMmmCpEeepRAuL5W3NLlab-3kBku0jcUNleNjnuNxbSoB0\",\n"
                + "            \"type\": \"audio\",\n"
                + "            \"url\": \"https://a1.easemob.com/1188170313178478/youhaodongxi/chatfiles/19f70c90-60a9-11e7-9927-df857dc80880\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"timestamp\": \"1499166505987\",\n"
                + "    \"to\": \"20814677213185\"\n"
                + "},\n"
                + "{\n"
                + "    \"chat_type\": \"GroupChat\",\n"
                + "    \"direction\": \"SEND\",\n"
                + "    \"from\": \"lz4\",\n"
                + "    \"msg_id\": \"350476682890577908\",\n"
                + "    \"payload\": {\n"
                + "        \"bodies\": [{\n"
                + "            \"filename\": \"image-1849353007.jpg\",\n"
                + "            \"secret\": \"HVLhymCpEeeBP8fLp1zGL-bWFjrve4Hclkni9XVDriCrrB2w\",\n"
                + "            \"size\": {\n"
                + "                \"height\": 1263,\n"
                + "                \"width\": 840\n"
                + "            },\n"
                + "            \"type\": \"img\",\n"
                + "            \"url\": \"https://a1.easemob.com/1188170313178478/youhaodongxi/chatfiles/1d52e1c0-60a9-11e7-94a9-071c28363fe3\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"timestamp\": \"1499166511701\",\n"
                + "    \"to\": \"20814677213185\"\n"
                + "},\n"
                + "{\n"
                + "    \"chat_type\": \"GroupChat\",\n"
                + "    \"direction\": \"SEND\",\n"
                + "    \"from\": \"lz5\",\n"
                + "    \"msg_id\": \"350461429712685056\",\n"
                + "    \"payload\": {\n"
                + "        \"bodies\": [{\n"
                + "            \"filename\": \"image269235292.jpg\",\n"
                + "            \"secret\": \"2GwAimCgEeepbS3Xzirr2IlxF_VoO4i_dByB0XMIIDx-UNqG\",\n"
                + "            \"size\": {\n"
                + "                \"height\": 360,\n"
                + "                \"width\": 640\n"
                + "            },\n"
                + "            \"type\": \"img\",\n"
                + "            \"url\": \"https://a1.easemob.com/1188170313178478/youhaodongxi/chatfiles/d86c0080-60a0-11e7-ade1-6dfb6ad635c4\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"timestamp\": \"1499162960283\",\n"
                + "    \"to\": \"20814677213185\"\n"
                + "},\n"
                + "{\n"
                + "    \"from\": \"lz6\",\n"
                + "    \"msg_id\": \"350445529139775524\",\n"
                + "    \"payload\": {\n"
                + "        \"ext\": {},\n"
                + "        \"bodies\": [{\n"
                + "            \"msg\": \"忒MSN\",\n"
                + "            \"type\": \"txt\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"timestamp\": \"1499159258091\",\n"
                + "    \"to\": \"20814677213185\",\n"
                + "    \"chat_type\": \"GroupChat\"\n"
                + "},\n"
                + "{\n"
                + "    \"from\": \"lz7\",\n"
                + "    \"msg_id\": \"350445455370360868\",\n"
                + "    \"payload\": {\n"
                + "        \"ext\": {},\n"
                + "        \"bodies\": [{\n"
                + "            \"msg\": \"测试\",\n"
                + "            \"type\": \"txt\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"timestamp\": \"1499159240911\",\n"
                + "    \"to\": \"20814677213185\",\n"
                + "    \"chat_type\": \"GroupChat\"\n"
                + "}]";
        List<EMMessage> messageList = new ArrayList<EMMessage>();
        try {
            JSONArray jsonArray = new JSONArray(msgJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                long timestamp = jsonObject.optLong("timestamp");
                String from = jsonObject.optString("from");
                String to = jsonObject.optString("to");
                String msgId = jsonObject.optString("msg_id");

                JSONObject bodyObject = jsonObject.optJSONObject("payload").optJSONArray("bodies").getJSONObject(0);
                String type = bodyObject.optString("type");
                EMMessage message = null;
                if (type.equals("txt")) {
                    message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                    EMTextMessageBody body = new EMTextMessageBody(bodyObject.optString("msg"));
                    message.addBody(body);
                } else if (type.equals("video")) {
                    message = EMMessage.createReceiveMessage(EMMessage.Type.VIDEO);
                    EMVideoMessageBody body = new EMVideoMessageBody();
                    body.setThumbnailUrl(bodyObject.optString("thumb"));
                    body.setThumbnailSecret(bodyObject.optString("thumb_secret"));
                    body.setRemoteUrl(bodyObject.optString("url"));
                    body.setVideoFileLength(bodyObject.optLong("file_length"));
                    body.setSecret(bodyObject.optString("secret"));
                    message.addBody(body);
                } else if (type.equals("audio")) {
                    message = EMMessage.createReceiveMessage(EMMessage.Type.VOICE);
                    File file = new File("");
                    EMVoiceMessageBody body = new EMVoiceMessageBody(file, bodyObject.optInt("length"));
                    body.setRemoteUrl(bodyObject.optString("url"));
                    body.setSecret(bodyObject.optString("secret"));
                    body.setFileName(bodyObject.optString("filename"));
                    message.addBody(body);
                } else if (type.equals("img")) {
                    message = EMMessage.createReceiveMessage(EMMessage.Type.IMAGE);
                    File file = new File("");
                    // 这里使用反射获取 ImageBody，为了设置 size
                    Class<?> bodyClass = Class.forName("com.hyphenate.chat.EMImageMessageBody");
                    Class<?>[] parTypes = new Class<?>[1];
                    parTypes[0] = File.class;
                    Constructor<?> constructor = bodyClass.getDeclaredConstructor(parTypes);
                    Object[] pars = new Object[1];
                    pars[0] = file;
                    EMImageMessageBody body = (EMImageMessageBody) constructor.newInstance(pars);
                    Method setSize = Class.forName("com.hyphenate.chat.EMImageMessageBody")
                            .getDeclaredMethod("setSize", int.class, int.class);
                    setSize.setAccessible(true);
                    int width = bodyObject.optJSONObject("size").optInt("width");
                    int height = bodyObject.optJSONObject("size").optInt("height");
                    setSize.invoke(body, width, height);

                    body.setFileName(bodyObject.optString("filename"));
                    body.setSecret(bodyObject.optString("secret"));
                    body.setRemoteUrl(bodyObject.optString("url"));
                    body.setThumbnailUrl(bodyObject.optString("thumb"));
                    message.addBody(body);
                }
                message.setFrom(from);
                message.setTo(to);
                message.setMsgTime(timestamp);
                message.setLocalTime(timestamp);
                message.setMsgId(msgId);
                message.setChatType(EMMessage.ChatType.GroupChat);
                message.setStatus(EMMessage.Status.SUCCESS);
                messageList.add(message);
            }

            VMLog.d("conversation 1- count: %d", EMClient.getInstance().chatManager().getAllConversations().size());
            EMClient.getInstance().chatManager().importMessages(messageList);
            VMLog.d("conversation 2- count: %d", EMClient.getInstance().chatManager().getAllConversations().size());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载更多消息
     */
    private void loadMoreMessage() {
        EMConversation conversation = EMClient.getInstance()
                .chatManager()
                .getConversation("20814677213185", EMConversation.EMConversationType.GroupChat, true);
        String msgId = conversation.getAllMessages().get(0).getMsgId();
        List<EMMessage> list = conversation.loadMoreMsgFromDB(msgId, 20);
        VMLog.d("load more message result %d, all msg count %d", list.size(), conversation.getAllMsgCount());
    }

    /**
     * 保存一条消息到本地
     */
    private void saveMessage() {
        EMMessage textMessage = EMMessage.createSendMessage(EMMessage.Type.TXT);
        textMessage.setChatType(EMMessage.ChatType.Chat);
        textMessage.setFrom("lz0");
        textMessage.setTo("lz1");
        textMessage.setStatus(EMMessage.Status.SUCCESS);
        textMessage.setUnread(true);
        EMTextMessageBody body = new EMTextMessageBody("test save message");
        textMessage.addBody(body);

        textMessage.setAttribute("key1", "value");
        textMessage.setAttribute("key2", false);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key1", "value1");
            jsonObject.put("key2", true);
            jsonObject.put("key3", 100);
            textMessage.setAttribute("json", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EMClient.getInstance().chatManager().saveMessage(textMessage);
    }
}

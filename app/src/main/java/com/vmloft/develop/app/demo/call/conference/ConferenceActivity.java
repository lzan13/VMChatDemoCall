package com.vmloft.develop.app.demo.call.conference;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.conference.EMConference;
import com.hyphenate.chat.conference.EMConferenceListener;
import com.hyphenate.chat.conference.EMConferenceStream;
import com.hyphenate.chat.conference.EMConferenceStreamConfig;
import com.hyphenate.exceptions.HyphenateException;
import com.vmloft.develop.app.demo.call.R;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.widget.VMViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzan13 on 2017/8/15.
 * 多人音视频会议界面
 */
public class ConferenceActivity extends VMActivity implements EMConferenceListener {

    private EMConferenceListener conferenceListener;

    private AudioManager audioManager;

    // 会议实体类对象，保存了会议的一些简单信息
    private EMConference conference;
    private EMConferenceStreamConfig config;
    private boolean isCreator = false;

    // 屏幕宽度，为了处理画面展示大小
    private int screenWidth;

    private List<EMConferenceStream> streamList = new ArrayList<>();

    private ConferenceMemberView localView;
    @BindView(R.id.surface_view_group) VMViewGroup callConferenceViewGroup;

    // 使用 ButterKnife 注解的方式获取控件
    @BindView(R.id.layout_root) View rootView;
    @BindView(R.id.layout_call_control) View controlLayout;
    @BindView(R.id.layout_surface_container) RelativeLayout surfaceLayout;

    @BindView(R.id.btn_exit_full_screen) ImageButton exitFullScreenBtn;
    @BindView(R.id.text_call_time) TextView callTimeView;
    @BindView(R.id.btn_mic_switch) ImageButton micSwitch;
    @BindView(R.id.btn_camera_switch) ImageButton cameraSwitch;
    @BindView(R.id.btn_speaker_switch) ImageButton speakerSwitch;
    @BindView(R.id.btn_change_camera_switch) ImageButton changeCameraSwitch;
    @BindView(R.id.btn_cancel) FloatingActionButton cancelBtn;
    @BindView(R.id.btn_exit) FloatingActionButton exitBtn;
    @BindView(R.id.btn_add) FloatingActionButton addBtn;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        ButterKnife.bind(activity);

        init();

        initConferenceViewGroup();
    }

    /**
     * 初始化
     */
    private void init() {
        conferenceListener = this;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        String confId = getIntent().getStringExtra("confId");
        String password = getIntent().getStringExtra("password");
        conference = new EMConference();
        conference.setConferenceId(confId);
        conference.setPassword(password);

        config = new EMConferenceStreamConfig();
        // 默认开启扬声器
        speakerSwitch.setActivated(true);
        openSpeaker();

        isCreator = getIntent().getBooleanExtra("isCreator", false);
        if (isCreator) {
            creatorCreateConference();
            cancelBtn.setVisibility(View.GONE);
            addBtn.setVisibility(View.GONE);
        } else {
            memberGetConference();
            exitBtn.setVisibility(View.GONE);
        }
    }

    @OnClick({
            R.id.btn_exit_full_screen, R.id.btn_invite_join, R.id.btn_mic_switch, R.id.btn_speaker_switch, R.id.btn_camera_switch,
            R.id.btn_change_camera_switch, R.id.btn_cancel, R.id.btn_exit, R.id.btn_add
    }) void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_exit_full_screen:
                Toast.makeText(activity, "暂未实现最小化", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_invite_join:
                inviteUserToJoinConference();
                break;
            case R.id.btn_mic_switch:
                voiceSwitch();
                break;
            case R.id.btn_speaker_switch:
                if (speakerSwitch.isActivated()) {
                    closeSpeaker();
                } else {
                    openSpeaker();
                }
                break;
            case R.id.btn_camera_switch:
                videoSwitch();
                break;
            case R.id.btn_change_camera_switch:
                changeCamera();
                break;
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_exit:
                exitConference();
                break;
            case R.id.btn_add:
                joinConference();
                break;
        }
    }

    /**
     * 初始化多人音视频画面管理控件
     */
    private void initConferenceViewGroup() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(0, 0);
        lp.width = screenWidth;
        lp.height = screenWidth;
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        callConferenceViewGroup.setLayoutParams(lp);

        localView = new ConferenceMemberView(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        params.width = screenWidth;
        params.height = screenWidth;
        localView.setLayoutParams(params);
        localView.updateVideoState(config.isVideoOff());
        localView.updateMuteState(config.isMute());
        localView.setPubOrSub(false);
        callConferenceViewGroup.addView(localView);
        EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView.getSurfaceView());
        localView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (localView.isPubOrSub()) {
                    unpublish();
                } else {
                    publish();
                }
            }
        });
    }

    /**
     * 添加一个展示远端画面的 view
     */
    private void addConferenceView() {
        final ConferenceMemberView memberView = new ConferenceMemberView(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        params.width = screenWidth / 3;
        params.height = screenWidth / 3;
        memberView.setLayoutParams(params);
        callConferenceViewGroup.addView(memberView);
        memberView.setPubOrSub(false);
        //设置 view 点击监听
        memberView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                int index = callConferenceViewGroup.indexOfChild(view);
                final EMConferenceStream stream = streamList.get(index - 1);
                if (memberView.isPubOrSub()) {
                    unsubscribe(stream, memberView);
                } else {
                    subscribe(stream, memberView);
                }
            }
        });
    }

    /**
     * 移除指定位置的 View，移除时如果已经订阅需要取消订阅
     */
    private void removeConferenceView(EMConferenceStream stream) {
        int index = streamList.indexOf(stream);
        final ConferenceMemberView memberView = (ConferenceMemberView) callConferenceViewGroup.getChildAt(index + 1);
        //if (memberView.isPubOrSub()) {
        //    // 已订阅就取消订阅
        //    EMClient.getInstance().conferenceManager().asyncUnsubscribe(stream, null);
        //}
        streamList.remove(stream);
        callConferenceViewGroup.removeView(memberView);
    }

    /**
     * 更新指定 View
     */
    private void updateConferenceMemberView(EMConferenceStream stream) {
        int position = streamList.indexOf(stream);
        ConferenceMemberView conferenceMemberView = (ConferenceMemberView) callConferenceViewGroup.getChildAt(position + 1);
        conferenceMemberView.updateMuteState(stream.isAudioOff());
        conferenceMemberView.updateVideoState(stream.isVideoOff());
        int memberViewSize;
        if (streamList.size() > 8) {
            memberViewSize = screenWidth / 4;
        } else if (streamList.size() > 3) {
            memberViewSize = screenWidth / 3;
        } else if (streamList.size() >= 1) {
            memberViewSize = screenWidth / 2;
        } else {
            memberViewSize = screenWidth;
        }
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(0, 0);
        lp.width = memberViewSize;
        lp.height = memberViewSize;
        for (int i = 0; i < callConferenceViewGroup.getChildCount(); i++) {
            ConferenceMemberView view = (ConferenceMemberView) callConferenceViewGroup.getChildAt(i);
            view.setLayoutParams(lp);
        }
    }

    /**
     * 作为会议创建者创建会议
     */
    private void creatorCreateConference() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    // 创建者需要先创建会议，得到 ticket，然后邀请其他人加入
                    conference = EMClient.getInstance().conferenceManager().createConference(conference.getPassword());
                    joinConference();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 作为成员获取会议信息
     */
    private void memberGetConference() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    // 成员加入前需要根据收到的 ConferenceId 和 Password 获取
                    EMConference temp = EMClient.getInstance()
                            .conferenceManager()
                            .getConference(conference.getConferenceId(), conference.getPassword());
                    // 这里因为外部可能已经设置了 conference 其他信息，所以没有直接 =
                    conference.setTicket(temp.getTicket());
                    conference.setConferenceId(temp.getConferenceId());
                    conference.setPassword(temp.getPassword());
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 加入会议，不论创建者还是其他成员，都需要主动去调用加入会议方法
     */
    private void joinConference() {
        cancelBtn.setVisibility(View.GONE);
        exitBtn.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.GONE);
        try {
            EMClient.getInstance().conferenceManager().joinConference(conference, config);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 邀请用户加入会议
     */
    private void inviteUserToJoinConference() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("邀请加入会议");
        final EditText editText = new EditText(activity);
        editText.setHint("Input username");
        alertDialogBuilder.setView(editText);
        alertDialogBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                    Toast.makeText(activity, "输入不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                final String username = editText.getText().toString();
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            EMClient.getInstance()
                                    .conferenceManager()
                                    .inviteUserToJoinConference(conference.getConferenceId(), conference.getPassword(), username,
                                            "{'extension':'invite ext'}");
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.show();
    }

    /**
     * 退出会议
     */
    private void exitConference() {
        try {
            EMClient.getInstance().conferenceManager().exitConference();
            finish();
        } catch (HyphenateException e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    /**
     * 开始推自己的数据
     */
    private void publish() {
        try {
            EMClient.getInstance().conferenceManager().publish(config);
            localView.setPubOrSub(true);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止推自己的数据
     */
    private void unpublish() {
        try {
            EMClient.getInstance().conferenceManager().unpublish();
            localView.setPubOrSub(false);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(final EMConferenceStream stream, final ConferenceMemberView memberView) {
        // 未订阅就去订阅
        try {
            EMClient.getInstance().conferenceManager().subscribe(stream, memberView.getSurfaceView());
            memberView.setPubOrSub(true);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe(EMConferenceStream stream, final ConferenceMemberView memberView) {
        // 已订阅就取消订阅
        try {
            EMClient.getInstance().conferenceManager().unsubscribe(stream);
            memberView.setPubOrSub(false);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开扬声器
     * 主要是通过扬声器的开关以及设置音频播放模式来实现
     * 1、MODE_NORMAL：是正常模式，一般用于外放音频
     * 2、MODE_IN_CALL：
     * 3、MODE_IN_COMMUNICATION：这个和 CALL 都表示通讯模式，不过 CALL 在华为上不好使，故使用 COMMUNICATION
     * 4、MODE_RINGTONE：铃声模式
     */
    public void openSpeaker() {
        // 检查是否已经开启扬声器
        if (!audioManager.isSpeakerphoneOn()) {
            // 打开扬声器
            audioManager.setSpeakerphoneOn(true);
        }
        // 开启了扬声器之后，因为是进行通话，声音的模式也要设置成通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        speakerSwitch.setActivated(true);
    }

    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    public void closeSpeaker() {
        // 检查是否已经开启扬声器
        if (audioManager.isSpeakerphoneOn()) {
            // 关闭扬声器
            audioManager.setSpeakerphoneOn(false);
        }
        // 设置声音模式为通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        speakerSwitch.setActivated(false);
    }

    /**
     * 语音开关
     */
    private void voiceSwitch() {
        if (config.isMute()) {
            config.setMute(false);
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
        } else {
            config.setMute(true);
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
        }
        micSwitch.setActivated(config.isMute());
        localView.updateMuteState(config.isMute());
    }

    /**
     * 视频开关
     */
    private void videoSwitch() {
        if (config.isVideoOff()) {
            config.setVideoOff(false);
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        } else {
            config.setVideoOff(true);
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
        }
        cameraSwitch.setActivated(config.isVideoOff());
        localView.updateVideoState(config.isVideoOff());
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        if (EMClient.getInstance().conferenceManager().getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            changeCameraSwitch.setImageResource(R.drawable.ic_camera_rear_white_24dp);
        } else {
            changeCameraSwitch.setImageResource(R.drawable.ic_camera_front_white_24dp);
        }
        EMClient.getInstance().conferenceManager().switchCamera();
    }

    @Override protected void onStart() {
        super.onStart();
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);
    }

    @Override protected void onStop() {
        super.onStop();
        EMClient.getInstance().conferenceManager().removeConferenceListener(conferenceListener);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        exitConference();
    }

    /**
     * --------------------------------------------------------------------
     * 多人音视频会议回调方法
     */

    @Override public void onMemberJoined(final String username) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, username + " joined conference!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onMemberExited(final String username) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, username + " removed conference!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onStreamAdded(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, stream.getUsername() + " stream add!", Toast.LENGTH_SHORT).show();
                streamList.add(stream);
                addConferenceView();
                updateConferenceMemberView(stream);
            }
        });
    }

    @Override public void onStreamRemoved(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, stream.getUsername() + " stream removed!", Toast.LENGTH_SHORT).show();
                if (streamList.contains(stream)) {
                    removeConferenceView(stream);
                    updateConferenceMemberView(stream);
                }
            }
        });
    }

    @Override public void onStreamUpdate(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, stream.getUsername() + " stream update!", Toast.LENGTH_SHORT).show();
                updateConferenceMemberView(stream);
            }
        });
    }

    @Override public void onPassiveLeave(final int error, final String message) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, "Passive exit " + error + ", message" + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onState(final ConferenceState state, final String confId, final Object object) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                if (state == ConferenceState.STATE_PUBLISH_SETUP) {
                    localView.setPubOrSub(true);
                }
                Toast.makeText(activity, "State=" + state + ", confId=" + confId + ", object=" + object, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * 收到其他人的会议邀请
     *
     * @param confId 会议 id
     * @param password 会议密码
     * @param extension 邀请扩展内容
     */
    @Override public void onReceiveInvite(final String confId, String password, String extension) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, "Receive invite " + confId, Toast.LENGTH_LONG).show();
            }
        });
    }
}

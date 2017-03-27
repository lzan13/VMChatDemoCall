package com.vmloft.develop.app.demo.call;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;
import com.superrtc.sdk.VideoView;
import com.vmloft.develop.library.tools.utils.VMLog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lzan13 on 2017/3/27.
 * 音视频通话悬浮窗操作类
 */
public class VMFloatWindow {

    // 上下文菜单
    private Context context;

    // 当前单例类实例
    private static VMFloatWindow instance;

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams layoutParams = null;

    // 悬浮窗需要显示的布局
    private View floatView;
    private TextView callTimeView;

    public VMFloatWindow(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static VMFloatWindow getInstance(Context context) {
        if (instance == null) {
            instance = new VMFloatWindow(context);
        }
        return instance;
    }

    /**
     * 开始展示悬浮窗
     */
    public void startFloatWindow() {
        EventBus.getDefault().register(this);
        layoutParams = new WindowManager.LayoutParams();
        // 位置为右侧顶部
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 设置宽高自适应
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // 设置悬浮窗透明
        layoutParams.format = PixelFormat.TRANSPARENT;

        // 设置窗口类型
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        // 设置窗口标志类型，其中 FLAG_NOT_FOCUSABLE 是放置当前悬浮窗拦截点击事件，造成桌面控件不可操作
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

        // 获取要现实的布局
        floatView = LayoutInflater.from(context).inflate(R.layout.widget_float_window, null);
        // 添加悬浮窗 View 到窗口
        windowManager.addView(floatView, layoutParams);
        if (VMCallManager.getInstance().getCallType() == VMCallManager.CallType.VOICE) {
            floatView.findViewById(R.id.layout_call_voice).setVisibility(View.VISIBLE);
            floatView.findViewById(R.id.layout_call_video).setVisibility(View.GONE);
            callTimeView = (TextView) floatView.findViewById(R.id.text_call_time);
            refreshCallTime();
        } else {
            floatView.findViewById(R.id.layout_call_voice).setVisibility(View.GONE);
            floatView.findViewById(R.id.layout_call_video).setVisibility(View.VISIBLE);
            EMLocalSurfaceView localView =
                    (EMLocalSurfaceView) floatView.findViewById(R.id.surface_view_local);
            EMOppositeSurfaceView oppositeView =
                    (EMOppositeSurfaceView) floatView.findViewById(R.id.surface_view_opposite);
            // 设置本地预览图像显示在最上层
            localView.setZOrderMediaOverlay(true);
            localView.setZOrderOnTop(true);
            // 设置通话画面的填充方式
            localView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
            oppositeView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
            // 将 SurfaceView设置给 SDK
            EMClient.getInstance().callManager().setSurfaceView(localView, oppositeView);
        }

        // 当点击悬浮窗时，返回到通话界面
        floatView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent();
                if (VMCallManager.getInstance().getCallType() == VMCallManager.CallType.VOICE) {
                    intent.setClass(context, VMVoiceCallActivity.class);
                } else {
                    intent.setClass(context, VMVideoCallActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        //设置监听浮动窗口的触摸移动
        floatView.setOnTouchListener(new View.OnTouchListener() {
            boolean result = false;

            float x = 0;
            float y = 0;
            float startX = 0;
            float startY = 0;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        result = false;
                        x = event.getX();
                        y = event.getY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        VMLog.d("start x: %f, y: %f", startX, startY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        VMLog.d("move x: %f, y: %f", event.getRawX(), event.getRawY());
                        // 当移动距离大于特定值时，表示是多动悬浮窗，则不触发后边的点击监听
                        if (Math.abs(event.getRawX() - startX) > 20
                                || Math.abs(event.getRawY() - startY) > 20) {
                            result = true;
                        }
                        // getRawX 获取触摸点相对于屏幕的坐标，getX 相对于当前悬浮窗坐标
                        // 根据当前触摸点 X 坐标计算悬浮窗 X 坐标，
                        layoutParams.x = (int) (event.getRawX() - x);
                        // 根据当前触摸点 Y 坐标计算悬浮窗 Y 坐标，减25为状态栏的高度
                        layoutParams.y = (int) (event.getRawY() - y - 25);
                        // 刷新悬浮窗
                        windowManager.updateViewLayout(floatView, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return result;
            }
        });
    }

    /**
     * 停止悬浮窗
     */
    public void stopFloatWindow() {
        EventBus.getDefault().unregister(this);
        if (windowManager != null && floatView != null) {
            windowManager.removeView(floatView);
            floatView = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) public void onEventBus(VMCallEvent event) {
        if (event.isState()) {
            refreshCallView(event);
        }
        if (event.isTime()) {
            refreshCallTime();
        }
    }

    /**
     * 刷新通话界面
     */
    private void refreshCallView(VMCallEvent event) {
        EMCallStateChangeListener.CallError callError = event.getCallError();
        EMCallStateChangeListener.CallState callState = event.getCallState();
        switch (callState) {
            case CONNECTING: // 正在呼叫对方，TODO 没见回调过
                VMLog.i("正在呼叫对方" + callError);
                break;
            case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
                VMLog.i("正在连接" + callError);
                break;
            case ACCEPTED: // 通话已接通
                VMLog.i("通话已接通");
                break;
            case DISCONNECTED: // 通话已中断
                VMLog.i("通话已结束" + callError);
                VMCallManager.getInstance().removeFloatWindow();
                break;
            // TODO 3.3.0版本 SDK 下边几个暂时都没有回调
            case NETWORK_UNSTABLE:
                if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                    VMLog.i("没有通话数据" + callError);
                } else {
                    VMLog.i("网络不稳定" + callError);
                }
                break;
            case NETWORK_NORMAL:
                VMLog.i("网络正常");
                break;
            case VIDEO_PAUSE:
                VMLog.i("视频传输已暂停");
                break;
            case VIDEO_RESUME:
                VMLog.i("视频传输已恢复");
                break;
            case VOICE_PAUSE:
                VMLog.i("语音传输已暂停");
                break;
            case VOICE_RESUME:
                VMLog.i("语音传输已恢复");
                break;
            default:
                break;
        }
    }

    private void refreshCallTime() {
        int t = VMCallManager.getInstance().getCallTime();
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
}

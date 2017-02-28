package net.melove.app.chat.demo.call;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by lzan13 on 2015/7/4.
 * Activity 的基类，做一些子类公共的工作
 */
public class MLBaseActivity extends AppCompatActivity {

    protected String className = this.getClass().getSimpleName();

    //
    protected Toolbar toolbar;

    // 当前界面的上下文菜单对象
    protected MLBaseActivity activity;

    protected AlertDialog.Builder dialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 封装公共的 Activity 跳转方法
     * 基类定义并实现的方法，为了以后方便扩展
     *
     * @param activity 当前 Activity 对象
     * @param intent 界面跳转 Intent 实例对象
     */
    public void onStartActivity(Activity activity, Intent intent) {
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
    }

    /**
     * 带有共享元素的 Activity 跳转方法
     *
     * @param activity 当前活动 Activity
     * @param intent 跳转意图
     * @param sharedElement 共享元素
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) public void onStartActivity(Activity activity,
            Intent intent, View sharedElement) {
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElement,
                        sharedElement.getTransitionName());
        ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
    }

    /**
     * 带有多个共享元素的 Activity 跳转方法
     *
     * @param activity 当前活动 Activity
     * @param intent 跳转意图
     * @param pairs 共享元素集合
     */
    public void onStartActivity(Activity activity, Intent intent, Pair<View, String>... pairs) {
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairs);
        ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
    }

    /**
     * 自定义返回方法
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP) protected void onFinish() {
        // 根据不同的系统版本选择不同的 finish 方法
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            activity.finish();
        } else {
            ActivityCompat.finishAfterTransition(activity);
        }
    }

    @Override protected void onRestart() {
        super.onRestart();
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}

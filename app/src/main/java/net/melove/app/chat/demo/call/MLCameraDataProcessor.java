package net.melove.app.chat.demo.call;

import android.hardware.Camera;

import com.hyphenate.chat.EMCallManager;

/**
 * Created by lz on 2016/7/9.
 */
public class MLCameraDataProcessor implements EMCallManager.EMCameraDataProcessor {


    @Override
    public void onProcessData(byte[] bytes, Camera camera, int width, int height) {
        int wh = width * height;
        for (int i = 0; i < wh; i++) {
            int d = (bytes[i] & 0xFF);
            d = d < 16 ? 16 : d;
            d = d > 235 ? 235 : d;
            bytes[i] = (byte)d;
        }
    }
}

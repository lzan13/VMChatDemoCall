package com.vmloft.develop.app.demo.call.conference;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hyphenate.chat.conference.EMConferenceStream;
import com.hyphenate.media.EMCallSurfaceView;
import com.superrtc.sdk.VideoView;
import com.vmloft.develop.app.demo.call.R;
import java.util.List;

/**
 * Created by lzan13 on 2017/8/17.
 * 多人会议 UI 界面适配器
 */
public class ConferenceViewAdapter extends RecyclerView.Adapter<ConferenceViewAdapter.ConferenceViewHolder> {

    private Context context;
    private ConferenceCallItemClickListener clickListener;

    private List<EMConferenceStream> streamList;

    public ConferenceViewAdapter(Context context, List<EMConferenceStream> list) {
        this.context = context;
        streamList = list;
    }

    @Override public ConferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_conference_view, null);
        return new ConferenceViewHolder(view);
    }

    @Override public void onBindViewHolder(ConferenceViewHolder holder, final int position) {
        holder.surfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onItemClick(position, view);
                }
            }
        });
    }

    @Override public int getItemCount() {
        return streamList.size();
    }

    /**
     * 设置点击监听接口实现
     */
    public void setConferenceCallItemClickListener(ConferenceCallItemClickListener listener) {
        clickListener = listener;
    }

    /**
     * 定义 Item 点击监听接口
     */
    public interface ConferenceCallItemClickListener {
        public void onItemClick(int position, View view);
    }

    static class ConferenceViewHolder extends RecyclerView.ViewHolder {

        EMCallSurfaceView surfaceView;

        public ConferenceViewHolder(View itemView) {
            super(itemView);
            surfaceView = (EMCallSurfaceView) itemView.findViewById(R.id.item_surface_view);
        }
    }
}

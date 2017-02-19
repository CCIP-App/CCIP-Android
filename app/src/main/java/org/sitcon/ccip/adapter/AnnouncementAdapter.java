package org.sitcon.ccip.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sitcon.ccip.R;
import org.sitcon.ccip.model.Announcement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<Announcement> announcementList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView msg, time;

        public ViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.invalid_token_msg);
            time = (TextView) itemView.findViewById(R.id.time);
        }
    }

    public AnnouncementAdapter(Context context, List<Announcement> programSlotList) {
        mContext = context;
        announcementList = programSlotList;

        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);

        itemView.setBackgroundResource(mBackground);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        final Announcement announcement = announcementList.get(position);

        if (mContext.getResources().getConfiguration().locale.getLanguage().startsWith("zh")) {
            holder.msg.setText(announcement.getMsgZh());
        } else {
            holder.msg.setText(announcement.getMsgEn());
        }
        holder.time.setText(sdf.format(new Date(announcement.getDatetime() * 1000L)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (announcement.getUri() != null) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(announcement.getUri())));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }
}

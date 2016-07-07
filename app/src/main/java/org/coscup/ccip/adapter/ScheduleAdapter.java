package org.coscup.ccip.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;

import java.text.SimpleDateFormat;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<List<Program>> mProgramSlotList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView startTimeText;

        public ViewHolder(View itemView) {
            super(itemView);
            startTimeText = (TextView) itemView.findViewById(R.id.start_time);
        }
    }

    public ScheduleAdapter(Context context, List<List<Program>> programSlotList) {
        mContext = context;
        mProgramSlotList = programSlotList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

        final List<Program> programs = mProgramSlotList.get(position);
        holder.startTimeText.setText(programs.get(0).getStarttime());
    }

    @Override
    public int getItemCount() {
        return mProgramSlotList.size();
    }
}

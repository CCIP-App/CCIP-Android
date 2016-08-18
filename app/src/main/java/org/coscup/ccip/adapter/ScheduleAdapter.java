package org.coscup.ccip.adapter;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd HH:mm");

    private Context mContext;
    private List<List<Program>> mProgramSlotList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView startTimeText;
        private RecyclerView programView;

        public ViewHolder(View itemView) {
            super(itemView);
            startTimeText = (TextView) itemView.findViewById(R.id.start_time);
            programView = (RecyclerView) itemView.findViewById(R.id.programs);
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

        holder.programView.setLayoutManager(new LinearLayoutManager(mContext));
        holder.programView.setItemAnimator(new DefaultItemAnimator());

        final List<Program> programs = mProgramSlotList.get(position);
        try {
            Date date = ISO8601Utils.parse(programs.get(0).getStarttime(), new ParsePosition(0));
            holder.startTimeText.setText(SDF.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.programView.setAdapter(new ProgramAdapter(mContext, programs));
    }

    @Override
    public int getItemCount() {
        return mProgramSlotList.size();
    }
}

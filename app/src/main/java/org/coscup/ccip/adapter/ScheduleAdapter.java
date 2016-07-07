package org.coscup.ccip.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.coscup.ccip.CountdownActivity;
import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;
import org.coscup.ccip.model.Scenario;

import java.text.SimpleDateFormat;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<List<Program>> mProgramSlotList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView scenarioName, status, allowTimeRange, disableReason;
        public CardView card;

        public ViewHolder(View itemView) {
            super(itemView);
            scenarioName = (TextView) itemView.findViewById(R.id.scenario_name);
            status = (TextView) itemView.findViewById(R.id.status);
            allowTimeRange = (TextView) itemView.findViewById(R.id.allow_time_range);
            disableReason = (TextView) itemView.findViewById(R.id.disable_reason);
            card = (CardView) itemView.findViewById(R.id.card);
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
        holder.scenarioName.setText(programs.get(0).getStarttime());
    }

    public void startCountdownActivity(Scenario scenario) {
        Gson gson = new Gson();
        Intent intent = new Intent();
        intent.setClass(mContext, CountdownActivity.class);
        intent.putExtra(CountdownActivity.INTENT_EXTRA_SCENARIO, gson.toJson(scenario));
        mContext.startActivity(intent);
    }

    private void setCardUnclickable(CardView card) {
        card.setClickable(false);
        card.setOnClickListener(null);
        card.setCardBackgroundColor(Color.LTGRAY);
    }

    @Override
    public int getItemCount() {
        return mProgramSlotList.size();
    }
}

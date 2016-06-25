package org.coscup.ccip.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Scenario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScenarioAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<Scenario> mScenarioList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView scenarioName, status, allowTimeRange, disableReason;

        public ViewHolder(View itemView) {
            super(itemView);
            scenarioName = (TextView) itemView.findViewById(R.id.scenario_name);
            status = (TextView) itemView.findViewById(R.id.status);
            allowTimeRange = (TextView) itemView.findViewById(R.id.allow_time_range);
            disableReason = (TextView) itemView.findViewById(R.id.disable_reason);
        }
    }

    public ScenarioAdapter(Context context, List<Scenario> scenarioList) {
        mContext = context;
        mScenarioList = scenarioList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scenario_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = ((ViewHolder) viewHolder);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

        Scenario scenario = mScenarioList.get(position);
        holder.scenarioName.setText(scenario.getId());

        if (scenario.getUsed() == null) {
            holder.status.setText("Unused");
        } else {
            holder.status.setText(sdf.format(new Date(scenario.getAvailableTime() * 1000L)));
        }

        sdf = new SimpleDateFormat("MM/dd HH:mm");
        StringBuffer timeRange = new StringBuffer();
        timeRange.append(sdf.format(new Date(scenario.getAvailableTime() * 1000L)));
        timeRange.append(" ~ ");
        timeRange.append(sdf.format(new Date(scenario.getExpireTime() * 1000L)));
        holder.allowTimeRange.setText(timeRange);
    }

    @Override
    public int getItemCount() {
        return mScenarioList.size();
    }
}

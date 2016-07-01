package org.coscup.ccip.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.model.Scenario;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.util.TokenUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScenarioAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<Scenario> mScenarioList;

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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

        final Scenario scenario = mScenarioList.get(position);
        holder.scenarioName.setText(scenario.getId());

        if (scenario.getUsed() == null) {
            holder.status.setText("Unused");
        } else {
            holder.status.setText(sdf.format(new Date(scenario.getUsed() * 1000L)));
        }

        sdf = new SimpleDateFormat("MM/dd HH:mm");
        StringBuffer timeRange = new StringBuffer();
        timeRange.append(sdf.format(new Date(scenario.getAvailableTime() * 1000L)));
        timeRange.append(" ~ ");
        timeRange.append(sdf.format(new Date(scenario.getExpireTime() * 1000L)));
        holder.allowTimeRange.setText(timeRange);

        if (scenario.getDisabled() != null) {
            holder.allowTimeRange.setVisibility(View.GONE);
            holder.disableReason.setVisibility(View.VISIBLE);
            holder.disableReason.setText(scenario.getDisabled());
            return;
        }

        if (scenario.getUsed() == null) {
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Call<Attendee> attendeeCall = CCIPClient.get().use(scenario.getId(), TokenUtil.getToken(mContext));
                    attendeeCall.enqueue(new Callback<Attendee>() {
                        @Override
                        public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                            if (response.isSuccessful()) {
                                Attendee attendee = response.body();
                                mScenarioList = attendee.getScenarios();
                                notifyDataSetChanged();
                                holder.card.setOnClickListener(null);
                            } else {
                                Toast.makeText(mContext, "Already used or expire", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Attendee> call, Throwable t) {
                            Toast.makeText(mContext, "Use req fail, " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mScenarioList.size();
    }
}

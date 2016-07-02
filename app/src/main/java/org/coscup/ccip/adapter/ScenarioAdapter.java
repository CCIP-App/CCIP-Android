package org.coscup.ccip.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.coscup.ccip.CountdownActivity;
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
        holder.scenarioName.setText(mContext.getResources().getIdentifier(scenario.getId(), "string", mContext.getPackageName()));

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
            setCardUnclickable(holder.card);
            return;
        }

        if (scenario.getUsed() == null) {
            holder.card.setClickable(true);
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (scenario.getCountdown() > 0) {
                        showConfirmDialog(scenario, holder);
                    } else {
                        use(scenario, holder);
                    }
                }
            });
        } else {
            if (new Date().getTime() / 1000 - scenario.getUsed() < scenario.getCountdown()) {
                holder.card.setClickable(true);
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startCountdownActivity(scenario);
                    }
                });
            } else {
                setCardUnclickable(holder.card);
            }
        }
    }

    public void showConfirmDialog(final Scenario scenario, final ViewHolder holder) {
        new AlertDialog.Builder(mContext)
                .setTitle("按下去就悲劇囉")
                .setPositiveButton("好啦", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        use(scenario, holder);
                        startCountdownActivity(scenario);
                    }
                })
                .setNegativeButton("嗚嗚 我後悔了", null)
                .show();
    }

    public void startCountdownActivity(Scenario scenario) {
        Gson gson = new Gson();
        Intent intent = new Intent();
        intent.setClass(mContext, CountdownActivity.class);
        intent.putExtra(CountdownActivity.INTENT_EXTRA_SCENARIO, gson.toJson(scenario));
        mContext.startActivity(intent);
    }

    public void use(Scenario scenario, final ViewHolder holder) {
        Call<Attendee> attendeeCall = CCIPClient.get().use(scenario.getId(), TokenUtil.getToken(mContext));
        attendeeCall.enqueue(new Callback<Attendee>() {
            @Override
            public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                if (response.isSuccessful()) {
                    Attendee attendee = response.body();
                    mScenarioList = attendee.getScenarios();
                    notifyDataSetChanged();
                    setCardUnclickable(holder.card);
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

    private void setCardUnclickable(CardView card) {
        card.setClickable(false);
        card.setOnClickListener(null);
        card.setCardBackgroundColor(Color.LTGRAY);
    }

    @Override
    public int getItemCount() {
        return mScenarioList.size();
    }
}

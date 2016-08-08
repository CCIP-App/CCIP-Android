package org.coscup.ccip.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.coscup.ccip.activity.CountdownActivity;
import org.coscup.ccip.R;
import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.model.Error;
import org.coscup.ccip.model.Scenario;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.network.ErrorUtil;
import org.coscup.ccip.util.PreferenceUtil;

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

        public ImageView scenarioIcon, tickIcon;
        public TextView scenarioName, allowTimeRange;
        public CardView card;

        public ViewHolder(View itemView) {
            super(itemView);
            scenarioIcon = (ImageView) itemView.findViewById(R.id.icon);
            tickIcon = (ImageView) itemView.findViewById(R.id.tick);
            scenarioName = (TextView) itemView.findViewById(R.id.scenario_name);
            allowTimeRange = (TextView) itemView.findViewById(R.id.allow_time_range);
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
                .inflate(R.layout.item_scenario, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

        final Scenario scenario = mScenarioList.get(position);
        int iconResId = mContext.getResources().getIdentifier(scenario.getId().indexOf("lunch") > 0 ? "lunch" : scenario.getId(), "drawable", mContext.getPackageName());
        holder.scenarioIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconResId));
        holder.scenarioName.setText(mContext.getResources().getIdentifier(scenario.getId(), "string", mContext.getPackageName()));

        sdf = new SimpleDateFormat("MM/dd HH:mm");
        StringBuffer timeRange = new StringBuffer();
        timeRange.append(sdf.format(new Date(scenario.getAvailableTime() * 1000L)));
        timeRange.append(" ~ ");
        timeRange.append(sdf.format(new Date(scenario.getExpireTime() * 1000L)));
        holder.allowTimeRange.setText(timeRange);

        if (scenario.getDisabled() != null) {
            holder.allowTimeRange.setVisibility(View.GONE);
            setCardUsed(holder);
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
                setCardUsed(holder);
            }
        }
    }

    public void showConfirmDialog(final Scenario scenario, final ViewHolder holder) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.confirm_dialog_title)
                .setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        use(scenario, holder);
                    }
                })
                .setNegativeButton(R.string.negative_button, null)
                .show();
    }

    public void startCountdownActivity(Scenario scenario) {
        Gson gson = new Gson();
        Intent intent = new Intent();
        intent.setClass(mContext, CountdownActivity.class);
        intent.putExtra(CountdownActivity.INTENT_EXTRA_SCENARIO, gson.toJson(scenario));
        mContext.startActivity(intent);
    }

    public void use(final Scenario scenario, final ViewHolder holder) {
        Call<Attendee> attendeeCall = CCIPClient.get().use(scenario.getId(), PreferenceUtil.getToken(mContext));
        attendeeCall.enqueue(new Callback<Attendee>() {
            @Override
            public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                if (response.isSuccessful()) {
                    Attendee attendee = response.body();
                    mScenarioList = attendee.getScenarios();
                    notifyDataSetChanged();
                    setCardUsed(holder);

                    if (scenario.getCountdown() > 0) {
                        startCountdownActivity(scenario);
                    }
                } else {
                    if (response.code() == 400) {
                        Error error = ErrorUtil.parseError(response);
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                    } else if (response.code() == 403) {
                        new AlertDialog.Builder(mContext)
                                .setTitle(R.string.connect_to_coscup_wifi)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    } else {
                        Toast.makeText(mContext, "Unexpected response", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Attendee> call, Throwable t) {
                Toast.makeText(mContext, "Use req fail, " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCardUsed(ViewHolder holder) {
        holder.card.setClickable(false);
        holder.card.setOnClickListener(null);
        holder.tickIcon.setVisibility(View.VISIBLE);
        holder.scenarioIcon.setAlpha(0.4f);
        holder.scenarioName.setTextColor(Color.parseColor("#FF9B9B9B"));
    }

    @Override
    public int getItemCount() {
        return mScenarioList.size();
    }
}

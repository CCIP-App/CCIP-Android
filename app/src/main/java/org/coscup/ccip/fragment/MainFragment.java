package org.coscup.ccip.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.onesignal.OneSignal;

import org.coscup.ccip.R;
import org.coscup.ccip.activity.MainActivity;
import org.coscup.ccip.adapter.ScenarioAdapter;
import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.util.PreferenceUtil;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends TrackFragment {

    private Activity mActivity;
    View notCOSCUPWifiView;
    TextView msg;
    RecyclerView scenarioView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        notCOSCUPWifiView = view.findViewById(R.id.not_coscup_wifi);
        msg = (TextView) view.findViewById(R.id.msg);
        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(mActivity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt(getString(R.string.scan_kktix_qrcode));
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
        scenarioView = (RecyclerView) view.findViewById(R.id.scenarios);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        mActivity = getActivity();
        scenarioView.setLayoutManager(new LinearLayoutManager(mActivity));
        scenarioView.setItemAnimator(new DefaultItemAnimator());

        if (mActivity.getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            String token = mActivity.getIntent().getData().getQueryParameter("token");
            PreferenceUtil.setIsNewToken(mActivity, true);
            PreferenceUtil.setToken(mActivity, token);

            JSONObject tags = new JSONObject();
            try {
                tags.put("token", token);
                OneSignal.sendTags(tags);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (PreferenceUtil.getToken(mActivity) == null) {
            msg.setVisibility(View.VISIBLE);
            msg.setText(R.string.open_via_link);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateStatus();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    void updateStatus() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        msg.setVisibility(View.GONE);
        Call<Attendee> attendee = CCIPClient.get().status(PreferenceUtil.getToken(mActivity));
        attendee.enqueue(new Callback<Attendee>() {
            @Override
            public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    Attendee attendee = response.body();
                    JsonObject attr = attendee.getAttr().getAsJsonObject();

                    if (PreferenceUtil.getIsNewToken(mActivity)) {
                        PreferenceUtil.setIsNewToken(mActivity, false);
                        new AlertDialog.Builder(mActivity)
                                .setMessage(mActivity.getString(R.string.hi)
                                        + attendee.getUserId()
                                        + mActivity.getString(R.string.login_success))
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }

                    JsonElement attrTitle = attr.get("title");
                    if (attrTitle != null) {
                        MainActivity.setUserTitle(attrTitle.getAsString());
                    }
                    MainActivity.setUserId(attendee.getUserId());

                    scenarioView.setAdapter(new ScenarioAdapter(mActivity, attendee.getScenarios()));
                } else if (response.code() == 403) {
                    swipeRefreshLayout.setRefreshing(false);
                    notCOSCUPWifiView.setVisibility(View.VISIBLE);
                    notCOSCUPWifiView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            swipeRefreshLayout.setRefreshing(true);
                            notCOSCUPWifiView.setVisibility(View.GONE);
                            updateStatus();
                        }
                    });
                }
                else {
                    Toast.makeText(mActivity, "invalid token", Toast.LENGTH_LONG).show();
                    msg.setText(getString(R.string.open_via_link));
                    msg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Attendee> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(mActivity, R.string.offline, Toast.LENGTH_LONG).show();
            }
        });
    }
}

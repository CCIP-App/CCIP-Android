package org.pycontw.ccip.fragment;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.pycontw.ccip.R;
import org.pycontw.ccip.activity.MainActivity;
import org.pycontw.ccip.adapter.ScenarioAdapter;
import org.pycontw.ccip.model.Attendee;
import org.pycontw.ccip.network.CCIPClient;
import org.pycontw.ccip.util.PreferenceUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends TrackFragment {

    private Activity mActivity;
    View notCOSCUPWifiView;
    TextView invalidTokenMsg;
    RecyclerView scenarioView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        notCOSCUPWifiView = view.findViewById(R.id.not_conf_wifi);
        invalidTokenMsg = (TextView) view.findViewById(R.id.invalid_token_msg);
        invalidTokenMsg.setOnClickListener(new View.OnClickListener() {
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

            if (token != null) {
                PreferenceUtil.setIsNewToken(mActivity, true);
                PreferenceUtil.setToken(mActivity, token);
            }
        }

        if (PreferenceUtil.getToken(mActivity) == null) {
            invalidTokenMsg.setVisibility(View.VISIBLE);
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
        if (PreferenceUtil.getToken(mActivity) == null) {
            invalidTokenMsg.setVisibility(View.VISIBLE);
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        invalidTokenMsg.setVisibility(View.GONE);
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

                        JSONObject tags = new JSONObject();
                        try {
                            tags.put("token", attendee.getToken());
                            tags.put("type", attendee.getType());
                            OneSignal.sendTags(tags);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
                    invalidTokenMsg.setVisibility(View.VISIBLE);
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

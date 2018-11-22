package app.opass.ccip.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import app.opass.ccip.R;
import app.opass.ccip.activity.CaptureActivity;
import app.opass.ccip.activity.MainActivity;
import app.opass.ccip.adapter.ScenarioAdapter;
import app.opass.ccip.model.Attendee;
import app.opass.ccip.network.CCIPClient;
import app.opass.ccip.util.PreferenceUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.onesignal.OneSignal;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

    View noNetworkView;
    View notConfWifiView;
    View loginView;
    TextView loginTitle;
    RecyclerView scenarioView;
    SwipeRefreshLayout swipeRefreshLayout;
    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        noNetworkView = view.findViewById(R.id.no_network);
        notConfWifiView = view.findViewById(R.id.not_conf_wifi);
        loginView = view.findViewById(R.id.login);
        loginTitle = view.findViewById(R.id.login_title);

        View enterTokenButton = view.findViewById(R.id.enter_token);

        enterTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = mActivity.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_enter_token, null);
                final TextInputLayout tokenInputLayout = dialogView.findViewById(R.id.token_input_layout);
                final TextInputEditText tokenInput = dialogView.findViewById(R.id.token_input);

                final AlertDialog dialog = new AlertDialog.Builder(mActivity)
                    .setView(dialogView)
                    .setTitle(R.string.enter_your_token)
                    .setPositiveButton(R.string.positive_button, null)
                    .setNegativeButton(R.string.negative_button, null)
                    .create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String token = tokenInput.getText().toString().trim();

                                if (token.equals("")) {
                                    tokenInputLayout.setError(getString(R.string.token_required));
                                    return;
                                }
                                PreferenceUtil.setIsNewToken(mActivity, true);
                                PreferenceUtil.setToken(mActivity, token);
                                dialog.dismiss();
                                updateStatus();
                            }
                        });
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

                dialog.show();
            }
        });

        loginTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(mActivity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt(getString(R.string.scan_kktix_qrcode));
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.initiateScan();
            }
        });
        scenarioView = view.findViewById(R.id.scenarios);
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer);

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
            loginView.setVisibility(View.VISIBLE);
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
            loginView.setVisibility(View.VISIBLE);
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        loginView.setVisibility(View.GONE);
        noNetworkView.setVisibility(View.GONE);
        notConfWifiView.setVisibility(View.GONE);

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
                            tags.put("event_id", attendee.getEventId());
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
                    notConfWifiView.setVisibility(View.VISIBLE);
                    notConfWifiView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            swipeRefreshLayout.setRefreshing(true);
                            notConfWifiView.setVisibility(View.GONE);
                            updateStatus();
                        }
                    });
                } else {
                    Snackbar.make(getView(), getString(R.string.invalid_token), Snackbar.LENGTH_LONG).show();
                    PreferenceUtil.setToken(mActivity, null);
                    loginView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Attendee> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                noNetworkView.setVisibility(View.VISIBLE);
                noNetworkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeRefreshLayout.setRefreshing(true);
                        noNetworkView.setVisibility(View.GONE);
                        updateStatus();
                    }
                });
            }
        });
    }
}

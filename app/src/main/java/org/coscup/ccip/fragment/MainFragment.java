package org.coscup.ccip.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.coscup.ccip.MainActivity;
import org.coscup.ccip.R;
import org.coscup.ccip.adapter.ScenarioAdapter;
import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.util.TokenUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

    private Activity mActivity;
    TextView msg;
    RecyclerView scenarioView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        msg = (TextView) view.findViewById(R.id.msg);
        scenarioView = (RecyclerView) view.findViewById(R.id.scenarios);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        mActivity = getActivity();
        scenarioView.setLayoutManager(new LinearLayoutManager(mActivity));
        scenarioView.setItemAnimator(new DefaultItemAnimator());

        if (mActivity.getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            TokenUtil.setToken(mActivity, mActivity.getIntent().getData().getQueryParameter("token"));
        }

        if (TokenUtil.getToken(mActivity) == null) {
            msg.setVisibility(View.VISIBLE);
            msg.setText("Please open this app via link");
            swipeRefreshLayout.setVisibility(View.GONE);
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
        Call<Attendee> attendee = CCIPClient.get().status(TokenUtil.getToken(mActivity));
        attendee.enqueue(new Callback<Attendee>() {
            @Override
            public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    Attendee attendee = response.body();
                    MainActivity.setUserId(attendee.getUserId());
                    scenarioView.setAdapter(new ScenarioAdapter(mActivity, attendee.getScenarios()));
                } else {
                    Toast.makeText(mActivity, "invalid token", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Attendee> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(mActivity, "get status fail, " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

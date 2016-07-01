package org.coscup.ccip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import org.coscup.ccip.adapter.ScenarioAdapter;
import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.util.TokenUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Activity mActivity;
    TextView userId;
    RecyclerView scenarioView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = (TextView) findViewById(R.id.user_id);
        scenarioView = (RecyclerView) findViewById(R.id.scenarios);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        mActivity = this;
        scenarioView.setLayoutManager(new LinearLayoutManager(mActivity));
        scenarioView.setItemAnimator(new DefaultItemAnimator());

        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            TokenUtil.setToken(mActivity, getIntent().getData().getQueryParameter("token"));
        }

        if (TokenUtil.getToken(mActivity) == null) {
            userId.setText("Please open this app via link");
            return;
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateStatus();
            }
        });
    }

    @Override
    protected void onResume() {
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
                    userId.setText("Hello " + attendee.getUserId());
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

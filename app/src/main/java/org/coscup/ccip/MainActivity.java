package org.coscup.ccip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.coscup.ccip.adapter.ScenarioAdapter;
import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.util.TokenUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView hello = (TextView) findViewById(R.id.hello);
        final RecyclerView scenarioView = (RecyclerView) findViewById(R.id.scenarios);
        String token;

        mActivity = this;
        scenarioView.setLayoutManager(new LinearLayoutManager(mActivity));
        scenarioView.setItemAnimator(new DefaultItemAnimator());

        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            token = getIntent().getData().getQueryParameter("token");
            TokenUtil.setToken(mActivity, token);
        } else {
            token = TokenUtil.getToken(mActivity);
        }

        if (token == null) {
            hello.setText("Please open this app via link");
            return;
        }

        Call<Attendee> attendee = CCIPClient.get().status(token);
        attendee.enqueue(new Callback<Attendee>() {
            @Override
            public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                if (response.isSuccessful()) {
                    Attendee attendee = response.body();
                    hello.setText("Hello " + attendee.getUserId());
                    scenarioView.setAdapter(new ScenarioAdapter(mActivity, attendee.getScenarios()));
                } else {
                    hello.setText("invalid token");
                }
            }

            @Override
            public void onFailure(Call<Attendee> call, Throwable t) {
                hello.setText("get status fail");
            }
        });
    }
}

package org.coscup.ccip;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.coscup.ccip.model.Scenario;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CountdownActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_SCENARIO = "scenario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        final TextView countdownText = (TextView) findViewById(R.id.countdown);
        final TextView currentTimeText = (TextView) findViewById(R.id.current_time);
        final RelativeLayout countdownLayot = (RelativeLayout) findViewById(R.id.countdown_layout);

        final Gson gson = new Gson();
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        final Scenario scenario = gson.fromJson(getIntent().getStringExtra(INTENT_EXTRA_SCENARIO), Scenario.class);

        long countdown;

        if (scenario.getUsed() == null) {
            countdown = scenario.getCountdown() * 1000L;
        } else {
            countdown = (scenario.getUsed() + scenario.getCountdown()) * 1000L - new Date().getTime();
        }

        new CountDownTimer(countdown, 1000L) {

            @Override
            public void onTick(long l) {
                countdownText.setText(l/1000 + "");
                currentTimeText.setText(sdf.format(new Date().getTime()));
            }

            @Override
            public void onFinish() {
                countdownText.setText("0");
                currentTimeText.setVisibility(View.GONE);
                countdownLayot.setBackgroundColor(Color.RED);
            }
        }.start();
    }
}

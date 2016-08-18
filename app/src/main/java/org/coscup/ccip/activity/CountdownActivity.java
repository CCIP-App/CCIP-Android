package org.coscup.ccip.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Scenario;
import org.coscup.ccip.util.JsonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CountdownActivity extends TrackActivity {

    public static final String INTENT_EXTRA_SCENARIO = "scenario";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        final TextView attrText = (TextView) findViewById(R.id.attr);
        final TextView countdownText = (TextView) findViewById(R.id.countdown);
        final TextView currentTimeText = (TextView) findViewById(R.id.current_time);
        final RelativeLayout countdownLayot = (RelativeLayout) findViewById(R.id.countdown_layout);
        final Button button = (Button) findViewById(R.id.button);

        final Scenario scenario = JsonUtil.fromJson(getIntent().getStringExtra(INTENT_EXTRA_SCENARIO), Scenario.class);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        JsonObject attr = scenario.getAttr().getAsJsonObject();
        JsonElement elemDiet = attr.get("diet");
        if (elemDiet != null) {
            String diet = elemDiet.getAsString();
            if (diet.equals("meat")) {
                countdownLayot.setBackgroundColor(getResources().getColor(R.color.colorDietMeat));
                attrText.setText(R.string.meal);
            }
            else{
                countdownLayot.setBackgroundColor(getResources().getColor(R.color.colorDietVegetarian));
                attrText.setText(R.string.vegan);
            }
        }
        JsonElement elemSize = attr.get("shirt_size");
        if (elemSize != null) {
            attrText.setText(elemSize.getAsString());
        }

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
                currentTimeText.setText(SDF.format(new Date().getTime()));
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

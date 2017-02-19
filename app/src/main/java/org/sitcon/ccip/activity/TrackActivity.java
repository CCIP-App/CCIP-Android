package org.sitcon.ccip.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.sitcon.ccip.application.CCIPApplicaion;

public class TrackActivity extends AppCompatActivity {
    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CCIPApplicaion application = (CCIPApplicaion) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

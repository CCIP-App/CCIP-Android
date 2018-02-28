package org.sitcon.ccip.application;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.OneSignal;

import org.sitcon.ccip.R;

public class CCIPApplicaion extends Application{
    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(getString(R.string.ga_tracking_id));
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this).init();
    }
}

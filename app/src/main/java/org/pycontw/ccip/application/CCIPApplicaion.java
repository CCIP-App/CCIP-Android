package org.pycontw.ccip.application;

import android.app.Application;

import com.onesignal.OneSignal;

public class CCIPApplicaion extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this).init();
    }
}

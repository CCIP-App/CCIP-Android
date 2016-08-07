package org.coscup.ccip.fragment;

import android.app.Fragment;
import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.coscup.ccip.application.CCIPApplicaion;

public class TrackFragment extends Fragment {
    Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CCIPApplicaion application = (CCIPApplicaion) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

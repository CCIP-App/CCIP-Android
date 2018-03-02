package org.sitcon.ccip.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.sitcon.ccip.R;
import org.sitcon.ccip.adapter.AnnouncementAdapter;
import org.sitcon.ccip.model.Announcement;
import org.sitcon.ccip.network.CCIPClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementFragment extends TrackFragment {

    private Activity mActivity;
    RecyclerView announcementView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_announcement, container, false);

        announcementView = (RecyclerView) view.findViewById(R.id.announcement);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        mActivity = getActivity();
        announcementView.setLayoutManager(new LinearLayoutManager(mActivity));
        announcementView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        CCIPClient.get().announcement().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.body().isEmpty()) {
                    view.findViewById(R.id.announcement_empty).setVisibility(View.VISIBLE);
                }
                announcementView.setAdapter(new AnnouncementAdapter(mActivity, response.body()));
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(mActivity, R.string.offline, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

}

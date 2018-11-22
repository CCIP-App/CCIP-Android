package app.opass.ccip.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import app.opass.ccip.R;
import app.opass.ccip.adapter.AnnouncementAdapter;
import app.opass.ccip.model.Announcement;
import app.opass.ccip.network.CCIPClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class AnnouncementFragment extends Fragment {

    RecyclerView announcementView;
    SwipeRefreshLayout swipeRefreshLayout;
    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_announcement, container, false);

        announcementView = view.findViewById(R.id.announcement);
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer);

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
                if (response.isSuccessful() && !response.body().isEmpty()) {
                    announcementView.setAdapter(new AnnouncementAdapter(mActivity, response.body()));
                } else {
                    view.findViewById(R.id.announcement_empty).setVisibility(View.VISIBLE);
                }
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

package org.coscup.ccip.fragment;

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

import org.coscup.ccip.R;
import org.coscup.ccip.adapter.AnnouncementAdapter;
import org.coscup.ccip.adapter.ScheduleAdapter;
import org.coscup.ccip.model.Announcement;
import org.coscup.ccip.model.Program;
import org.coscup.ccip.model.Room;
import org.coscup.ccip.network.CCIPClient;
import org.coscup.ccip.network.COSCUPClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);

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

    public void setScheduleAdapter(List<Program> programs) {
        HashMap<String, List<Program>> map = new HashMap();
        for (Program program : programs) {
            if (program.getStarttime() == null) continue;

            if (map.containsKey(program.getStarttime())) {
                List<Program> tmp = map.get(program.getStarttime());
                tmp.add(program);
                map.put(program.getStarttime(), tmp);
            } else {
                List<Program> list = new ArrayList();
                list.add(program);
                map.put(program.getStarttime(), list);
            }
        }

        SortedSet<String> keys = new TreeSet(map.keySet());
        List<List<Program>> programSlotList = new ArrayList();
        for (String key : keys) {
            programSlotList.add(map.get(key));
        }
    }

}

package org.coscup.ccip.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.coscup.ccip.R;
import org.coscup.ccip.adapter.ScheduleAdapter;
import org.coscup.ccip.model.Program;
import org.coscup.ccip.model.Room;
import org.coscup.ccip.model.Type;
import org.coscup.ccip.network.COSCUPClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleFragment extends Fragment {

    private Activity mActivity;
    RecyclerView scenarioView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        scenarioView = (RecyclerView) view.findViewById(R.id.schedule);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        mActivity = getActivity();
        scenarioView.setLayoutManager(new LinearLayoutManager(mActivity));
        scenarioView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        Call<List<Room>> roomCall = COSCUPClient.get().room();
        roomCall.enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful()) {
                    List<Room> rooms = response.body();
                    HashMap<String, String> roomMap = new HashMap();
                    for (Room room : rooms) {
                        roomMap.put(room.getRoom(), room.getName());
                    }
                    getType(roomMap);
                } else {

                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {

            }
        });

        return view;
    }

    public void getType(final Map<String, String> roomMap) {
        Call<List<Type>> typeCall = COSCUPClient.get().type();
        typeCall.enqueue(new Callback<List<Type>>() {
            @Override
            public void onResponse(Call<List<Type>> call, Response<List<Type>> response) {
                if (response.isSuccessful()) {
                    List<Type> types = response.body();
                    HashMap<Integer, Type> typeMap = new HashMap();
                    for (Type type : types) {
                        typeMap.put(type.getType(), type);
                    }
                    getSchedule(roomMap, typeMap);
                } else {

                }
            }

            @Override
            public void onFailure(Call<List<Type>> call, Throwable t) {

            }
        });
    }

    public void getSchedule(final Map<String, String> roomMap, final Map<Integer, Type> typeMap) {
        final Call<List<Program>> program = COSCUPClient.get().program();
        program.enqueue(new Callback<List<Program>>() {
            @Override
            public void onResponse(Call<List<Program>> call, Response<List<Program>> response) {
                if (response.isSuccessful()) {
                    List<Program> programs = response.body();
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
                        swipeRefreshLayout.setRefreshing(false);
                        scenarioView.setAdapter(new ScheduleAdapter(mActivity, programSlotList, roomMap, typeMap));
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<List<Program>> call, Throwable t) {

            }
        });
    }

}

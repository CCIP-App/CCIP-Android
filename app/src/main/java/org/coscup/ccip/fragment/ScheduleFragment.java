package org.coscup.ccip.fragment;

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

import com.google.gson.internal.bind.util.ISO8601Utils;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import org.coscup.ccip.R;
import org.coscup.ccip.adapter.ScheduleAdapter;
import org.coscup.ccip.model.Submission;
import org.coscup.ccip.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ScheduleFragment extends TrackFragment {

    private Activity mActivity;
    RecyclerView scheduleView;
    SwipeRefreshLayout swipeRefreshLayout;
    private List<Submission> mSubmissions;
    private String date;
    private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("MM/dd");

    public static Fragment newInstance(String date, List<Submission> submissions) {
        ScheduleFragment scheduleFragment = new ScheduleFragment();
        scheduleFragment.date = date;
        scheduleFragment.mSubmissions = submissions;
        return scheduleFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        scheduleView = (RecyclerView) view.findViewById(R.id.schedule);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        mActivity = getActivity();
        scheduleView.setLayoutManager(new LinearLayoutManager(mActivity));
        scheduleView.setItemAnimator(new DefaultItemAnimator());

        setHasOptionsMenu(true);

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        if (mSubmissions != null) {
            swipeRefreshLayout.setRefreshing(false);
            scheduleView.setAdapter(new ScheduleAdapter(mActivity, transformSubmissions(mSubmissions)));
        }

        return view;
    }

    private List<Submission> loadStarSubmissions() {
        List<Submission> tmp = new ArrayList<>();
        for (Submission submission : PreferenceUtil.loadStars(mActivity)) {
            try {
                String tmpDate = SDF_DATE
                    .format(ISO8601Utils.parse(submission.getStart(), new ParsePosition(0)));
                if (tmpDate.equals(date)) {
                    tmp.add(submission);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return tmp;
    }

    private List<List<Submission>> transformSubmissions(List<Submission> submissions) {
        HashMap<String, List<Submission>> map = new HashMap();
        for (Submission submission : submissions) {
            if (submission.getStart() == null) continue;

            if (map.containsKey(submission.getStart())) {
                List<Submission> tmp = map.get(submission.getStart());
                tmp.add(submission);
                Collections.sort(tmp, new Comparator<Submission>() {
                    @Override
                    public int compare(Submission s1, Submission s2) {
                        return s1.getRoom().compareTo(s2.getRoom());
                    }
                });
                map.put(submission.getStart(), tmp);
            } else {
                List<Submission> list = new ArrayList();
                list.add(submission);
                map.put(submission.getStart(), list);
            }
        }

        SortedSet<String> keys = new TreeSet(map.keySet());
        List<List<Submission>> submissionSlotList = new ArrayList();
        for (String key : keys) {
            submissionSlotList.add(map.get(key));
        }
        return submissionSlotList;
    }

    public void toggleStarFilter(boolean isStar) {
        ((ScheduleAdapter) scheduleView.getAdapter()).update(
            transformSubmissions(isStar ? loadStarSubmissions() : mSubmissions));
    }

}

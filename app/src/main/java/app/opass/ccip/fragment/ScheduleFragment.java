package app.opass.ccip.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.opass.ccip.R;
import app.opass.ccip.adapter.ScheduleAdapter;
import app.opass.ccip.model.Submission;
import app.opass.ccip.util.PreferenceUtil;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleFragment extends Fragment {

    private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("MM/dd");
    RecyclerView scheduleView;
    private Activity mActivity;
    private List<Submission> mSubmissions;
    private String date;
    private boolean starFilter = false;

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

        scheduleView = view.findViewById(R.id.schedule);

        mActivity = getActivity();
        scheduleView.setLayoutManager(new LinearLayoutManager(mActivity));
        scheduleView.setItemAnimator(new DefaultItemAnimator());

        if (mSubmissions != null) {
            scheduleView.setAdapter(new ScheduleAdapter(mActivity, transformSubmissions(mSubmissions)));
        }

        return view;
    }

    private List<Submission> loadStarSubmissions() {
        List<Submission> tmp = new ArrayList<>();
        List<Submission> starSubmissions = PreferenceUtil.loadStars(mActivity);
        if (starSubmissions != null) {
            for (Submission submission : starSubmissions) {
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
        this.starFilter = isStar;
        ((ScheduleAdapter) scheduleView.getAdapter()).update(
            transformSubmissions(isStar ? loadStarSubmissions() : mSubmissions));
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleStarFilter(starFilter);
    }
}

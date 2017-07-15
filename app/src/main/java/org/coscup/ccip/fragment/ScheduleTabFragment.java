package org.coscup.ccip.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.List;
import org.coscup.ccip.R;
import org.coscup.ccip.adapter.ScheduleTabAdapter;
import org.coscup.ccip.model.Submission;
import org.coscup.ccip.network.COSCUPClient;
import org.coscup.ccip.util.PreferenceUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleTabFragment extends TrackFragment {

  private Activity mActivity;
  private boolean starFilter = false;
  private List<Submission> mSubmissions;
  private ScheduleTabAdapter scheduleTabAdapter;
  TabLayout tabLayout;
  ViewPager viewPager;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.fragment_schedule_tab, container, false);

    tabLayout = (TabLayout) view.findViewById(R.id.tabs);
    viewPager = (ViewPager) view.findViewById(R.id.pager);

    mActivity = getActivity();

    setHasOptionsMenu(true);

    Call<List<Submission>> submissionCall = COSCUPClient.get().submission();
    submissionCall.enqueue(new Callback<List<Submission>>() {
      @Override
      public void onResponse(Call<List<Submission>> call, Response<List<Submission>> response) {
        if (response.isSuccessful()) {
          mSubmissions = response.body();
          PreferenceUtil.savePrograms(mActivity, mSubmissions);
        } else {
          loadOfflineSchedule();
        }
        setupViewPager();
      }

      @Override
      public void onFailure(Call<List<Submission>> call, Throwable t) {
        loadOfflineSchedule();
      }
    });

    return view;
  }

  private void setupViewPager() {
    scheduleTabAdapter = new ScheduleTabAdapter(((AppCompatActivity) mActivity).getSupportFragmentManager());
    viewPager.setAdapter(scheduleTabAdapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.add("star")
        .setIcon(R.drawable.ic_star_border_white_48dp)
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            starFilter = !starFilter;
            if (starFilter) {
              item.setIcon(R.drawable.ic_star_white_48dp);
            } else {
              item.setIcon(R.drawable.ic_star_border_white_48dp);
            }
            return false;
          }
        })
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
  }

  public void loadOfflineSchedule() {
    Toast.makeText(mActivity, R.string.offline, Toast.LENGTH_LONG).show();
    List<Submission> submissions = PreferenceUtil.loadPrograms(mActivity);
    if (submissions != null) {

    }
  }
}

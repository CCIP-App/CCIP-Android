package org.coscup.ccip.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.coscup.ccip.R;

public class ScheduleTabFragment extends TrackFragment {

  private Activity mActivity;
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

    return view;
  }
}

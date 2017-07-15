package org.coscup.ccip.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import org.coscup.ccip.R;

public class ScheduleTabFragment extends TrackFragment {

  private Activity mActivity;
  private boolean starFilter = false;
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

    return view;
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
}

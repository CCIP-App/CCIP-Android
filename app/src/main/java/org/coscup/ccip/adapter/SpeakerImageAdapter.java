package org.coscup.ccip.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.coscup.ccip.fragment.SpeakerFragment;
import org.coscup.ccip.model.Speaker;

import java.util.ArrayList;
import java.util.List;

public class SpeakerImageAdapter extends FragmentStatePagerAdapter {
    private List<Speaker> speakers = new ArrayList<>();
    private List<Fragment> mFragmentList = new ArrayList<>();

    public SpeakerImageAdapter(FragmentManager fm, List<Speaker> speakers) {
        super(fm);
        this.speakers = speakers;
        for (Speaker speaker : this.speakers) {
            mFragmentList.add(SpeakerFragment.newInstance(speaker));
        }
    }

    @Override
    public int getCount() {
        return this.mFragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }
}

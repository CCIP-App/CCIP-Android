package app.opass.ccip.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import app.opass.ccip.fragment.SpeakerFragment;
import app.opass.ccip.model.Speaker;

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

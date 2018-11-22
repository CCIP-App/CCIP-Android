package app.opass.ccip.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import app.opass.ccip.fragment.SpeakerFragment;
import app.opass.ccip.model.Speaker;

import java.util.ArrayList;
import java.util.List;

public class SpeakerImageAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragmentList = new ArrayList<>();

    public SpeakerImageAdapter(FragmentManager fm, List<Speaker> speakers) {
        super(fm);
        for (Speaker speaker : speakers) {
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

package app.opass.ccip.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import app.opass.ccip.fragment.SpeakerFragment
import app.opass.ccip.model.Speaker
import java.util.*

class SpeakerImageAdapter(fm: FragmentManager, speakers: List<Speaker>) : FragmentStatePagerAdapter(fm) {
    private val mFragmentList = ArrayList<Fragment>()

    init {
        for (speaker in speakers) {
            mFragmentList.add(SpeakerFragment.newInstance(speaker))
        }
    }

    override fun getCount(): Int {
        return this.mFragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }
}

package app.opass.ccip.ui.schedule

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.*

class ScheduleTabAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getItem(position: Int) = mFragmentList[position]

    override fun getCount() = mFragmentList.size

    override fun getPageTitle(position: Int) = mFragmentTitleList[position]

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    fun toggleStarFilter(isStar: Boolean) {
        for (fragment in mFragmentList) {
            (fragment as ScheduleFragment).toggleStarFilter(isStar)
        }
    }
}

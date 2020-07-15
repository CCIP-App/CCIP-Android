package app.opass.ccip.ui.schedule

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ScheduleTabAdapter(
    fm: FragmentManager,
    private val dates: List<String>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) = ScheduleFragment.newInstance(dates[position])

    override fun getCount() = dates.size

    override fun getPageTitle(position: Int) = dates[position]
}

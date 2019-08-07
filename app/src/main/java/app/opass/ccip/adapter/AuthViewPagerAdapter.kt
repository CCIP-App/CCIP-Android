package app.opass.ccip.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.opass.ccip.activity.AuthActivity

class AuthViewPagerAdapter(fa: FragmentActivity, val fragments: MutableList<AuthActivity.PageFragment>) :
    FragmentStateAdapter(fa) {
    override fun createFragment(position: Int) = fragments[position]
    override fun getItemCount() = fragments.size
}

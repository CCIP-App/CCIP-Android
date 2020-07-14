package app.opass.ccip.ui.auth

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AuthViewPagerAdapter(fa: FragmentActivity, val fragments: MutableList<AuthActivity.PageFragment>) :
    FragmentStateAdapter(fa) {
    override fun createFragment(position: Int) = fragments[position]
    override fun getItemCount() = fragments.size
}

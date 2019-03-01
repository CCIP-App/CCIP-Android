package app.opass.ccip.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.transaction
import app.opass.ccip.R
import app.opass.ccip.fragment.*
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.navigation.NavigationView
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {
    companion object {
        private val URI_GITHUB = Uri.parse("https://github.com/CCIP-App/CCIP-Android")
    }

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var mActivity: Activity
    private lateinit var confLogoImageView: ImageView
    private lateinit var userTitleTextView: TextView
    private lateinit var userIdTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mActivity = this

        mDrawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        confLogoImageView = navigationView.getHeaderView(0).findViewById(R.id.conf_logo)
        userTitleTextView = navigationView.getHeaderView(0).findViewById(R.id.user_title)
        userIdTextView = navigationView.getHeaderView(0).findViewById(R.id.user_id)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        setupDrawerContent(navigationView)

        drawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)

        setTitle(R.string.fast_pass)
        supportFragmentManager.transaction {
            replace(R.id.content_frame, MainFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (navigationView.menu.findItem(R.id.fast_pass).isChecked) {
            super.onBackPressed()
        } else {
            setTitle(R.string.fast_pass)
            supportFragmentManager.transaction {
                replace(R.id.content_frame, MainFragment())
            }
            navigationView.setCheckedItem(R.id.fast_pass)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            PreferenceUtil.setIsNewToken(this, true)
            PreferenceUtil.setToken(this, result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun setUserTitle(userTitle: String) {
        userTitleTextView.visibility = View.VISIBLE
        userTitleTextView.text = userTitle
    }

    fun setUserId(userId: String) {
        userIdTextView.text = userId
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem -> jumpToFragment(menuItem) }
    }

    private fun jumpToFragment(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true

        when {
            menuItem.itemId == R.id.star -> mActivity.startActivity(Intent(Intent.ACTION_VIEW, URI_GITHUB))
            menuItem.itemId == R.id.telegram -> mActivity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(PreferenceUtil.getCurrentEvent(mActivity).features.telegram)
                )
            )
            else -> {
                val fragment = when (menuItem.itemId) {
                    R.id.fast_pass -> MainFragment()
                    R.id.schedule -> ScheduleTabFragment()
                    R.id.announcement -> AnnouncementFragment()
                    R.id.puzzle -> PuzzleFragment()
                    R.id.ticket -> MyTicketFragment()
                    R.id.irc -> IRCFragment()
                    R.id.venue -> VenueFragment()
                    R.id.sponsors -> SponsorFragment()
                    R.id.staffs -> StaffFragment()
                    else -> null
                }

                title = menuItem.title

                fragment?.let {
                    supportFragmentManager.transaction { replace(R.id.content_frame, it) }
                }
            }
        }

        mDrawerLayout.closeDrawers()

        return true
    }
}

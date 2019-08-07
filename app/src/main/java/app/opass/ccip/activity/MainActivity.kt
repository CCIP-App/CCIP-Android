package app.opass.ccip.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.transaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.adapter.DrawerMenuAdapter
import app.opass.ccip.adapter.IdentityAction
import app.opass.ccip.fragment.*
import app.opass.ccip.model.FeatureType
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso

private const val STATE_ACTION_BAR_TITLE = "ACTION_BAR_TITLE"
private const val STATE_IS_DEFAULT_FEATURE_SELECTED = "IS_DEFAULT_FEATURE_SELECTED"

class MainActivity : AppCompatActivity() {
    companion object {
        const val ARG_IS_FROM_NOTIFICATION = "isFromNotification"
    }

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerMenu: RecyclerView
    private lateinit var drawerMenuAdapter: DrawerMenuAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var mActivity: Activity
    private lateinit var confLogoImageView: ImageView
    private lateinit var userTitleTextView: TextView
    private lateinit var userIdTextView: TextView

    private lateinit var defaultFeatureItem: DrawerMenuAdapter.FeatureItem
    private var isDefaultFeatureSelected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceUtil.getCurrentEvent(this).eventId.isEmpty()) {
            startActivity(Intent(this, EventActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        mActivity = this

        mDrawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        drawerMenu = findViewById(R.id.drawer_menu)
        confLogoImageView = navigationView.getHeaderView(0).findViewById(R.id.conf_logo)
        userTitleTextView = navigationView.getHeaderView(0).findViewById(R.id.user_title)
        userIdTextView = navigationView.getHeaderView(0).findViewById(R.id.user_id)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)

        buildDrawer()

        val isFromNotification = intent.getBooleanExtra(ARG_IS_FROM_NOTIFICATION, false)
        val isLaunchedFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
        when {
            isFromNotification and !isLaunchedFromHistory -> getFeatureItemByFeatureType(FeatureType.ANNOUNCEMENT)?.let(::onDrawerItemClick)
            savedInstanceState != null -> {
                savedInstanceState.getString(STATE_ACTION_BAR_TITLE)?.let(::setTitle)
                savedInstanceState.getBoolean(STATE_IS_DEFAULT_FEATURE_SELECTED).let { isDefaultFeatureSelected = it }
            }
            else -> onDrawerItemClick(defaultFeatureItem)
        }

        // Beacon need location access
        if (!PreferenceUtil.isBeaconPermissionRequested(mActivity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.beacon_request_permission_title)
                    .setMessage(R.string.beacon_request_permission_message)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), 1)
                    }
                    .setNegativeButton(getString(R.string.no_thanks), null)
                    .setOnDismissListener {
                        PreferenceUtil.setBeaconPermissionRequested(mActivity)
                    }
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        buildDrawer()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_ACTION_BAR_TITLE, title as String)
        outState.putBoolean(STATE_IS_DEFAULT_FEATURE_SELECTED, isDefaultFeatureSelected)
    }

    override fun onBackPressed() {
        when {
            mDrawerLayout.isDrawerOpen(GravityCompat.START) -> mDrawerLayout.closeDrawers()
            isDefaultFeatureSelected -> super.onBackPressed()
            else -> onDrawerItemClick(defaultFeatureItem)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(ARG_IS_FROM_NOTIFICATION, false) == true) {
            getFeatureItemByFeatureType(FeatureType.ANNOUNCEMENT)?.let(::onDrawerItemClick)
        }
    }

    fun setUserTitle(userTitle: String) {
        userTitleTextView.visibility = View.VISIBLE
        userTitleTextView.text = userTitle
    }

    fun setUserId(userId: String) {
        userIdTextView.text = userId
    }

    private fun getFeatureItemByFeatureType(type: FeatureType): DrawerMenuAdapter.FeatureItem? {
        if (type == FeatureType.WEBVIEW) throw IllegalArgumentException("shouldn't use webview at this time")
        val feature = PreferenceUtil.getCurrentEvent(this).features.firstOrNull { it.feature == type } ?: return null
        return DrawerMenuAdapter.FeatureItem.fromFeature(feature)
    }

    private fun buildDrawer() {
        val event = PreferenceUtil.getCurrentEvent(this)

        Picasso.get().load(event.logoUrl).into(confLogoImageView)

        val role = PreferenceUtil.getRole(this)
        val filteredFeatures = event.features.filter {
            it.visibleRoles?.contains(role) ?: true
        }

        defaultFeatureItem = DrawerMenuAdapter.FeatureItem.fromFeature(filteredFeatures[0])
        drawerMenuAdapter = DrawerMenuAdapter(this, filteredFeatures, ::onDrawerItemClick)
        drawerMenu.adapter = drawerMenuAdapter
        drawerMenu.layoutManager = LinearLayoutManager(this)
        navigationView.getHeaderView(0).findViewById<RelativeLayout>(R.id.nav_header_info)
            .setOnClickListener { headerView ->
                drawerMenuAdapter.apply {
                    shouldShowIdentities = !shouldShowIdentities
                    headerView.findViewById<ImageView>(R.id.identities_shown_indicator)
                        .animate()
                        .rotation(if (shouldShowIdentities) 180F else 0F)
                }
            }
    }

    private fun onDrawerItemClick(item: Any) {
        when (item) {
            IdentityAction.SWITCH_EVENT -> {
                this.startActivity(Intent(this, EventActivity::class.java))
                finish()
            }
            is DrawerMenuAdapter.FeatureItem -> {
                if (!item.isEmbedded) return this.startActivity(Intent(Intent.ACTION_VIEW, item.url!!.toUri()))

                isDefaultFeatureSelected = item == defaultFeatureItem
                val fragment = when (item.type) {
                    FeatureType.FAST_PASS -> MainFragment()
                    FeatureType.SCHEDULE -> ScheduleTabFragment()
                    FeatureType.ANNOUNCEMENT -> AnnouncementFragment()
                    FeatureType.TICKET -> MyTicketFragment()
                    FeatureType.PUZZLE -> if (item.url != null) PuzzleFragment.newInstance(item.url) else return
                    else -> WebViewFragment.newInstance(
                        item.url!!
                            .replace("{token}", PreferenceUtil.getToken(mActivity).toString())
                            .replace("{role}", PreferenceUtil.getRole(mActivity).toString()),
                        item.shouldUseBuiltinZoomControls
                    )
                }
                supportFragmentManager.transaction { replace(R.id.content_frame, fragment) }
            }
        }
        title = when (item) {
            is DrawerMenuAdapter.FeatureItem -> item.displayText.findBestMatch(this)
            else -> this.resources.getString(R.string.app_name)
        }
        mDrawerLayout.closeDrawers()
    }
}

package app.opass.ccip.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.extension.setOnApplyWindowInsetsListenerCompat
import app.opass.ccip.extension.updateMargin
import app.opass.ccip.model.Feature
import app.opass.ccip.model.FeatureType
import app.opass.ccip.model.WifiNetworkInfo
import app.opass.ccip.network.PortalClient
import app.opass.ccip.ui.announcement.AnnouncementFragment
import app.opass.ccip.ui.event.EventActivity
import app.opass.ccip.ui.fastpass.FastPassFragment
import app.opass.ccip.ui.fastpass.MyTicketFragment
import app.opass.ccip.ui.schedule.ScheduleTabFragment
import app.opass.ccip.util.CryptoUtil
import app.opass.ccip.util.PreferenceUtil
import app.opass.ccip.util.WifiUtil
import coil.load
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val STATE_ACTION_BAR_TITLE = "ACTION_BAR_TITLE"
private const val STATE_IS_DEFAULT_FEATURE_SELECTED = "IS_DEFAULT_FEATURE_SELECTED"

class MainActivity : AppCompatActivity(), CoroutineScope {
    companion object {
        const val ARG_IS_FROM_NOTIFICATION = "isFromNotification"
    }

    interface BackPressAwareFragment {
        // Returns true if the event is consumed.
        fun onBackPressed(): Boolean
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
    private lateinit var navbarAnchor: View

    private lateinit var defaultFeatureItem: DrawerMenuAdapter.FeatureItem
    private var isDefaultFeatureSelected: Boolean = true
    private var currentEventId: String? = null

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val event = PreferenceUtil.getCurrentEvent(this)
        if (event.eventId.isEmpty()) {
            startActivity(Intent(this, EventActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        mActivity = this
        mJob = Job()

        mDrawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        drawerMenu = findViewById(R.id.drawer_menu)
        confLogoImageView = navigationView.getHeaderView(0).findViewById(R.id.conf_logo)
        userTitleTextView = navigationView.getHeaderView(0).findViewById(R.id.user_title)
        userIdTextView = navigationView.getHeaderView(0).findViewById(R.id.user_id)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val drawerContent = findViewById<CoordinatorLayout>(R.id.main_content)
        navbarAnchor = findViewById(R.id.navbar_anchor)
        drawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        mDrawerLayout.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        mDrawerLayout.setOnApplyWindowInsetsListenerCompat { _, _, insetsCompat ->
            drawerContent.updatePadding(left = insetsCompat.systemWindowInsetLeft, right = insetsCompat.systemWindowInsetRight)
            drawerMenu.updatePadding(bottom = insetsCompat.systemGestureInsets.bottom)
            navbarAnchor.updateMargin(bottom = insetsCompat.systemGestureInsets.bottom)
            insetsCompat.inset(
                insetsCompat.systemWindowInsetLeft, 0,
                insetsCompat.systemWindowInsetRight, 0
            ).toWindowInsets()!!
        }

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
        currentEventId = event.eventId

        launch {
            try {
                val response = PortalClient.get().getEventConfig(event.eventId).asyncExecute()
                if (!response.isSuccessful) {
                    return@launch
                }
                val newEvent = response.body()!!
                if (event != newEvent) {
                    PreferenceUtil.setCurrentEvent(this@MainActivity, newEvent)
                    restartActivity()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        buildDrawer()
    }

    override fun onDestroy() {
        mJob.cancel()
        super.onDestroy()
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

    private fun dispatchBackPressToChildFragment(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.content_frame) ?: return false
        if (fragment !is BackPressAwareFragment) return false
        return fragment.onBackPressed()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            mDrawerLayout.isDrawerOpen(GravityCompat.START) -> mDrawerLayout.closeDrawers()
            dispatchBackPressToChildFragment() -> Unit
            isDefaultFeatureSelected -> super.onBackPressed()
            else -> onDrawerItemClick(defaultFeatureItem)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (PreferenceUtil.getCurrentEvent(this).eventId != currentEventId) {
            restartActivity()
        } else if (intent?.getBooleanExtra(ARG_IS_FROM_NOTIFICATION, false) == true) {
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

    private fun restartActivity() {
        val restartIntent = Intent(this, this::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        overridePendingTransition(0, 0)
        finish()
        startActivity(restartIntent)
    }

    private fun getFeatureItemByFeatureType(type: FeatureType): DrawerMenuAdapter.FeatureItem? {
        if (type == FeatureType.WEBVIEW) throw IllegalArgumentException("shouldn't use webview at this time")
        val feature = PreferenceUtil.getCurrentEvent(this).features.firstOrNull { it.feature == type } ?: return null
        return DrawerMenuAdapter.FeatureItem.fromFeature(feature)
    }

    private fun buildDrawer() {
        val event = PreferenceUtil.getCurrentEvent(this)

        confLogoImageView.load(event.logoUrl)

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
            MenuAction.SWITCH_EVENT -> {
                this.startActivity(Intent(this, EventActivity::class.java))
                finish()
            }
            MenuAction.LAUNCH_ABOUT_SCREEN -> {
                this.startActivity(Intent(this, AboutActivity::class.java))
            }
            is DrawerMenuAdapter.FeatureItem -> {
                val feature = item.origFeature
                if (item.shouldShowLaunchIcon) return this.startActivity(Intent(Intent.ACTION_VIEW, feature.url!!.toUri()))
                if (feature.feature == FeatureType.WIFI) {
                    feature.wifiNetworks?.let(::showWifiDialog)
                    mDrawerLayout.closeDrawers()
                    return
                }

                if (!isFeatureValid(feature)) return
                isDefaultFeatureSelected = item == defaultFeatureItem
                val fragment = when (feature.feature) {
                    FeatureType.FAST_PASS -> FastPassFragment()
                    FeatureType.SCHEDULE -> ScheduleTabFragment.newInstance(feature.url!!)
                    FeatureType.ANNOUNCEMENT -> AnnouncementFragment.newInstance(feature.url!!)
                    FeatureType.TICKET -> MyTicketFragment()
                    FeatureType.PUZZLE -> PuzzleFragment.newInstance(feature.url!!)
                    else -> WebViewFragment.newInstance(
                        feature.url!!
                            .replace("{token}", PreferenceUtil.getToken(mActivity) ?: "")
                            .replace(
                                "{public_token}",
                                CryptoUtil.toPublicToken(PreferenceUtil.getToken(mActivity)) ?: ""
                            )
                            .replace("{role}", PreferenceUtil.getRole(mActivity) ?: ""),
                        shouldUseBuiltinZoomControls = feature.feature == FeatureType.VENUE
                    )
                }
                supportFragmentManager.commit { replace(R.id.content_frame, fragment) }
            }
        }

        if (item is DrawerMenuAdapter.FeatureItem) {
            title = item.origFeature.displayText.findBestMatch(this)
        }
        mDrawerLayout.closeDrawers()
    }

    private fun onWifiSelected(info: WifiNetworkInfo) {
        val success = WifiUtil.installNetwork(this, info)
        if (success) {
            Snackbar
                .make(mDrawerLayout, R.string.wifi_saved, Snackbar.LENGTH_SHORT)
                .setAnchorView(navbarAnchor)
                .show()
        } else {
            val hasPassword = !info.password.isNullOrEmpty()
            if (!hasPassword) {
                Snackbar
                    .make(mDrawerLayout, R.string.failed_to_save_wifi, Snackbar.LENGTH_LONG)
                    .setAnchorView(navbarAnchor)
                    .show()
                return
            }

            getSystemService<ClipboardManager>()?.run {
                setPrimaryClip(ClipData.newPlainText("", info.password))
            } ?: return
            Snackbar
                .make(mDrawerLayout, R.string.failed_to_save_wifi_copied_to_clipboard, Snackbar.LENGTH_LONG)
                .setAnchorView(navbarAnchor)
                .show()
        }
    }

    private fun showWifiDialog(networks: List<WifiNetworkInfo>) {
        val dialog = AlertDialog.Builder(this).setTitle(R.string.choose_network_to_connect).create()
        val rv = RecyclerView(this).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = WifiNetworkAdapter(networks) { info ->
                dialog.dismiss()
                onWifiSelected(info)
            }
        }

        dialog.setView(rv)
        dialog.show()
    }

    private fun isFeatureValid(f: Feature) : Boolean {
        return when (f.feature) {
            FeatureType.FAST_PASS,
            FeatureType.SCHEDULE,
            FeatureType.ANNOUNCEMENT,
            FeatureType.PUZZLE -> f.url != null
            else -> true
        }
    }
}

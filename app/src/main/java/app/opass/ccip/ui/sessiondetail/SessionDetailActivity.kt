package app.opass.ccip.ui.sessiondetail

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import app.opass.ccip.R
import app.opass.ccip.databinding.ActivitySessionDetailBinding
import app.opass.ccip.model.Session
import app.opass.ccip.ui.MainActivity
import app.opass.ccip.ui.dialogs.NotificationDialogFragment
import app.opass.ccip.util.AlarmUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.internal.bind.util.ISO8601Utils
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat

class SessionDetailActivity : AppCompatActivity() {

    private val TAG = SessionDetailActivity::class.java.simpleName

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            AlarmUtil.setSessionAlarm(this, session)
        }

    private val startForPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Log.i(TAG, "Notification permission granted, trying to schedule alarm!")
                scheduleAlarm()
            } else {
                Log.i(TAG, "Missing notification permission!")
                Toast.makeText(this, getString(R.string.perm_denied), Toast.LENGTH_LONG).show()
            }
        }

    companion object {
        const val INTENT_EXTRA_SESSION_ID = "session_id"
        private val SDF_DATETIME = SimpleDateFormat("MM/dd HH:mm")
        private val SDF_TIME = SimpleDateFormat("HH:mm")
    }

    private lateinit var mActivity: Activity
    private lateinit var session: Session
    private lateinit var fab: FloatingActionButton
    private lateinit var speakerInfo: TextView
    private lateinit var binding: ActivitySessionDetailBinding
    private var isStar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mActivity = this
        val speakerViewPager = binding.viewPagerSpeaker

        session = PreferenceUtil.loadSchedule(this)?.sessions?.find {
            it.id == intent.getStringExtra(INTENT_EXTRA_SESSION_ID)
        } ?: return showToastAndFinish()
        isStar = PreferenceUtil.loadStarredIds(this).contains(session.id)

        val toolbar = binding.toolbar
        toolbar.title = if (session.speakers.isEmpty()) "" else session.speakers[0].getSpeakerDetail(mActivity).name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val markwon = Markwon.builder(this)
            .usePlugin(LinkifyPlugin.create())
            .build()

        if (session.speakers.isEmpty()) {
            binding.appBar.run {
                setExpanded(false)
                val behavior =
                    (layoutParams as CoordinatorLayout.LayoutParams).behavior as ScrollingControlAppBarLayoutBehavior
                behavior.shouldScroll = false
            }
        } else {
            val adapter = SpeakerImageAdapter(supportFragmentManager, session.speakers)
            speakerViewPager.adapter = adapter
            speakerViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

                override fun onPageSelected(position: Int) {
                    markwon.setMarkdown(speakerInfo, session.speakers[position].getSpeakerDetail(mActivity).bio)
                    binding.toolbarLayout.title = session.speakers[position].getSpeakerDetail(mActivity).name
                }

                override fun onPageScrollStateChanged(state: Int) = Unit
            })

            binding.springDotsIndicator.setupWithViewPager(speakerViewPager)
        }
        if (session.speakers.size <= 1) binding.springDotsIndicator.visibility = View.INVISIBLE

        val room = binding.content.room
        val title = binding.content.title
        val time = binding.content.time
        val type = binding.content.type
        val slideLayout = binding.content.slideLayout
        val slide = binding.content.slide
        val coWriteLayout = binding.content.coWriteLayout
        val coWrite = binding.content.coWrite
        val liveLayout = binding.content.liveLayout
        val live = binding.content.live
        val recordLayout = binding.content.recordLayout
        val record = binding.content.record
        val qaLayout = binding.content.qaLayout
        val qa = binding.content.qa
        val langLayout = binding.content.langLayout
        val lang = binding.content.lang
        val programAbstract = binding.content.programAbstract
        val speakerInfoBlock = binding.content.speakerInfoBlock
        speakerInfo = binding.content.speakerinfo

        room.text = session.room.getDetails(mActivity).name
        title.text = session.getSessionDetail(mActivity).title
        title.setOnClickListener { view -> copyToClipboard(view as TextView) }

        try {
            val timeString = StringBuffer()
            val startDate = ISO8601Utils.parse(session.start, ParsePosition(0))
            timeString.append(SDF_DATETIME.format(startDate))
            timeString.append(" ~ ")
            val endDate = ISO8601Utils.parse(session.end, ParsePosition(0))
            timeString.append(SDF_TIME.format(endDate))

            timeString.append(", " + (endDate.time - startDate.time) / 1000 / 60 + resources.getString(R.string.min))

            time.text = timeString
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        type.text = session.type?.getDetails(mActivity)?.name ?: ""

        setSessionInfo(session.language, langLayout, lang)
        setClickableUri(session.slide, slideLayout, slide)
        setClickableUri(session.coWrite, coWriteLayout, coWrite)
        setClickableUri(session.live, liveLayout, live)
        setClickableUri(session.record, recordLayout, record)
        setClickableUri(session.qa, qaLayout, qa)

        if (session.speakers.isEmpty() || session.speakers[0].getSpeakerDetail(mActivity).name.isEmpty()) {
            speakerInfoBlock.visibility = View.GONE
        } else {
            markwon.setMarkdown(speakerInfo, session.speakers[0].getSpeakerDetail(mActivity).bio)
            speakerInfo.setOnClickListener { view -> copyToClipboard(view as TextView) }
        }
        markwon.setMarkdown(programAbstract, session.getSessionDetail(mActivity).description)
        programAbstract.setOnClickListener { view -> copyToClipboard(view as TextView) }

        fab = binding.fab
        checkFabIcon()
        fab.setOnClickListener { view -> toggleFab(view) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isTaskRoot) {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()

                return true
            }

            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, session.getSessionDetail(mActivity).title)
                intent.putExtra(Intent.EXTRA_TEXT, session.uri)

                startActivity(Intent.createChooser(intent, resources.getText(R.string.share)))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun checkFabIcon() {
        if (isStar) {
            fab.setImageResource(R.drawable.ic_bookmark_black_24dp)
        } else {
            fab.setImageResource(R.drawable.ic_bookmark_border_black_24dp)
        }
        fab.drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP)
    }

    private fun toggleFab(view: View) {
        isStar = !isStar
        updateStarSessions(view)
        checkFabIcon()
    }

    private fun updateStarSessions(view: View) {
        val sessionIds = PreferenceUtil.loadStarredIds(this).toMutableList()
        if (sessionIds.contains(session.id)) {
            sessionIds.remove(session.id)
            AlarmUtil.cancelSessionAlarm(this, session)
            Snackbar.make(view, R.string.remove_bookmark, Snackbar.LENGTH_LONG).show()
        } else {
            sessionIds.add(session.id)

            if (PreferenceUtil.shouldPromptForNotification(this)) {
                val notificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (!notificationManager.areNotificationsEnabled()) {
                    NotificationDialogFragment (::requestNotificationPermission)
                        .show(supportFragmentManager, NotificationDialogFragment.TAG)
                }
            }
        }
        PreferenceUtil.saveStarredIds(this, sessionIds)
    }

    private fun copyToClipboard(textView: TextView) {
        val cManager = mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val cData = ClipData.newPlainText("text", textView.text)
        cManager.setPrimaryClip(cData)
        Toast.makeText(mActivity, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun setSessionInfo(text: String?, layout: View, textView: TextView) {
        if (text != null) {
            layout.visibility = View.VISIBLE
            textView.text = text
        }
    }

    private fun setClickableUri(uri: String?, layout: View, textView: TextView) {
        if (uri != null) {
            setSessionInfo(uri, layout, textView)
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            textView.setOnClickListener {
                mActivity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(uri)
                    )
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.session_detail, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val test = menu.findItem(R.id.share)
        test.isVisible = session.uri != null

        return super.onPrepareOptionsMenu(menu)
    }

    private fun showToastAndFinish() {
        Toast.makeText(this, getString(R.string.cannot_read_session_info), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun requestNotificationPermission() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationManager.areNotificationsEnabled()
        ) {
            startForPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                AlarmUtil.setSessionAlarm(mActivity, session)
                Snackbar.make(binding.root, R.string.add_bookmark, Snackbar.LENGTH_LONG).show()
            } else {
                val uri = Uri.parse("package:" + this.packageName)
                startForResult.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri))
            }
        } else {
            AlarmUtil.setSessionAlarm(mActivity, session)
        }
    }
}

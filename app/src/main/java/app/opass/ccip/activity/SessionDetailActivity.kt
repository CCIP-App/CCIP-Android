package app.opass.ccip.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import app.opass.ccip.R
import app.opass.ccip.adapter.SpeakerImageAdapter
import app.opass.ccip.model.Session
import app.opass.ccip.ui.ScrollingControlAppBarLayoutBehavior
import app.opass.ccip.util.AlarmUtil
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.internal.bind.util.ISO8601Utils
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.activity_session_detail.*
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat

class SessionDetailActivity : AppCompatActivity() {
    companion object {
        const val INTENT_EXTRA_PROGRAM = "program"
        private val SDF_DATETIME = SimpleDateFormat("MM/dd HH:mm")
        private val SDF_TIME = SimpleDateFormat("HH:mm")
    }

    private lateinit var mActivity: Activity
    private lateinit var session: Session
    private lateinit var fab: FloatingActionButton
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var speakerInfo: TextView
    private var isStar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        mActivity = this
        val speakerViewPager: ViewPager = findViewById(R.id.viewPager_speaker)

        session = JsonUtil.fromJson(intent.getStringExtra(INTENT_EXTRA_PROGRAM), Session::class.java)
        isStar = PreferenceUtil.loadStars(this).contains(session)

        collapsingToolbarLayout = findViewById(R.id.toolbar_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = if (session.speakers.isEmpty()) "" else session.speakers[0].getSpeakerDetail(mActivity).name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val markwon = Markwon.builder(this)
            .usePlugin(LinkifyPlugin.create())
            .build()

        if (session.speakers.isEmpty()) {
            findViewById<AppBarLayout>(R.id.app_bar).run {
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
                    collapsingToolbarLayout.title = session.speakers[position].getSpeakerDetail(mActivity).name
                }

                override fun onPageScrollStateChanged(state: Int) = Unit
            })

            spring_dots_indicator.setViewPager(speakerViewPager)
        }
        if (session.speakers.size <= 1) spring_dots_indicator.visibility = View.INVISIBLE

        val room: TextView = findViewById(R.id.room)
        val title: TextView = findViewById(R.id.title)
        val time: TextView = findViewById(R.id.time)
        val type: TextView = findViewById(R.id.type)
        val community: TextView = findViewById(R.id.community)
        val slideLayout: View = findViewById(R.id.slide_layout)
        val slide: TextView = findViewById(R.id.slide)
        val qaLayout: View = findViewById(R.id.qa_layout)
        val qa: TextView = findViewById(R.id.qa)
        val lang: TextView = findViewById(R.id.lang)
        val programAbstract: TextView = findViewById(R.id.program_abstract)
        val speakerInfoBlock: View = findViewById(R.id.speaker_info_block)
        speakerInfo = findViewById(R.id.speakerinfo)

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

        setClickableUri(session.slide, slideLayout, slide)
        setClickableUri(session.qa, qaLayout, qa)

        if (session.speakers.isEmpty() || session.speakers[0].getSpeakerDetail(mActivity).name.isEmpty()) {
            speakerInfoBlock.visibility = View.GONE
        } else {
            markwon.setMarkdown(speakerInfo, session.speakers[0].getSpeakerDetail(mActivity).bio)
            speakerInfo.setOnClickListener { view -> copyToClipboard(view as TextView) }
        }
        markwon.setMarkdown(programAbstract, session.getSessionDetail(mActivity).description)
        programAbstract.setOnClickListener { view -> copyToClipboard(view as TextView) }

        fab = findViewById(R.id.fab)
        checkFabIcon()
        fab.setOnClickListener { view -> toggleFab(view) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
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
        var sessions: MutableList<Session>? = PreferenceUtil.loadStars(this)
        if (sessions != null) {
            if (sessions.contains(session)) {
                sessions.remove(session)
                AlarmUtil.cancelSessionAlarm(this, session)
                Snackbar.make(view, R.string.remove_bookmark, Snackbar.LENGTH_LONG).show()
            } else {
                sessions.add(session)
                AlarmUtil.setSessionAlarm(this, session)
                Snackbar.make(view, R.string.add_bookmark, Snackbar.LENGTH_LONG).show()
            }
        } else {
            sessions = mutableListOf(session)
        }
        PreferenceUtil.saveStars(this, sessions)
    }

    private fun copyToClipboard(textView: TextView) {
        val cManager = mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val cData = ClipData.newPlainText("text", textView.text)
        cManager.setPrimaryClip(cData)
        Toast.makeText(mActivity, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun setClickableUri(uri: String?, layout: View, textView: TextView) {
        if (uri != null) {
            layout.visibility = View.VISIBLE
            textView.text = uri
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
}

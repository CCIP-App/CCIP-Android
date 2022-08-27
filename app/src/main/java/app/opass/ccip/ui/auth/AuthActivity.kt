package app.opass.ccip.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import app.opass.ccip.R
import app.opass.ccip.databinding.ActivityAuthBinding
import app.opass.ccip.ui.MainActivity
import app.opass.ccip.ui.event.EventActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import java.io.IOException

private const val REQUEST_SCAN_FROM_GALLERY = 1

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var adapter: AuthViewPagerAdapter
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button

    enum class AuthMethod {
        SCAN_FROM_CAMERA,
        SCAN_FROM_GALLERY,
        ENTER_TOKEN
    }

    abstract class PageFragment(resId: Int = 0) : Fragment(resId) {
        open fun onSelected() {}
        open fun onNextButtonClicked() {}
        open fun getNextButtonText(): Int? = null
        open fun getPreviousButtonText(): Int? = null
        abstract fun shouldShowNextButton(): Boolean
        open fun shouldShowPreviousButton() = true
        // Returns true if the event is handled.
        open fun onBackPressed() = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        nextButton = binding.next
        prevButton = binding.previous

        val fragmentList =
            intent?.extras?.getString(EXTRA_EVENT_ID)?.let {
                mutableListOf<PageFragment>(
                    EventCheckFragment.newInstance(
                        it
                    )
                )
            } ?: mutableListOf<PageFragment>(MethodSelectionFragment())

        adapter = AuthViewPagerAdapter(this, fragmentList)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_IDLE && adapter.fragments.lastIndex >= binding.viewPager.currentItem) {
                    onPageSelected()
                }
            }
        })
        binding.viewPager.adapter = adapter

        nextButton.setOnClickListener {
            if (adapter.fragments.lastIndex >= binding.viewPager.currentItem) {
                adapter.fragments[binding.viewPager.currentItem].onNextButtonClicked()
            }
        }
        prevButton.setOnClickListener { onBackPressed() }
        onPageSelected()
    }

    override fun onBackPressed() {
        val item = adapter.fragments[binding.viewPager.currentItem]
        if (!item.onBackPressed() && !popFragment()) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.contents?.let { processToken(it) }

        if (requestCode == REQUEST_SCAN_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            data?.let(::processGalleryIntent)
        }
    }

    private fun onPageSelected() {
        adapter.fragments[binding.viewPager.currentItem].onSelected()
        updateButtonState()
    }

    private fun processGalleryIntent(intent: Intent) {
        val bitmap: Bitmap

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, intent.data)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()
        try {
            val result = reader.decode(binaryBitmap)
            result?.text?.let { processToken(it) }
        } catch (e: NotFoundException) {
            Snackbar.make(binding.content, R.string.no_qr_code_found, Snackbar.LENGTH_SHORT).show()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
    }

    private fun addAndAdvance(fragment: PageFragment) {
        adapter.fragments.add(fragment)
        adapter.notifyDataSetChanged()
        binding.viewPager.currentItem++
    }

    private fun popFragment(): Boolean {
        if (adapter.fragments.size <= 1) return false

        binding.viewPager.currentItem--
        adapter.fragments.removeAt(adapter.fragments.lastIndex)
        adapter.notifyDataSetChanged()
        return true
    }

    fun updateButtonState() {
        adapter.fragments[binding.viewPager.currentItem].run {
            nextButton.isGone = !shouldShowNextButton()
            prevButton.isGone = !shouldShowPreviousButton()
            if (shouldShowNextButton()) nextButton.text = getNextButtonText()?.let(this@AuthActivity::getString)
            if (shouldShowPreviousButton()) prevButton.text = getPreviousButtonText()?.let(this@AuthActivity::getString)
        }
    }

    fun onAuthMethodSelected(method: AuthMethod) {
        when (method) {
            AuthMethod.SCAN_FROM_CAMERA -> {
                IntentIntegrator(this).run {
                    setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    setPrompt(getString(R.string.scan_ticket_qrcode))
                    setCameraId(0)
                    setBeepEnabled(false)
                    setBarcodeImageEnabled(false)
                    captureActivity = CaptureActivity::class.java
                    initiateScan()
                }
            }
            AuthMethod.SCAN_FROM_GALLERY -> {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                }

                startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.select_picture)),
                    REQUEST_SCAN_FROM_GALLERY
                )
            }
            AuthMethod.ENTER_TOKEN -> addAndAdvance(TokenEntryFragment())
        }
    }

    fun processToken(token: String, disableRetry: Boolean = false) {
        addAndAdvance(TokenCheckFragment.newInstance(token, disableRetry))
    }

    fun onAuthFinished() {
        val isStartedByUrl = intent?.extras?.getString(EXTRA_EVENT_ID) != null
        if (isStartedByUrl) startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun onEventChecked(isSuccess: Boolean) {
        if (isSuccess) {
            intent!!.extras!!.getString(EXTRA_TOKEN)?.let { processToken(it, disableRetry = true) } ?: onAuthFinished()
        } else {
            startActivity(Intent(this, EventActivity::class.java))
            finish()
        }
    }

    fun switchEvent() {
        startActivity(Intent(this, EventActivity::class.java))
        finish()
    }

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        fun createIntent(context: Context, eventId: String, token: String?) =
            Intent(context, AuthActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
                token?.let { putExtra(EXTRA_TOKEN, it) }
            }
    }
}

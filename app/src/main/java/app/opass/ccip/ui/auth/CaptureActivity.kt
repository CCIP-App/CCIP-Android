package app.opass.ccip.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.opass.ccip.R
import com.journeyapps.barcodescanner.CaptureManager

class CaptureActivity : AppCompatActivity() {
    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        capture = CaptureManager(this, findViewById(R.id.barcode_view))
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }
}

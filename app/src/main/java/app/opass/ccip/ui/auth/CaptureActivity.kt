package app.opass.ccip.ui.auth

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.opass.ccip.R
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.*
import com.google.zxing.client.android.Intents
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.CaptureManager
import java.io.IOException

class CaptureActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE = 1
    }

    private lateinit var capture: CaptureManager
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        rootView = findViewById(android.R.id.content)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val bitmap: Bitmap
            val uri = data.data

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
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

                if (result != null && result.text != null) {
                    val intent = Intent()
                    intent.putExtra(Intents.Scan.RESULT, result.text)

                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } catch (e: NotFoundException) {
                Snackbar.make(rootView, R.string.no_qr_code_found, Snackbar.LENGTH_SHORT).show()
            } catch (e: ChecksumException) {
                e.printStackTrace()
            } catch (e: FormatException) {
                e.printStackTrace()
            }

        }
    }

    fun selectFromGallery(v: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE)
    }
}

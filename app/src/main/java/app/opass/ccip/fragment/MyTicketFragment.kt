package app.opass.ccip.fragment

import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.util.PreferenceUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

class MyTicketFragment : Fragment() {
    companion object {
        private const val WHITE = -0x1
        private const val BLACK = -0x1000000

        fun encodeAsBitmap(
            contents: String?,
            format: BarcodeFormat,
            desiredWidth: Int,
            desiredHeight: Int
        ): Bitmap {
            val writer = MultiFormatWriter()

            var result: BitMatrix? = null
            try {
                result = writer.encode(contents, format, desiredWidth, desiredHeight, null)
            } catch (e: WriterException) {
                e.printStackTrace()
            }

            val width = result!!.width
            val height = result.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        }
    }

    private lateinit var mActivity: Activity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_my_ticket, container, false)

        mActivity = requireActivity()

        val qrcodeImageView = view.findViewById<ImageView>(R.id.qrcodeImage)

        if (PreferenceUtil.getToken(mActivity) != null) {
            val widthPixels = Resources.getSystem().displayMetrics.widthPixels / 4

            val bm = encodeAsBitmap(PreferenceUtil.getToken(mActivity), BarcodeFormat.QR_CODE, widthPixels, widthPixels)
            qrcodeImageView.setImageBitmap(bm)
        }

        return view
    }
}

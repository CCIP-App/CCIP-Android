package app.opass.ccip.ui.fastpass

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentMyTicketBinding
import app.opass.ccip.ui.auth.AuthActivity
import app.opass.ccip.util.PreferenceUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

class MyTicketFragment : Fragment(R.layout.fragment_my_ticket) {
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

    private var _binding: FragmentMyTicketBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyTicketBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView() {
        val activity = requireActivity()

        if (PreferenceUtil.getToken(activity) != null) {
            val widthPixels = Resources.getSystem().displayMetrics.widthPixels / 4

            val bm = encodeAsBitmap(PreferenceUtil.getToken(activity), BarcodeFormat.QR_CODE, widthPixels, widthPixels)
            binding.qrcodeImage.setImageBitmap(bm)

            binding.qrcodeImage.isGone = false
            binding.ticketNotice.isGone = false
            binding.login.loginView.isGone = true
        } else {
            binding.qrcodeImage.isGone = true
            binding.ticketNotice.isGone = true
            binding.login.loginView.isGone = false

            binding.login.loginButton.setOnClickListener {
                activity.startActivity(Intent(activity, AuthActivity::class.java))
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateView()
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }
}

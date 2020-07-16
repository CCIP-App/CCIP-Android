package app.opass.ccip.extension

import android.view.View
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat

fun View.setOnApplyWindowInsetsListenerCompat(block: (v: View, insets: WindowInsets, insetsCompat: WindowInsetsCompat) -> WindowInsets) {
    setOnApplyWindowInsetsListener { v, insets ->
        val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
        block(v, insets, insetsCompat)
    }
}

package app.opass.ccip.extension

import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.annotation.Px
import androidx.core.view.*

fun View.setOnApplyWindowInsetsListenerCompat(block: (v: View, insets: WindowInsets, insetsCompat: WindowInsetsCompat) -> WindowInsets) {
    setOnApplyWindowInsetsListener { v, insets ->
        val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
        block(v, insets, insetsCompat)
    }
}

data class EdgeInsets(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

fun View.doOnApplyWindowInsets(block: (v: View, insets: WindowInsetsCompat, padding: EdgeInsets, margin: EdgeInsets) -> Unit) {
    val padding = EdgeInsets(this.paddingLeft, this.paddingTop, this.paddingRight, this.paddingBottom)
    val margin = EdgeInsets(this.marginLeft, this.marginTop, this.marginRight, this.marginBottom)
    setOnApplyWindowInsetsListenerCompat { v, insets, insetsCompat ->
        block(v, insetsCompat, padding, margin)
        insets
    }
}

fun View.updateMargin(
    @Px left: Int = marginLeft,
    @Px top: Int = marginTop,
    @Px right: Int = marginRight,
    @Px bottom: Int = marginBottom
) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.setMargins(left, top ,right, bottom)
    }
}

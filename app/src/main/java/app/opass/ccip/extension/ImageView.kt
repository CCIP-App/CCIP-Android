package app.opass.ccip.extension

import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView

private val NEGATIVE by lazy { ColorMatrixColorFilter(floatArrayOf(
    -1.0f, 0f, 0f, 0f, 255f, 0f, -1.0f, 0f, 0f, 255f, 0f, 0f, -1.0f, 0f, 255f, 0f, 0f, 0f, 1.0f, 0f
))}

var ImageView.isInverted: Boolean
    get() = colorFilter == NEGATIVE
    set(value) {
        colorFilter = if (value) NEGATIVE else null
    }

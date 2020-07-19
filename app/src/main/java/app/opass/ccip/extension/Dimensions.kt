package app.opass.ccip.extension

import android.content.res.Resources
import android.util.TypedValue

fun Float.dpToPx(resources: Resources) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics).toInt()

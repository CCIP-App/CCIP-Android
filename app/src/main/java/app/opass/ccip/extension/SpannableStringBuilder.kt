package app.opass.ccip.extension

import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.inSpans

inline fun SpannableStringBuilder.clickable(
    crossinline onClickAction: () -> Unit,
    builderAction: SpannableStringBuilder.() -> Unit
) = inSpans(object : ClickableSpan() {
    override fun onClick(widget: View) {
        onClickAction()
    }
}, builderAction = builderAction)

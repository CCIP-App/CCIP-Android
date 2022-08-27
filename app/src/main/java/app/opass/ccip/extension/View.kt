package app.opass.ccip.extension

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Px
import androidx.core.content.getSystemService
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

fun View.getIme() = context.getSystemService<InputMethodManager>()
fun View.hideIme() = getIme()?.hideSoftInputFromWindow(windowToken, 0)

fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                getIme()?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

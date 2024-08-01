package app.opass.ccip.util

import android.content.Context
import java.util.Locale

object LocaleUtil {
    fun getCurrentLocale(context: Context): Locale =
        context.resources.configuration.locales.get(0)
}

package app.opass.ccip.model

import android.content.Context
import android.os.LocaleList

class LocalizedString {
    companion object {
        fun fromUntranslated(value: String) = LocalizedString().apply {
            isUntranslated = true
            untranslatedValue = value
        }
    }

    private val translations = HashMap<String, String>()

    var untranslatedValue: String? = null
        private set
    var isUntranslated: Boolean = false
        private set

    fun addTranslation(lang: String, value: String) {
        if (isUntranslated) throw IllegalStateException("Cannot add translations to an untranslated LocalizedString")
        translations[lang] = value
    }

    fun getTranslation(lang: String): String? = translations[lang]

    fun getTranslations(): Map<String, String> = translations

    fun findBestMatch(context: Context): String? {
        if (isUntranslated) return untranslatedValue
        if (translations.size == 0) return null

        val keys = translations.keys.toList()
        localeListToStringList(context.resources.configuration.locales).firstOrNull(keys::contains)?.let {
            return translations[it]
        }

        // No matches. Fallback to the first translation.
        return translations[keys[0]]
    }

    private fun localeListToStringList(localeList: LocaleList): Array<String> {
        val list = mutableSetOf<String>()
        for (i in 0 until localeList.size()) {
            list.add(localeList[i].language)
        }
        return list.toTypedArray()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is LocalizedString) return false
        if (this === other) return true
        return isUntranslated == other.isUntranslated
            && untranslatedValue == other.untranslatedValue
            && translations == other.translations
    }

    override fun hashCode(): Int {
        val prime = 31
        val isU = isUntranslated.hashCode()
        val uV = if (untranslatedValue == null) {
            0
        } else {
            untranslatedValue.hashCode()
        }
        val map = translations.hashCode()
        return prime * (prime * (prime + isU) + uV) + map
    }
}

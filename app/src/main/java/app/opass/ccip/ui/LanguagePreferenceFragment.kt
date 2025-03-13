package app.opass.ccip.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LanguagePreferenceFragment : DialogFragment() {

    companion object {
        private const val TAG = "LanguagePreferenceFragment"

        fun show(fragmentManager: FragmentManager) {
            LanguagePreferenceFragment().show(fragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(requireContext())
            adapter = LanguagePreferenceAdapter(
                items = listOf(
                    LanguageOptionItem(
                        languageTag = "x-default",
                        localName = requireContext().getString(R.string.lang_translated_name_x_default),
                        translatedName = requireContext().getString(R.string.lang_translated_name_x_default)
                    ),
                    LanguageOptionItem(
                        languageTag = "en-US",
                        localName = requireContext().getString(R.string.lang_local_name_en_us),
                        translatedName = requireContext().getString(R.string.lang_translated_name_en_us)
                    ),
                    LanguageOptionItem(
                        languageTag = "nan-Hant-TW",
                        localName = requireContext().getString(R.string.lang_local_name_nan_hant_tw),
                        translatedName = requireContext().getString(R.string.lang_translated_name_nan_hant_tw)
                    ),
                    LanguageOptionItem(
                        languageTag = "nan-Latn-TW-pehoeji",
                        localName = requireContext().getString(R.string.lang_local_name_nan_latn_tw_pehoeji),
                        translatedName = requireContext().getString(R.string.lang_translated_name_nan_latn_tw_pehoeji)
                    ),
                    LanguageOptionItem(
                        languageTag = "nan-Latn-TW-tailo",
                        localName = requireContext().getString(R.string.lang_local_name_nan_latn_tw_tailo),
                        translatedName = requireContext().getString(R.string.lang_translated_name_nan_latn_tw_tailo)
                    ),
                    LanguageOptionItem(
                        languageTag = "hi-IN",
                        localName = requireContext().getString(R.string.lang_local_name_hi_in),
                        translatedName = requireContext().getString(R.string.lang_translated_name_hi_in)
                    ),
                    LanguageOptionItem(
                        languageTag = "nb-NO",
                        localName = requireContext().getString(R.string.lang_local_name_nb_no),
                        translatedName = requireContext().getString(R.string.lang_translated_name_nb_no)
                    ),
                    LanguageOptionItem(
                        languageTag = "ta-IN",
                        localName = requireContext().getString(R.string.lang_local_name_ta_in),
                        translatedName = requireContext().getString(R.string.lang_translated_name_ta_in)
                    ),
                    LanguageOptionItem(
                        languageTag = "zh-Hant-TW",
                        localName = requireContext().getString(R.string.lang_local_name_zh_hant_tw),
                        translatedName = requireContext().getString(R.string.lang_translated_name_zh_hant_tw)
                    )
                )
            ) { item ->
                dialog?.dismiss()
                AppCompatDelegate.setApplicationLocales(
                    (
                        if (item.languageTag == "x-default") {
                            LocaleListCompat.getEmptyLocaleList()
                        } else {
                            LocaleListCompat.forLanguageTags(item.languageTag)
                        }
                    )
                )
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_app_language)
            .setView(recyclerView)
            .create()
    }
}

data class LanguageOptionItem(
    val languageTag: String,
    val localName: String,
    val translatedName: String
)

class LanguagePreferenceAdapter(
    private val items: List<LanguageOptionItem>,
    private val onItemClick: (LanguageOptionItem) -> Unit
) : RecyclerView.Adapter<LanguagePreferenceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguagePreferenceViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option_language, parent, false)
            .let(::LanguagePreferenceViewHolder)
            .apply {
                itemView.setOnClickListener {
                    val pos = getBindingAdapterPosition()
                    if (pos != RecyclerView.NO_POSITION) onItemClick(items[pos])
                }
            }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: LanguagePreferenceViewHolder, position: Int) {
        val item = items[position]
        holder.localName.text = item.localName
        holder.translatedName.text = item.translatedName
    }
}

class LanguagePreferenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val localName: TextView = view.findViewById(R.id.option_local_name)
    val translatedName: TextView = view.findViewById(R.id.option_translated_name)
}

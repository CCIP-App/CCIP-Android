package app.opass.ccip.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.extension.updateMargin
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
                        languageTag = "hi-IN",
                        localName = requireContext().getString(R.string.lang_local_name_hi_in),
                        translatedName = requireContext().getString(R.string.lang_translated_name_hi_in)
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
                        languageTag = "zh-Hans-CN",
                        localName = requireContext().getString(R.string.lang_local_name_zh_hans_cn),
                        translatedName = requireContext().getString(R.string.lang_translated_name_zh_hans_cn)
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
        val isSelected = (
            if (item.languageTag == "x-default") {
                AppCompatDelegate.getApplicationLocales() == LocaleListCompat.getEmptyLocaleList()
            } else {
                AppCompatDelegate.getApplicationLocales() == LocaleListCompat.forLanguageTags(item.languageTag)
            }
        )

        holder.localName.text = item.localName
        holder.translatedName.text = item.translatedName

        if (position == 0) {
            holder.optionItem.updateMargin(top = 36)
        }

        if (position == items.size - 1) {
            holder.optionItem.updateMargin(bottom = 36)
        }

        if (!isSelected) {
            holder.selectedIcon.setImageDrawable(null)
        } else {
            holder.optionItem.setBackgroundResource(R.color.secondaryContainer)
        }
    }
}

class LanguagePreferenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val optionItem: LinearLayout = view.findViewById(R.id.option_language_item)
    val localName: TextView = view.findViewById(R.id.option_local_name)
    val translatedName: TextView = view.findViewById(R.id.option_translated_name)
    val selectedIcon: ImageView = view.findViewById(R.id.icon_selected)
}

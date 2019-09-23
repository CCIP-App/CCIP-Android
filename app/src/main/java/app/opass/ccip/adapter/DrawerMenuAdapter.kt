package app.opass.ccip.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import app.opass.ccip.R
import app.opass.ccip.adapter.DrawerMenuViewHolder.*
import app.opass.ccip.model.Feature
import app.opass.ccip.model.FeatureType
import app.opass.ccip.model.LocalizedString
import com.squareup.picasso.Picasso

private const val URL_GITHUB = "https://github.com/CCIP-App/CCIP-Android"

class DrawerMenuAdapter(
    private val context: Context,
    private val features: List<Feature>,
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<DrawerMenuViewHolder>() {
    private val differ = AsyncListDiffer(this, DiffCallback)

    var shouldShowIdentities: Boolean = false
        set(value) {
            field = value
            differ.submitList(buildMergedList())
        }

    init {
        differ.submitList(buildMergedList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerMenuViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            R.layout.item_menu_item -> MenuItemViewHolder(
                inflater.inflate(viewType, parent, false)
            ).apply {
                itemView.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != NO_POSITION) onItemClick(differ.currentList[pos])
                }
            }
            R.layout.item_placeholder_menu_item -> PlaceholderItemViewHolder(
                inflater.inflate(viewType, parent, false)
            )
            R.layout.item_divider -> DividerViewHolder(
                inflater.inflate(viewType, parent, false)
            )
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: DrawerMenuViewHolder, position: Int) {
        when (holder) {
            is MenuItemViewHolder -> {
                val item = differ.currentList[position]
                val itemView = holder.itemView
                val titleView = itemView.findViewById<TextView>(R.id.title)
                val iconView = itemView.findViewById<ImageView>(R.id.icon)
                val launchIconView = itemView.findViewById<ImageView>(R.id.launch_icon).apply { visibility = View.GONE }

                when (item) {
                    is IdentityAction -> {
                        titleView.text = getTitleByAction(item)
                        iconView.setImageDrawable(getDrawable(getIconByAction(item)))
                    }
                    is FeatureItem -> {
                        val feature = item.origFeature

                        titleView.text = feature.displayText.findBestMatch(context)
                        launchIconView?.isGone = !item.shouldShowLaunchIcon
                        if (item.iconDrawable != null) return iconView.setImageDrawable(getDrawable(item.iconDrawable))
                        iconView.setImageDrawable(null)
                        Picasso.get().load(feature.icon).into(iconView)
                    }
                }
            }
            is PlaceholderItemViewHolder -> Unit
            is DividerViewHolder -> Unit
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is PlaceholderItem -> R.layout.item_placeholder_menu_item
            is DividerItem -> R.layout.item_divider
            is IdentityAction -> R.layout.item_menu_item
            is FeatureItem -> R.layout.item_menu_item
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    private fun buildMergedList(): List<Any> {
        val merged = mutableListOf<Any>(PlaceholderItem)
        if (shouldShowIdentities) {
            merged.addAll(arrayOf(IdentityAction.SWITCH_EVENT, DividerItem))
        }
        merged.addAll(features.map(FeatureItem.Companion::fromFeature))
        merged.add(
            FeatureItem(
                Feature(
                    FeatureType.WEBVIEW,
                    icon = null,
                    displayText = LocalizedString.fromUntranslated(context.resources.getString(R.string.star_on_github)),
                    url = URL_GITHUB,
                    visibleRoles = null
                ),
                iconDrawable = R.drawable.github_mark,
                shouldShowLaunchIcon = true
            )
        )
        return merged
    }

    private fun getTitleByAction(action: IdentityAction): String {
        return when (action) {
            IdentityAction.SWITCH_EVENT -> context.resources.getString(R.string.switch_event)
        }
    }

    private fun getIconByAction(action: IdentityAction): Int = when (action) {
        IdentityAction.SWITCH_EVENT -> R.drawable.ic_swap_horiz_black_24dp
    }

    private fun getDrawable(id: Int?): Drawable? {
        return id?.let { context.resources.getDrawable(it, context.theme) }
    }

    data class FeatureItem(
        val origFeature: Feature,
        val iconDrawable: Int? = null,
        val shouldShowLaunchIcon: Boolean = false
    ) {
        companion object {
            private fun getIconByType(type: FeatureType): Int? = when (type) {
                FeatureType.FAST_PASS -> R.drawable.ic_local_activity_black_48dp
                FeatureType.SCHEDULE -> R.drawable.ic_event_note_black_48dp
                FeatureType.ANNOUNCEMENT -> R.drawable.ic_announcement_black_48dp
                FeatureType.PUZZLE -> R.drawable.ic_extension_black_24dp
                FeatureType.TICKET -> R.drawable.qr_code
                FeatureType.TELEGRAM -> R.drawable.telegram_logo
                FeatureType.IM -> R.drawable.ic_question_answer_black_48dp
                FeatureType.SPONSORS -> R.drawable.ic_redeem_black_48dp
                FeatureType.STAFFS -> R.drawable.ic_group_black_48dp
                FeatureType.VENUE -> R.drawable.ic_map_black_48dp
                FeatureType.WEBVIEW -> null
            }

            fun fromFeature(feature: Feature): FeatureItem {
                var item = FeatureItem(feature)
                if (feature.feature == FeatureType.TELEGRAM) {
                    item = item.copy(shouldShowLaunchIcon = true)
                }
                getIconByType(feature.feature)?.let { item = item.copy(iconDrawable = it) }
                return item
            }
        }
    }
}

object DiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is IdentityAction && newItem is IdentityAction -> oldItem == newItem
            oldItem is DrawerMenuAdapter.FeatureItem && newItem is DrawerMenuAdapter.FeatureItem -> oldItem == newItem
            oldItem is PlaceholderItem && newItem is PlaceholderItem -> true
            oldItem is DividerItem && newItem is DividerItem -> true
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals") // Data classes already implemented equals
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is IdentityAction && newItem is IdentityAction -> oldItem == newItem
            oldItem is DrawerMenuAdapter.FeatureItem && newItem is DrawerMenuAdapter.FeatureItem -> oldItem == newItem
            else -> true
        }
    }
}

object PlaceholderItem
object DividerItem

enum class IdentityAction {
    SWITCH_EVENT
}

sealed class DrawerMenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class MenuItemViewHolder(view: View) : DrawerMenuViewHolder(view)
    class PlaceholderItemViewHolder(view: View) : DrawerMenuViewHolder(view)
    class DividerViewHolder(view: View) : DrawerMenuViewHolder(view)
}

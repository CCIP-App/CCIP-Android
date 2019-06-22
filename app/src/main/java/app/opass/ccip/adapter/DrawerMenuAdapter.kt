package app.opass.ccip.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import app.opass.ccip.R
import app.opass.ccip.adapter.DrawerMenuViewHolder.*
import app.opass.ccip.model.Feature
import app.opass.ccip.model.Features
import app.opass.ccip.model.LocalizedString
import com.squareup.picasso.Picasso

private const val URL_GITHUB = "https://github.com/CCIP-App/CCIP-Android"

class DrawerMenuAdapter(
    private val context: Context,
    private val features: Features,
    private val customFeatures: List<Feature>,
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
                    is Action -> {
                        titleView.text = getTitleByAction(item)
                        iconView.setImageDrawable(getDrawable(getIconByAction(item)))
                    }
                    is IdentityAction -> {
                        titleView.text = getTitleByAction(item)
                        iconView.setImageDrawable(getDrawable(getIconByAction(item)))
                    }
                    is Feature -> {
                        titleView.text = item.displayName.findBestMatch(context)
                        if (!item.isEmbedded) launchIconView?.visibility = View.VISIBLE
                        if (item.iconDrawable != null) return iconView.setImageDrawable(getDrawable(item.iconDrawable))
                        iconView.setImageDrawable(null)
                        Picasso.get().load(item.icon).into(iconView)
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
            is Action -> R.layout.item_menu_item
            is IdentityAction -> R.layout.item_menu_item
            is Feature -> R.layout.item_menu_item
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    private fun buildMergedList(): List<Any> {
        val merged = mutableListOf<Any>(PlaceholderItem)
        if (shouldShowIdentities) {
            merged.addAll(arrayOf(IdentityAction.SWITCH_EVENT, DividerItem))
        }
        merged.addAll(arrayOf(Action.FAST_PASS, Action.SCHEDULE, Action.ANNOUNCEMENT, Action.PUZZLE, Action.TICKET))
        merged.addAll(fromFeatures(features, context))
        merged.addAll(customFeatures)
        merged.add(
            Feature(
                null,
                LocalizedString.fromUntranslated(context.resources.getString(R.string.star_on_github)),
                URL_GITHUB,
                iconDrawable = R.drawable.github_mark,
                isEmbedded = false
            )
        )
        return merged
    }

    private fun getTitleByAction(action: Action): String {
        return when (action) {
            Action.FAST_PASS -> context.resources.getString(R.string.fast_pass)
            Action.SCHEDULE -> context.resources.getString(R.string.schedule)
            Action.ANNOUNCEMENT -> context.resources.getString(R.string.announcement)
            Action.TICKET -> context.resources.getString(R.string.my_ticket)
            Action.PUZZLE -> context.resources.getString(R.string.puzzle)
        }
    }

    private fun getTitleByAction(action: IdentityAction): String {
        return when (action) {
            IdentityAction.SWITCH_EVENT -> context.resources.getString(R.string.switch_event)
        }
    }

    private fun getDrawable(id: Int?): Drawable? {
        if (id == null) return null
        return if (Build.VERSION.SDK_INT < 21) {
            context.resources.getDrawable(id)
        } else {
            context.resources.getDrawable(id, context.theme)
        }
    }

    private fun getIconByAction(action: Action): Int = when (action) {
        Action.FAST_PASS -> R.drawable.ic_local_activity_black_48dp
        Action.SCHEDULE -> R.drawable.ic_event_note_black_48dp
        Action.ANNOUNCEMENT -> R.drawable.ic_announcement_black_48dp
        Action.TICKET -> R.drawable.qr_code
        Action.PUZZLE -> R.drawable.ic_extension_black_24dp
    }

    private fun getIconByAction(action: IdentityAction): Int = when (action) {
        IdentityAction.SWITCH_EVENT -> R.drawable.ic_swap_horiz_black_24dp
    }
}

object DiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is Action && newItem is Action -> oldItem == newItem
            oldItem is Feature && newItem is Feature -> oldItem == newItem
            oldItem is PlaceholderItem && newItem is PlaceholderItem -> true
            oldItem is DividerItem && newItem is DividerItem -> true
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals") // Data classes already implemented equals
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is Action && newItem is Action -> oldItem == newItem
            oldItem is Feature && newItem is Feature -> oldItem == newItem
            else -> true
        }
    }
}

private fun fromFeatures(features: Features, context: Context): List<Feature> {
    val list = mutableListOf<Feature>()
    features.irc.let {
        if (it.isNotEmpty()) list.add(
            Feature(
                null,
                LocalizedString.fromUntranslated(context.resources.getString(R.string.irclog)),
                it,
                iconDrawable = R.drawable.ic_question_answer_black_48dp,
                isEmbedded = true
            )
        )
    }
    features.sponsors.let {
        if (it.isNotEmpty()) list.add(
            Feature(
                null,
                LocalizedString.fromUntranslated(context.resources.getString(R.string.sponsors)),
                it,
                iconDrawable = R.drawable.ic_redeem_black_48dp,
                isEmbedded = true
            )
        )
    }
    features.staffs.let {
        if (it.isNotEmpty()) list.add(
            Feature(
                null,
                LocalizedString.fromUntranslated(context.resources.getString(R.string.staffs)),
                it,
                iconDrawable = R.drawable.ic_group_black_48dp,
                isEmbedded = true
            )
        )
    }
    features.telegram.let {
        if (it.isNotEmpty()) list.add(
            Feature(
                null,
                LocalizedString.fromUntranslated(context.resources.getString(R.string.telegram)),
                it,
                iconDrawable = R.drawable.telegram_logo,
                isEmbedded = false
            )
        )
    }
    features.venue.let {
        if (it.isNotEmpty()) list.add(
            Feature(
                null,
                LocalizedString.fromUntranslated(context.resources.getString(R.string.venue)),
                it,
                iconDrawable = R.drawable.ic_map_black_48dp,
                isEmbedded = true,
                shouldUseBuiltinZoomControls = true
            )
        )
    }
    return list
}

object PlaceholderItem
object DividerItem

enum class Action {
    FAST_PASS,
    SCHEDULE,
    ANNOUNCEMENT,
    TICKET,
    PUZZLE
}

enum class IdentityAction {
    SWITCH_EVENT
}

sealed class DrawerMenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class MenuItemViewHolder(view: View) : DrawerMenuViewHolder(view)
    class PlaceholderItemViewHolder(view: View) : DrawerMenuViewHolder(view)
    class DividerViewHolder(view: View) : DrawerMenuViewHolder(view)
}

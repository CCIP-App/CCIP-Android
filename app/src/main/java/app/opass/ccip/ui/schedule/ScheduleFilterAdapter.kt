package app.opass.ccip.ui.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import app.opass.ccip.R
import app.opass.ccip.extension.dpToPx
import app.opass.ccip.extension.updateMargin
import app.opass.ccip.model.SessionTag
import app.opass.ccip.model.SessionType
import com.google.android.material.chip.Chip

typealias SectionHeader = String

class ScheduleFilterAdapter(
    private val context: Context,
    private val onFilterClick: (SessionFilter) -> Unit
) : ListAdapter<Any, ViewHolder>(Differ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_filter_chip -> {
                val chip = view as Chip
                FilterChipViewHolder(chip).apply {
                    itemView.setOnClickListener {
                        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onFilterClick(currentList[bindingAdapterPosition] as SessionFilter)
                    }
                }
            }
            R.layout.item_section_header -> {
                SectionHeaderViewHolder(view as TextView)
            }
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = currentList[position]) {
            is SessionFilter -> {
                (holder as FilterChipViewHolder).chip.run {
                    isChecked = item.isActivated
                    text = when (item) {
                        is SessionFilter.StarredFilter -> context.getString(R.string.bookmarked)
                        is SessionFilter.TagFilter -> item.tag.getDetails(context).name
                        is SessionFilter.TypeFilter -> item.type.getDetails(context).name
                    }
                    isChipIconVisible = item is SessionFilter.StarredFilter
                    checkedIcon =
                        if (item is SessionFilter.StarredFilter && text == context.getString(R.string.bookmarked)) {
                            AppCompatResources.getDrawable(
                                context,
                                R.drawable.ic_bookmark_black_24dp
                            )
                        } else {
                            AppCompatResources.getDrawable(
                                context,
                                R.drawable.ic_mtrl_chip_checked_circle
                            )
                        }
                }
            }
            is SectionHeader -> {
                (holder as SectionHeaderViewHolder).text.text = item
            }
            else -> throw IllegalStateException("Unknown item type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is SessionFilter -> R.layout.item_filter_chip
            is SectionHeader -> R.layout.item_section_header
            else -> throw IllegalStateException("Unknown item type")
        }
    }

    fun submitFilters(list: List<SessionFilter>) {
        val merged = list.toMutableList<Any>()
        merged
            .indexOfFirst { it is SessionFilter.TypeFilter }
            .let { firstTypeFilterIdx ->
                if (firstTypeFilterIdx != -1) {
                    merged.add(firstTypeFilterIdx, context.resources.getText(R.string.type))
                }
            }
        merged
            .indexOfFirst { it is SessionFilter.TagFilter }
            .let { firstTagFilterIdx ->
                if (firstTagFilterIdx != -1) {
                    merged.add(firstTagFilterIdx, context.resources.getText(R.string.tag))
                }
            }
        super.submitList(merged)
    }
}

class FilterHeaderChipAdapter(
    private val context: Context
) : RecyclerView.Adapter<FilterChipViewHolder>() {
    private val items = mutableListOf<SessionFilter>()

    fun submitList(filters: List<SessionFilter>) {
        items.clear()
        items.addAll(filters)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterChipViewHolder {
        val resources = context.resources
        val chip = LayoutInflater.from(context)
            .inflate(R.layout.item_filter_chip, parent, false)
            .apply {
                updateMargin(
                    left = 4F.dpToPx(resources),
                    right = 4F.dpToPx(resources)
                )
            } as Chip
        return FilterChipViewHolder(chip)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FilterChipViewHolder, position: Int) {
        val item = items[position]
        holder.chip.run {
            text = when (item) {
                is SessionFilter.StarredFilter -> context.getString(R.string.bookmarked)
                is SessionFilter.TagFilter -> item.tag.getDetails(context).name
                is SessionFilter.TypeFilter -> item.type.getDetails(context).name
            }
            isChipIconVisible = item is SessionFilter.StarredFilter
            chipIcon = AppCompatResources.getDrawable(context, R.drawable.ic_bookmark_black_24dp)
        }
    }
}

class FilterChipViewHolder(val chip: Chip) : ViewHolder(chip)
class SectionHeaderViewHolder(val text: TextView) : ViewHolder(text)

sealed class SessionFilter(val isActivated: Boolean) {
    class StarredFilter(isActivated: Boolean) : SessionFilter(isActivated)
    class TagFilter(val tag: SessionTag, isActivated: Boolean) : SessionFilter(isActivated)
    class TypeFilter(val type: SessionType, isActivated: Boolean) : SessionFilter(isActivated)
}

private object Differ : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            (oldItem is SessionFilter.StarredFilter && newItem is SessionFilter.StarredFilter)
            -> true
            (oldItem is SessionFilter.TagFilter && newItem is SessionFilter.TagFilter)
            -> oldItem.tag.id == newItem.tag.id
            (oldItem is SessionFilter.TypeFilter && newItem is SessionFilter.TypeFilter)
            -> oldItem.type.id == newItem.type.id
            else -> oldItem === newItem
        }
    }

    @SuppressLint("DiffUtilEquals") // String implements equals!
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            (oldItem is SessionFilter && newItem is SessionFilter) -> oldItem.isActivated == newItem.isActivated
            (oldItem is SectionHeader && newItem is SectionHeader) -> oldItem == newItem
            else -> false
        }
    }
}

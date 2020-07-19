package app.opass.ccip.ui.schedule

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.extension.dpToPx
import app.opass.ccip.extension.updateMargin
import app.opass.ccip.model.SessionTag
import com.google.android.material.chip.Chip

class ScheduleFilterAdapter(
    private val context: Context,
    private val onFilterClick: (SessionFilter) -> Unit
) : ListAdapter<SessionFilter, FilterChipViewHolder>(Differ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterChipViewHolder {
        val resources = context.resources
        val chip = LayoutInflater.from(context)
            .inflate(R.layout.item_filter_chip, parent, false) as Chip
        return FilterChipViewHolder(chip).apply {
            itemView.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onFilterClick(currentList[adapterPosition])
            }
            itemView.updateMargin(
                left = 8F.dpToPx(resources),
                right = 8F.dpToPx(resources)
            )
        }
    }

    override fun onBindViewHolder(holder: FilterChipViewHolder, position: Int) {
        val item = currentList[position]
        holder.chip.run {
            isChecked = item.isActivated
            text = when (item) {
                is SessionFilter.StarredFilter -> context.getString(R.string.bookmarked)
                is SessionFilter.TagFilter -> item.tag.getDetails(context).name
            }
            isChipIconVisible = item is SessionFilter.StarredFilter
        }
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
            }
            isChipIconVisible = item is SessionFilter.StarredFilter
        }
    }
}

class FilterChipViewHolder(val chip: Chip) : RecyclerView.ViewHolder(chip)

sealed class SessionFilter(val isActivated: Boolean) {
    class StarredFilter(isActivated: Boolean) : SessionFilter(isActivated)
    class TagFilter(val tag: SessionTag, isActivated: Boolean) : SessionFilter(isActivated)
}

private object Differ : DiffUtil.ItemCallback<SessionFilter>() {
    override fun areItemsTheSame(oldItem: SessionFilter, newItem: SessionFilter): Boolean {
        return when {
            (oldItem is SessionFilter.StarredFilter && newItem is SessionFilter.StarredFilter)
            -> true
            (oldItem is SessionFilter.TagFilter && newItem is SessionFilter.TagFilter)
            -> oldItem.tag.id == newItem.tag.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: SessionFilter, newItem: SessionFilter): Boolean {
        return when {
            (oldItem is SessionFilter.StarredFilter && newItem is SessionFilter.StarredFilter)
                || (oldItem is SessionFilter.TagFilter && newItem is SessionFilter.TagFilter) -> oldItem.isActivated == newItem.isActivated
            else -> false
        }
    }
}

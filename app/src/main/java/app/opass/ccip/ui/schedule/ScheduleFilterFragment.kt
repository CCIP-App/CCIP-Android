package app.opass.ccip.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.GridLayoutManager
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentScheduleFilterBinding
import app.opass.ccip.extension.debounce
import app.opass.ccip.extension.doOnApplyWindowInsets
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.math.MathUtils

fun BottomSheetBehavior<*>.approxSlideOffset() = when (state) {
    BottomSheetBehavior.STATE_EXPANDED -> 1F
    BottomSheetBehavior.STATE_COLLAPSED -> 0F
    else -> 0F
}

fun BottomSheetBehavior<*>.collapseOrHide() {
    state = if (skipCollapsed) BottomSheetBehavior.STATE_HIDDEN
    else BottomSheetBehavior.STATE_COLLAPSED
}

class ScheduleFilterFragment : Fragment() {
    private var _binding: FragmentScheduleFilterBinding? = null
    private val binding get() = _binding!!
    private val vm by lazy { ViewModelProvider(requireParentFragment()).get<ScheduleViewModel>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sheetBehavior = BottomSheetBehavior.from(binding.filterSheet)
        val peekHeight = sheetBehavior.peekHeight
        binding.root.doOnApplyWindowInsets { _, insets, _, _ ->
            sheetBehavior.peekHeight = insets.systemWindowInsetBottom + peekHeight
        }

        binding.filterHeaderRv.isNestedScrollingEnabled = false
        binding.filterHeaderRv.adapter = FilterHeaderChipAdapter(requireContext())

        binding.filterContentRv.run {
            doOnApplyWindowInsets { v, insets, padding, _ ->
                v.updatePadding(bottom = padding.bottom + insets.systemGestureInsets.bottom)
            }
            (layoutManager as GridLayoutManager).run {
                spanCount = 2
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when ((adapter as ScheduleFilterAdapter).currentList[position]) {
                            is SessionFilter.TagFilter,
                            is SessionFilter.TypeFilter,
                            is SessionFilter.LangFilter -> 1
                            else -> 2
                        }
                    }
                }
            }
            adapter = ScheduleFilterAdapter(requireContext()) { filter ->
                when (filter) {
                    is SessionFilter.StarredFilter -> vm.toggleStarFilter()
                    is SessionFilter.TagFilter -> vm.toggleFilterTag(filter.tag.id)
                    is SessionFilter.TypeFilter -> vm.toggleFilterType(filter.type.id)
                    is SessionFilter.LangFilter -> vm.toggleLangType(filter.lang.id)
                }
            }
        }

        binding.collapseButton.setOnClickListener {
            sheetBehavior.collapseOrHide()
        }
        binding.clearButton.setOnClickListener {
            vm.clearFilter()
        }
        binding.expand.setOnClickListener {
            if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.filterSheet.post { updateSheetView(sheetBehavior.approxSlideOffset(), sheetBehavior) }
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                updateSheetView(slideOffset, sheetBehavior)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })

        vm.shouldFilterSheetCollapse.observe(viewLifecycleOwner) { collapse ->
            sheetBehavior.isHideable = !collapse
            sheetBehavior.skipCollapsed = !collapse
            if (!collapse && sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        MediatorLiveData<List<SessionFilter>>().apply {
            val update = update@{
                val starredOnly = vm.showStarredOnly.value!!
                val types = vm.types.value ?: return@update
                val tags = vm.tags.value ?: return@update
                val langList = vm.langList.value
                val selectedTypes = vm.selectedTypeIds.value!!
                val selectedTags = vm.selectedTagIds.value!!
                val selectedLangList = vm.selectedLangIds.value!!
                val filters = mutableListOf<SessionFilter>(SessionFilter.StarredFilter(starredOnly))
                types.map { type ->
                    SessionFilter.TypeFilter(type, selectedTypes.contains(type.id))
                }.let(filters::addAll)
                tags.map { tag ->
                    SessionFilter.TagFilter(tag, selectedTags.contains(tag.id))
                }.let(filters::addAll)
                langList?.map { sessionLang ->
                    SessionFilter.LangFilter(sessionLang, selectedLangList.contains(sessionLang.id))
                }?.let(filters::addAll)
                value = filters
            }
            addSource(vm.showStarredOnly) { update() }
            addSource(vm.types) { update() }
            addSource(vm.tags) { update() }
            addSource(vm.langList) { update() }
            addSource(vm.selectedTypeIds) { update() }
            addSource(vm.selectedTagIds) { update() }
            addSource(vm.selectedLangIds) { update() }
        }.observe(viewLifecycleOwner) { filters ->
            (binding.filterHeaderRv.adapter as FilterHeaderChipAdapter).submitList(filters.filter { f -> f.isActivated })
            (binding.filterContentRv.adapter as ScheduleFilterAdapter).submitFilters(filters)
        }

        MediatorLiveData<Any>().apply {
            val update = {
                value = Any()
            }
            addSource(vm.hasAnyFilter) { update() }
            addSource(vm.groupedSessionsToShow) { update() }
        }
            .debounce(150)
            .observe(viewLifecycleOwner) {
                if (vm.hasAnyFilter.value != true) {
                    binding.filterTitle.setText(R.string.filter)
                    return@observe
                }
                val sessions = vm.groupedSessionsToShow.value ?: return@observe
                var count = 0
                for (day in sessions.values) {
                    count += day.size
                }
                binding.filterTitle.text = resources.getQuantityString(R.plurals.n_matches, count, count)
            }
    }

    private fun updateSheetView(slideOffset: Float, sheetBehavior: BottomSheetBehavior<*>) {
        val offset = slideOffset.coerceIn(0F, 1F)
        val filterContentAlpha = if (sheetBehavior.skipCollapsed) 1F else MathUtils.lerp(0F, 1F, offset)
        val peekAlpha = if (sheetBehavior.skipCollapsed) 0F else MathUtils.lerp(1F, 0F, offset)
        binding.filterTitle.alpha = filterContentAlpha
        binding.filterContentRv.alpha = filterContentAlpha
        binding.collapseButton.alpha = filterContentAlpha
        binding.clearButton.alpha = peekAlpha
        binding.filterHeaderRv.alpha = peekAlpha

        binding.collapseButton.run {
            isClickable = offset == 1F
            isGone = alpha == 0F
        }
        binding.clearButton.run {
            isClickable = offset == 0F
            isGone = alpha == 0F
        }
    }
}

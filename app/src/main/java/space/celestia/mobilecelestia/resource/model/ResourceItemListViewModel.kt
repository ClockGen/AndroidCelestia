package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResourceItemListViewModel @Inject constructor(
    repository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : AsyncListPagingViewModel() {
    val category: ResourceCategory = savedStateHandle.get(ARG_CATEGORY)!!
    val language: String = savedStateHandle.get(ARG_LANG)!!
    override val itemsWithoutSeparators =
        repository.getItems(category = category.id, language = language).map { it.map { it as AsyncListItem } }

    override val stylized: Boolean
        get() = true

    companion object {
        const val ARG_CATEGORY = "category"
        const val ARG_LANG = "lang"
    }
}
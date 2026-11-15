package yos.music.player.data.objects

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import yos.music.player.data.libraries.Folder
import yos.music.player.data.libraries.YosMediaItem

@Stable
object LibraryObject {
    @Stable
    private val targetAlbumName = mutableStateOf("")
    fun setTargetAlbumName(name: String) {
        targetAlbumName.value = name
    }

    fun getTargetAlbumName(): String {
        return targetAlbumName.value
    }

    @Stable
    private val targetArtistName = mutableStateOf("")
    fun setTargetArtistName(name: String) {
        targetArtistName.value = name
    }

    fun getTargetArtistName(): String = targetArtistName.value

    @Stable
    private val targetList: MutableState<List<YosMediaItem>> = mutableStateOf(emptyList())
    @Stable
    private val targetListTitle = mutableStateOf("")
    @Stable
    private val targetKeepsOrder = mutableStateOf(false)
    // keepOrder: show the list as given (e.g. history, newest first) instead of the user's song sort
    fun setTargetListWithTitle(title: String, list: List<YosMediaItem>, keepOrder: Boolean = false) {
        targetListTitle.value = title
        targetList.value = list
        targetKeepsOrder.value = keepOrder
    }
    fun targetKeepsOrder(): Boolean = targetKeepsOrder.value
    fun getTargetListWithTitle(): Pair<String, List<YosMediaItem>> {
        return Pair(targetListTitle.value, targetList.value)
    }
}
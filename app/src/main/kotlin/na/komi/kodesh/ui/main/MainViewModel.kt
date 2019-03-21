package na.komi.kodesh.ui.main

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import na.komi.kodesh.Prefs
import na.komi.kodesh.model.Bible
import na.komi.kodesh.model.MainRepository
import na.komi.kodesh.util.Coroutines
import na.komi.kodesh.util.livedata.LiveEvent
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


class MainViewModel internal constructor(
    private val mainRepository: MainRepository
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    private val bg = Dispatchers.IO

    var kjvStyling = Prefs.kjvStylingPref//Prefs.kjvStylingPref


    var showDropCap = Prefs.dropCapPref//LiveEvent(Prefs.dropCapPref)
    var showParagraphs = Prefs.pBreakPref//LiveEvent(Prefs.pBreakPref)
    var showRedLetters = Prefs.redLetterPref//LiveEvent(Prefs.redLetterPref)
    var showVerseNumbers = Prefs.verseNumberPref
    var seperateVerses = Prefs.seperateVersePref
    var fromAdapterNotify = false
    val pagePosition by lazy { LiveEvent(Prefs.VP_Position) }
    val verseNumberPref by lazy { Prefs.verseNumberPref }
    val textSize by lazy { LiveEvent(Prefs.mainFontSize) }
    val progressBarStatus by lazy { LiveEvent(false) }

    val currentBook by lazy { LiveEvent(1) }
    val currentChapter by lazy { LiveEvent(1) }
    val currentChapterAmount by lazy { LiveEvent(1) }
    val currentVerse by lazy { LiveEvent(1) }
    val currentVerseAmount by lazy { LiveEvent(1) }
    val gotoPage by lazy { LiveEvent(0) }

    val buttonState by lazy { LiveEvent(false) }
    val currentScroll by lazy { LiveEvent(Prefs.currentScroll) }
    var versePicked: Int = 1

    private val _adapterUpdate by lazy { MutableLiveData<Boolean>() }
    val adapterUpdate: LiveData<Boolean>
        get() = _adapterUpdate

    fun setAdapterUpdate(state: Boolean) {
        _adapterUpdate.value = state
    }

    val currentLowProfileFlag by lazy { LiveEvent(false) }
    private val _lowProfileFlag by lazy { LiveEvent(true) }
    val lowProfileFlag
        get() = _lowProfileFlag

    fun setLowProfileFlag(flag: Boolean) {
        _lowProfileFlag.postValue(flag)
    }

    var MainRecyclerViewState: Parcelable? = null

    val pagesList =
        LivePagedListBuilder(
            mainRepository.getPagesSource(),
            PagedList.Config.Builder()
                .setPageSize(3)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(true)
                .build()
        ).setFetchExecutor(executorDispatcher.executor)
            .build()

    private var _executorDispatcher: ExecutorCoroutineDispatcher? = null
    val executorDispatcher: ExecutorCoroutineDispatcher
        get() = _executorDispatcher ?: Executors.newCachedThreadPool().asCoroutineDispatcher()

    val KEY_RECYCLER_STATE = "recycler_state"
    var mBundleRecyclerViewState: Bundle? = null

    fun getBooks() = runBlocking(IO) { mainRepository.getBooks() }

    fun getChapterAmount(bookID: Int) = runBlocking(IO) { mainRepository.getChapterAmount(bookID) }

    fun getVerseAmount(bookID: Int, chapterID: Int) =
        runBlocking(IO) { mainRepository.getVerseAmount(bookID, chapterID) }

    fun getPagePosition(bookID: Int) = runBlocking(IO) { mainRepository.getPageForBook(bookID) }

    fun getVersesRaw(book: Int, chapter: Int) = mainRepository.getVersesRaw(book, chapter)

    fun getRowAtPagePositon(i: Int) = runBlocking(IO) { mainRepository.getRowAtPagePositon(i) }

    var longClicked = false //LiveEvent(false)
    private val _list by lazy { MutableLiveData<List<Bible>>() }
    val list: LiveData<List<Bible>> get() = _list

    private val _item by lazy { LiveEvent(false) }
    val item: LiveData<Boolean>
        get() = _item

    fun setItem(flag: Boolean) {
        _item.postValue(flag)
    }

    // DataSource documentation states that it runs immediately
    fun getPages2(): LiveData<List<Bible>> {

        if (_list.value == null)
            Coroutines.ioThenMain({
                mainRepository.getPages()
            }) {
                _list.postValue(it)
            }

        return list
    }

    fun searchVerse(query: String) = runBlocking(Dispatchers.IO) {
        // Outputs a new livedata on each call
        LivePagedListBuilder(
            mainRepository.getVerses(query),
            PagedList.Config.Builder()
                .setPageSize(PAGE_SIZE)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .setEnablePlaceholders(ENABLE_PLACEHOLDERS)
                .build()
        )
            .build()
    }

    companion object {
        private const val PAGE_SIZE = 30
        private const val PREFETCH_DISTANCE = 30
        private const val ENABLE_PLACEHOLDERS = true
    }
}

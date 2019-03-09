package na.komi.kodesh.model

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.sqlite.db.SimpleSQLiteQuery

/**
 * Repository module for handling data operations.
 * Java singleton
 */
//class MainRepository private constructor(private val mainDao: MainDao) {
class MainRepository constructor(private val mainDao: MainDao) {

    fun getAll() = mainDao.getAll()

    fun getRow(position: Int) = mainDao.getRow(position)

    @WorkerThread
    fun getPageForBook(book: Int) = mainDao.getPageForBook(book)

    @WorkerThread
    fun getPages() = mainDao.getPages()

    fun getPagesSource() = mainDao.getPagesSource()

    fun getBooks() = mainDao.getBooks()

    fun getChapterAmount(bookID: Int) = mainDao.getChapterAmount(bookID)

    fun getVerseAmount(bookID: Int, chapterID: Int) = mainDao.getVerseAmount(bookID, chapterID)

    fun getVersesRaw(book: Int, chapter: Int) = mainDao.getVersesRaw(SimpleSQLiteQuery("SELECT * FROM Bible WHERE book_id=? AND chapter_id=?", arrayOf<Any>(book, chapter)))

    @UiThread
    fun getRowAtPagePositon(position:Int) = mainDao.getRowAtPagePositon(position)

   fun getVerses(query: String) = mainDao.getVerses(query)

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: MainRepository? = null

        fun getInstance(mainDao: MainDao) =
                instance ?: synchronized(this) {
                    instance ?: MainRepository(mainDao).also { instance = it }
                }
    }
}

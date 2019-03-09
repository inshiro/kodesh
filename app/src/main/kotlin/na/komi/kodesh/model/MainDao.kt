package na.komi.kodesh.model

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * The Data Access Object for the Verse class.
 */
@Suppress("ReplaceArrayOfWithLiteral")
@Dao
interface MainDao {
    @Query("SELECT * from Bible WHERE book_id=1 ORDER BY chapter_id")
    fun getAll(): DataSource.Factory<Int, Bible>

    @Query("SELECT * from Bible GROUP BY book_id, chapter_id")
    fun getPages(): List<Bible>

    @Query("SELECT * from Bible GROUP BY book_id, chapter_id")
    fun getPagesSource(): DataSource.Factory<Int, Bible>

    @Query("SELECT * from Bible GROUP BY book_id, chapter_id LIMIT 1 OFFSET :position")
    fun getRow(position: Int): Bible

    @Query("SELECT count(*) from (SELECT * from Bible GROUP BY book_id, chapter_id) as g where g.book_id<=(:book) and id>0;")
    fun getPageForBook(book: Int): Int

    @Query("SELECT DISTINCT book_name from Bible")
    fun getBooks(): Array<String>

    @Query("SELECT DISTINCT chapter_id from Bible WHERE book_id=(:bookID) ORDER BY chapter_id DESC LIMIT 1")
    fun getChapterAmount(bookID: Int): Int

    @Query("SELECT DISTINCT verse_id from Bible WHERE book_id=(:bookID) AND chapter_id=(:chapterID) ORDER BY verse_id DESC LIMIT 1")
    fun getVerseAmount(bookID: Int, chapterID: Int): Int

    @RawQuery(observedEntities = arrayOf(Bible::class))
    fun getRow(query: SupportSQLiteQuery): Bible

    @RawQuery(observedEntities = arrayOf(Bible::class))
    fun getVersesRaw(query: SupportSQLiteQuery): List<Bible>

    @Query("SELECT * FROM(SELECT * from Bible GROUP BY book_id, chapter_id limit (:position) ) ORDER BY id DESC LIMIT 1")
    fun getRowAtPagePositon(position:Int): Bible

    @Query("SELECT * FROM Bible WHERE REPLACE(REPLACE(verse_text, '[', ''), ']','') LIKE '%' || :query || '%'")
    fun getVerses(query: String): DataSource.Factory<Int, Bible>
}

package na.komi.kodesh.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Bible")
data class Bible(

        @ColumnInfo(name = "book_id") var bookId: Int? = 1,
        @ColumnInfo(name = "book_abbr") var bookAbbr: String? = "",
        @ColumnInfo(name = "book_name") var bookName: String? = "",
        @ColumnInfo(name = "chapter_id") var chapterId: Int? = 1,
        @ColumnInfo(name = "verse_id") var verseId: Int? = 1,
        @ColumnInfo(name = "verse_text") var verseText: String? = "",
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: Int,
        @ColumnInfo(name = "section") var section: String? = ""

)
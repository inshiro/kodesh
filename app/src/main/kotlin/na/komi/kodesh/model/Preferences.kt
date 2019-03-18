package na.komi.kodesh.model

/**
 * A helper class to get or set preferences.
 * You must first initialize your values with PreferenceManager.setDefaultValues()
 */
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import na.komi.kodesh.BuildConfig

class Preferences (context: Context) {
    private val PREFS_FILENAME = "${BuildConfig.APPLICATION_ID}.Preferences"
    private val BOOK = "BOOKID"
    private val CHAPTER = "CHAPTER"
    private val BOOK_LENGTH = "BOOK_LENGTH"
    private val CHAPTER_LENGTH = "CHAPTER_LENGTH"
    private val DAY_MODE = "DAY_MODE"
    private val VP_POSITION = "VP_POSITION"
    private val MAIN_FONT_SIZE = "MAIN_FONT_SIZE"
    private val FONT_SIZE = "FONT_SIZE"
    private val THEME_ID = "THEME_ID"
    private val KJVSTYLE_ID = "KJVSTYLE_ID"
    private val DROP_CAP_ID = "DROP_CAP_ID"
    private val PBREAK_ID = "PBREAK_ID"
    private val RED_LETTER_ID = "RED_LETTER_ID"
    private val VERSE_NUMBERS = "VERSE_NUMBERS"
    private val NEW_LINE_EACH_VERSE_ID = "NEW_LINE_EACH_VERSE_ID"
    private val HEADINGS_ID = "HEADINGS_ID"
    private val FOOTINGS_ID = "FOOTINGS_ID"
    private val SCROLLY_ID = "SCROLLY_ID"
    private val NAVIGATE_TO_POSITION = "NAVIGATE_TO_POSITION"
    private val SCROLL_STRING = "SCROLL_STRING"
    private val SCALE_FACTOR = "SCALE_FACTOR"
    private val TITLE_ID = "TITLE_ID"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)

    var Book: Int
        get() = prefs.getInt(BOOK, 1)
        set(value) = prefs.edit().putInt(BOOK, value).apply()
    var Chapter: Int
        get() = prefs.getInt(CHAPTER, 1)
        set(value) = prefs.edit().putInt(CHAPTER, value).apply()
    var BookLength: Int
        get() = prefs.getInt(BOOK_LENGTH, 50)
        set(value) = prefs.edit().putInt(BOOK_LENGTH, value).apply()
    var ChapterLength: Int
        get() = prefs.getInt(CHAPTER_LENGTH, 31)
        set(value) = prefs.edit().putInt(CHAPTER_LENGTH, value).apply()
    var dayMode: Boolean
        get() = prefs.getBoolean(DAY_MODE, true)
        set(value) = prefs.edit().putBoolean(DAY_MODE, value).apply()
    var VP_Position: Int
        get() = prefs.getInt(VP_POSITION, 0)
        set(value) = prefs.edit().putInt(VP_POSITION, value).apply()
    var textSizeMultiplier: Float
        get() = prefs.getFloat(FONT_SIZE, 1f)
        set(value) = prefs.edit().putFloat(FONT_SIZE, value).apply()
    var mainFontSize: Float
        get() = prefs.getFloat(MAIN_FONT_SIZE, 26f)
        set(value) = prefs.edit().putFloat(MAIN_FONT_SIZE, value).apply()
    var scaleFactor: Float
        get() = prefs.getFloat(SCALE_FACTOR, 5f)
        set(value) = prefs.edit().putFloat(SCALE_FACTOR, value).apply()
    var themeId: Int
        get() = prefs.getInt(THEME_ID, 0)
        set(value) = prefs.edit().putInt(THEME_ID, value).apply()
    var kjvStylingPref: Boolean
        get() = prefs.getBoolean(KJVSTYLE_ID, false)
        set(value) = prefs.edit().putBoolean(KJVSTYLE_ID, value).apply()
    var dropCapPref: Boolean
        get() = prefs.getBoolean(DROP_CAP_ID, true)
        set(value) = prefs.edit().putBoolean(DROP_CAP_ID, value).apply()
    var pBreakPref: Boolean
        get() = prefs.getBoolean(PBREAK_ID, false)
        set(value) = prefs.edit().putBoolean(PBREAK_ID, value).apply()
    var redLetterPref: Boolean
        get() = prefs.getBoolean(RED_LETTER_ID, true)
        set(value) = prefs.edit().putBoolean(RED_LETTER_ID, value).apply()
    var verseNumberPref: Boolean
        get() = prefs.getBoolean(VERSE_NUMBERS, true)
        set(value) = prefs.edit().putBoolean(VERSE_NUMBERS, value).apply()
    var seperateVersePref: Boolean
        get() = prefs.getBoolean(NEW_LINE_EACH_VERSE_ID, true)
        set(value) = prefs.edit().putBoolean(NEW_LINE_EACH_VERSE_ID, value).apply()
    var currentScroll: Int
        get() = prefs.getInt(SCROLLY_ID, 0)
        set(value) = prefs.edit().putInt(SCROLLY_ID, value).apply()
    var NavigateToPosition: Int
        get() = prefs.getInt(NAVIGATE_TO_POSITION, -1)
        set(value) = prefs.edit().putInt(NAVIGATE_TO_POSITION, value).apply()
    var ScrollString: String?
        get() = prefs.getString(SCROLL_STRING, "")
        set(value) = prefs.edit().putString(SCROLL_STRING, value).apply()
    var title: String
        get() = prefs.getString(TITLE_ID, "") ?: ""
        set(value) = prefs.edit().putString(TITLE_ID, value).apply()
}
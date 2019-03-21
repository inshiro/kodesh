package na.komi.kodesh.util.page
import android.graphics.Color
import androidx.core.content.ContextCompat
import na.komi.kodesh.Application
import na.komi.kodesh.R
import na.komi.kodesh.util.text.count
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.LinkedList
import java.util.regex.Pattern

object Formatting {
    private const val KJV_PATH = "databases/KJV1611.txt"
    private const val RED_LETTER_PATH = "databases/RedLetters.txt"
    private const val PARAGRAPH_PATH = "databases/ParagraphList.txt"

    val Italics by lazy { CustomTypefaceSpan(Fonts.GentiumPlus_I) }
    val NumColor by lazy { Color.parseColor("#877f66") }
    val WhiteColor by lazy { Color.parseColor("#ffffff") }
    val Transparent by lazy { Color.TRANSPARENT }
    val RedLetterColor by lazy { ContextCompat.getColor(Application.instance.applicationContext, R.color.redletter_color_dark) }
    val HighLightColor by lazy { ContextCompat.getColor(Application.instance.applicationContext, R.color.highlight_color_dark) }
    val HighlightFocusColor by lazy { ContextCompat.getColor(Application.instance.applicationContext, R.color.highlight_focus_color) }
    val SearchNotFoundColor by lazy { ContextCompat.getColor(Application.instance.applicationContext, R.color.search_not_found) }
    val ColorAccent by lazy { ContextCompat.getColor(Application.instance.applicationContext, R.color.colorAccent) }
    val DefaultSelectColor by lazy { ContextCompat.getColor(Application.instance.baseContext, R.color.colorAccent) }
    //val typeface by lazy(LazyThreadSafetyMode.NONE) { Typeface.create("sans-serif", Typeface.NORMAL) }

    private fun getList(path: String): List<CharSequence> {
        var reader: BufferedReader? = null
        var l: List<CharSequence>? = null
        try {
            reader = BufferedReader(InputStreamReader(Application.instance.baseContext.assets.open(path), "UTF-8"))
            l = reader.readLines()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return l!!
    }

    val kjvList: List<CharSequence> by lazy { getList(KJV_PATH) }
    val paragraphList: List<CharSequence> by lazy { getList(PARAGRAPH_PATH) }
    val redLetterList: List<CharSequence> by lazy { getList(RED_LETTER_PATH) }
    private val dmp by lazy { diff_match_patch() }
    private val punct by lazy { "[.,;:?!]".toRegex() }
    private val lettersOnly by lazy { "[^\\p{P}]".toRegex() }
    private val andPattern by lazy { Pattern.compile("(?:&|and)", Pattern.CASE_INSENSITIVE).toRegex() }

    fun diffText(pce: String, kjv: CharSequence): String {
        if (pce.length <= 1) return pce
        val list = dmp.diff_main(pce, kjv)
        dmp.diff_cleanupSemanticLossless(list)
        val diffList = LinkedList<diff_match_patch.Diff>()

        var cappedWord = false
        var andWord = false
        var doublePunct = false
        for(idx in list.indices) {

            // Skip the pair of two words
            if (cappedWord || doublePunct || andWord) {
                when {
                    doublePunct -> {
                        doublePunct = false
                        val punctOnly = list[idx].text!!.replace(lettersOnly, "")
                        val s = list[idx - 1].text!!.replace(punct, punctOnly)
                        diffList.add(diff_match_patch.Diff(list[idx].operation, s))
                    }
                    andWord -> {
                        andWord = false
                        diffList.add(list[idx])
                    }
                    else -> {
                        cappedWord = false
                        diffList.add(list[idx])
                    }
                }
                continue // Add new text, then skip
            }

            // Capitalisations diffs  e.g KING; / King, | and / & diffs | punctuation diffs e.g fair; / faire,
            if (idx + 1 < list.size && list[idx].operation != diff_match_patch.Operation.EQUAL && list[idx + 1].operation != diff_match_patch.Operation.EQUAL) {
                if (list[idx].text!!.contains(andPattern) && list[idx + 1].text!!.contains(andPattern)) {
                    andWord = true
                    continue // Skip
                } else if (list[idx].text!!.contains(punct) && list[idx + 1].text!!.contains(punct)) {
                    if (
                            (list[idx].operation == diff_match_patch.Operation.DELETE && list[idx + 1].operation == diff_match_patch.Operation.INSERT)
                            || (list[idx].operation == diff_match_patch.Operation.INSERT && list[idx + 1].operation == diff_match_patch.Operation.DELETE)
                    )
                        doublePunct = true
                    continue // Skip
                } else if (
                        {
                            val s0 = list[idx].text!!.replace(punct, "").trim()
                            val s1 = list[idx + 1].text!!.replace(punct, "").trim()
                            s0.isNotBlank() && s1.isNotBlank() && s0.equals(s1, ignoreCase = true)
                        }()

                ) {
                    cappedWord = true
                    continue // Skip
                }
            }

            // Swap delete with insert to keep original text
            if (list[idx].operation == diff_match_patch.Operation.DELETE)
                diffList.add(diff_match_patch.Diff(diff_match_patch.Operation.INSERT, list[idx].text))

            // Filter to only get original text and new punctuation
            else if (list[idx].operation == diff_match_patch.Operation.EQUAL || list[idx].text!!.contains(punct)) {
                if (list[idx].operation == diff_match_patch.Operation.INSERT)
                    diffList.add(diff_match_patch.Diff(list[idx].operation, list[idx].text!!.replace(lettersOnly, "")))
                else
                    diffList.add(list[idx])
            }
        }

        return dmp.diff_text2(diffList)
    }

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    fun redLetterPositions(index: Int, block: (start: Int, end: Int, countSoFar: Int) -> Unit) {

        val it = redLetterList[index]
        val text = it.substring(it.lastIndexOf('\t') + 1)

        var e = 0
        text.count("{") { s, _, c ->
            e = s
            e = text.indexOf('}', e + 1) - 1
            if (e >= 0)
                block(s, e, c)

        }
    }

}
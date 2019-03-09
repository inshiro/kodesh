package na.komi.kodesh.util.text

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import na.komi.kodesh.util.IO_EXECUTOR
import na.komi.kodesh.util.log

/**
 * Count Occurrences of a String in a String
 */
inline fun CharSequence.count(sub: String, ignoreCase: Boolean = false, action: (start: Int, end: Int, countSoFar: Int) -> Unit = { _, _, _ -> }): Int {
    var count = 0
    var startIdx = 0
    while ({ indexOf(sub, startIdx, ignoreCase = ignoreCase).also { startIdx = it + 1 } }() >= 0) {
        count++
        action(startIdx - 1, startIdx - 1 + sub.length, count)
    }
    return count
}

inline fun CharSequence.count(sub: Char, action: (start: Int, end: Int, countSoFar: Int) -> Unit = { _, _, _ -> }): Int {
    var count = 0
    var startIdx = 0
    while (indexOf(sub, startIdx).also { startIdx = it + 1 } >= 0) {
        count++
        action(startIdx - 1, startIdx, count)
    }
    return count
}

inline fun SpannableStringBuilder.withSpan(span: Any, action: SpannableStringBuilder.() -> Unit = {}): SpannableStringBuilder {
    val from = length
    action()
    setSpan(span, if (from == length) 0 else from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

fun AppCompatTextView.futureSet(charSequence: CharSequence) {
    setTextFuture(PrecomputedTextCompat.getTextFuture(charSequence, this.textMetricsParamsCompat, null))
    //val future = PrecomputedTextCompat.getTextFuture(charSequence, this.textMetricsParamsCompat, IO_EXECUTOR)
    //this.setTextFuture(future!!)
}

/**
 * Removes all spans given a class
 */
fun <T> Spannable.removeSpans(start: Int, end: Int, aClass: Class<T>) {
    val spans = getSpans(start, end, aClass)
    if (spans.isNotEmpty()) {
        for (span in spans) {
            removeSpan(span)
        }
    }
}

fun SpannableStringBuilder.deleteAll(str: String): SpannableStringBuilder{
    var i = 0
    while(i >=0 && indexOf(str,i).also { i = it } >= 0 )
        delete(i,i+str.length)
    return this
}
fun SpannableStringBuilder.spanBetween(what: Any, begin: String, end: String, removeDelimiters: Boolean): SpannableStringBuilder {

    val e = indexOf(end)
    if (e >= 0) {
        if (removeDelimiters)
            delete(e, e + end.length)
        val s = indexOf(begin)
        if (s >= 0) {
            if (removeDelimiters)
                delete(s, s + begin.length)
            setSpan(CharacterStyle.wrap(what as CharacterStyle), s, e - begin.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    return this

}

/** https://stackoverflow.com/a/34697667 **/
fun SpannableStringBuilder.spanBetweenEach(what: Any, begin: String, end: String, removeDelimiters: Boolean): SpannableStringBuilder {
    var i = indexOf(begin)
    while (i.also {i = indexOf(begin,  i+1) } >= 0) {
        spanBetween(CharacterStyle.wrap(what as CharacterStyle), begin, end, removeDelimiters)

    }
    return this
}

fun SpannableStringBuilder.capitalizeUntil(search: String): SpannableStringBuilder {
    val found = indexOf(search)
    if (found >= 0) {
        val newStr = substring(0, found).toUpperCase()
        delete(0, found)
        insert(0, newStr)
    }
    return this
}

fun StringBuilder.capitalizeUntil(search: String): StringBuilder {
    val found = indexOf(search)
    if (found >= 0) {
        val newStr = substring(0, found).toUpperCase()
        delete(0, found)
        insert(0, newStr)
    }
    return this
}
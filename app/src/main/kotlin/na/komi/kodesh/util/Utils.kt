package na.komi.kodesh.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DimenRes
import na.komi.kodesh.Application
import na.komi.kodesh.BuildConfig
import java.util.regex.Pattern

object log {

    infix fun d(message: String) {
        if (BuildConfig.DEBUG)
            Log.d(getTag(), message)
    }

    infix fun v(message: String) {
        if (BuildConfig.DEBUG)
            Log.v(getTag(), message)
    }

    infix fun i(message: String) {
        if (BuildConfig.DEBUG)
            Log.i(getTag(), message)
    }

    infix fun w(message: String) {
        //if (BuildConfig.DEBUG)
        Log.w(getTag(), message)
    }

    infix fun e(message: String) {
        //if (BuildConfig.DEBUG)
        Log.e(getTag(), message)
    }

    infix fun wtf(message: String) {
        //if (BuildConfig.DEBUG)
        Log.wtf(getTag(), message)
    }


    private val CALL_STACK_INDEX = 5
    private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    private val name = BuildConfig.APPLICATION_ID.substring(BuildConfig.APPLICATION_ID.indexOfLast { it == '.' } + 1)
    private fun getTag(): String {
        /*val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= CALL_STACK_INDEX) {
            throw IllegalStateException(
                "Synthetic stacktrace didn't have enough elements: are you using proguard?"
            )
        }*/
        return name//createStackElementTag(stackTrace[CALL_STACK_INDEX])
    }

    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }
}

inline fun tryy(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        Log.wtf("TAG", e)
        //throw RuntimeException(e)//Log.d("TAG", e.message)//e.printStackTrace()
    }
}


inline fun measureTimeMillis(block: () -> Unit): Long {
    val startTime = System.currentTimeMillis()
    block.invoke()
    return System.currentTimeMillis() - startTime
}

inline fun benchmark(range: IntRange, block: () -> Unit): Double {
    return (range).map {
        measureTimeMillis { block() }
    }.average()
}


fun Context.dimen(@DimenRes id: Int): Float = resources.getDimension(id)
inline val Float.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

inline val Int.dpToPx: Int
    get() = toFloat().dpToPx.toInt()

inline val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

inline val Int.pxToDp: Int
    get() = toFloat().pxToDp.toInt()

inline val Float.dpToSp: Float
    get() = this * Resources.getSystem().displayMetrics.scaledDensity

inline val Int.dpToSp: Int
    get() = toFloat().dpToSp.toInt()

inline val Float.spToDp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity

inline val Int.spToDp: Int
    get() = toFloat().spToDp.toInt()

fun View.hideKeyboard() {
    val inputMethodManager = Application.instance.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun dp(size: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    size,
    Application.instance.applicationContext.resources.displayMetrics
)

fun MutableList<Int>.groupConsecutiveString(): String {
    val listMain = this.groupConsecutive()
    var str = ""
    listMain.forEachIndexed { idx, it ->
        if(it.size > 1) str += "${it.min()}-${it.max()}"
        else
            str += it.get(0)
        if(str.isNotEmpty() && idx != listMain.size-1) str += ", "
    }
    return str
}
fun MutableList<Int>.groupConsecutive(): MutableList<List<Int>> {
    this.sort()

    val listMain = ArrayList<List<Int>>()
    var temp: MutableList<Int> = ArrayList()

    for (i in this.indices) {
        if (i + 1 < this.size && this[i] + 1 == this[i + 1]) {
            temp.add(this[i])
        } else {
            temp.add(this[i])
            listMain.add(temp)
            temp = ArrayList()
        }

    }
    return listMain.toMutableList()
}
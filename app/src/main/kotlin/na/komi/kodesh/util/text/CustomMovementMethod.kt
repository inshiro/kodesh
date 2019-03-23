package na.komi.kodesh.util.text

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView


class CustomMovementMethod : LinkMovementMethod() {

    companion object {
        private val LONG_CLICK_TIME = 500


        val instance: LinkMovementMethod
            get() {
                if (sInstance == null) {
                    sInstance = CustomMovementMethod()
                }
                return sInstance!!
            }
        private var sInstance: CustomMovementMethod? = null
    }

    override fun canSelectArbitrarily(): Boolean {
        return true
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.setSelection(text, text.length)
    }

    override fun onTakeFocus(view: TextView, text: Spannable, dir: Int) {
        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        if (dir and (View.FOCUS_FORWARD or View.FOCUS_DOWN) !== 0) {
            if (view.layout == null) {
                // This shouldn't be null, but do something sensible if it is.
                Selection.setSelection(text, text.length)
            }
        } else {
            Selection.setSelection(text, text.length)
        }
    }

    override fun onTouchEvent(
        widget: TextView, buffer: Spannable,
        event: MotionEvent
    ): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(off, off, ClickableSpan::class.java)

            if (link.size != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
                return true
            }
        }

        return Touch.onTouchEvent(widget, buffer, event)
    }
}
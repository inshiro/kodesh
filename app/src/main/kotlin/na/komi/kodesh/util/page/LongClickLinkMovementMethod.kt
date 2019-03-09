package na.komi.kodesh.util.page

import android.os.Handler
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.MotionEvent
import android.widget.TextView

/**
 * https://stackoverflow.com/a/31786969
 */
class LongClickLinkMovementMethod : LinkMovementMethod() {

    private var mLongClickHandler: Handler? = null
    private var mIsLongPressed = false

    override fun onTouchEvent(
        widget: TextView, buffer: Spannable,
        event: MotionEvent
    ): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_CANCEL) {
            if (mLongClickHandler != null) {
                mLongClickHandler!!.removeCallbacksAndMessages(null)
            }
        }

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

            val link = buffer.getSpans(off, off, LongClickableSpan::class.java)

            if (link.size != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (mLongClickHandler != null) {
                        mLongClickHandler!!.removeCallbacksAndMessages(null)
                    }
                    if (!mIsLongPressed) {
                        link[0].onClick(widget)
                    }
                    mIsLongPressed = false
                } else {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                    mLongClickHandler!!.postDelayed(Runnable {
                        link[0].onLongClick(widget)
                        mIsLongPressed = true
                    }, LONG_CLICK_TIME.toLong())
                }
                return true
            }
        }

        return super.onTouchEvent(widget, buffer, event)
    }

    companion object {
        private val LONG_CLICK_TIME = 500


        val instance: MovementMethod
            get() {
                if (sInstance == null) {
                    sInstance = LongClickLinkMovementMethod()
                    sInstance!!.mLongClickHandler = Handler()
                }

                return sInstance!!
            }
        private var sInstance: LongClickLinkMovementMethod? = null
    }
}
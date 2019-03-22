package na.komi.kodesh.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import na.komi.kodesh.Prefs
import na.komi.kodesh.util.log
import na.komi.kodesh.util.page.Fonts
import na.komi.kodesh.util.sp
import kotlin.math.absoluteValue


class LayoutedTextView : AppCompatTextView {

    private var mOnLayoutListener: OnLayoutListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    var showDropCap = false
    var dropCapText = ""
    var showDropCapMargin = false

    interface OnLayoutListener {
        fun onLayouted(view: AppCompatTextView)
    }

    fun setOnLayoutListener(listener: OnLayoutListener) {
        mOnLayoutListener = listener
    }

    fun removeOnLayoutListener(listener: OnLayoutListener) {
        mOnLayoutListener = listener
        mOnLayoutListener = null
    }

    override fun onLayout(
        changed: Boolean, left: Int, top: Int, right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        if (mOnLayoutListener != null) {
            mOnLayoutListener!!.onLayouted(this)
        }
    }

    val tp by lazy {
        TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
            typeface = Fonts.GentiumPlus_R
            textSize = sp(Prefs.mainFontSize * 4)
        }
    }
    var sLayout: StaticLayout? = null
    private var PADDING_BOUNDS = 20
    private val leadingMarginSpan by lazy { LeadingMarginSpan3() }
    fun getStaticLayout(str: CharSequence, textSize: Float, width: Int): StaticLayout {

        if (tp.textSize == textSize && sLayout != null) return sLayout!!
        if (tp.textSize != textSize || sLayout == null) {
            tp.textSize = textSize
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                log d "Create StaticLayout"
                val builder = StaticLayout.Builder.obtain(str, 0, str.length, tp, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(false)
                    .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
                return builder.build()
            } else {
                @Suppress("DEPRECATION")
                return StaticLayout(
                    str,
                    tp,
                    width,
                    Layout.Alignment.ALIGN_NORMAL,
                    1f,
                    0f,
                    false
                )
            }
        }
        return sLayout!!
    }

    /*
        override fun onDrawForeground(canvas: Canvas?) {
            if (!showDropCap) {super.onDrawForeground(canvas); return}
            if (canvas!=null) {
                //super.onDrawForeground(canvas)
                log d "Called onDrawForeground"
                val dLayout = getStaticLayout(text.take(1).toString(), width)
                dLayout.draw(canvas)
            }

        }
        */
    var drawn = false
    var drawCount = 0
    var hasPeriscope = false

    val MARGIN_PADDING = 20
    override fun onDraw(canvas: Canvas?) {
        if (!showDropCap) {
            //log d "onDraw"
            super.onDraw(canvas)
        } else {
            if (canvas != null) {
                // if (drawn) return
                //var moveY = y+height-(top+Math.abs(paint.descent()))
                //drawCount++
                //log w "onDraw $drawCount"


                var moveY = y + height - (top + Math.abs(paint.descent()))
                //log w "linecount: ${lineCount}"
                for (i in leadingMarginSpan.lineCount + 1..lineCount) {
                    moveY -= Math.abs(paint.fontMetrics.top) + Math.abs(paint.fontMetrics.bottom)
                }
                moveY += paint.fontMetrics.descent / 3
                //log w "movey: $moveY"

                if (hasPeriscope) {
                    // Getting n amount of line heights and multiplying that to the abs value of ascent (Bottom of first line text)
                    val line = layout.getLineForOffset(text.lastIndexOf("\n") + 3) + 1
                    val v = paint.fontMetrics.ascent.absoluteValue + (lineHeight.toFloat() * line)
                    //canvas.drawLine(0f, v, width.toFloat(), v + 5f, paint)
                    //log i "line for offset $line"
                    moveY = v
                }

                tp.textSize = paint.textSize * 4
                tp.color = paint.color

                // TOP of first line text
                // canvas.drawLine(0f, paint.fontMetrics.descent, width.toFloat(), paint.fontMetrics.descent + 5f, paint)
                // canvas.drawLine(0f, paint.fontMetrics.bottom, width.toFloat(), paint.fontMetrics.bottom + 5f, paint)
                // canvas.drawLine(0f, paint.fontMetrics.leading, width.toFloat(), paint.fontMetrics.leading + 5f, paint)

                // Bottom of first line text
                // canvas.drawLine(0f, moveY, width.toFloat(), moveY + 5f, paint)

                // Getting line height if TextView.getLineHeight() is not accessible. https://stackoverflow.com/a/16050019
                // val lh = paint.fontMetrics.top - paint.fontMetrics.bottom
                // log i "fm.top - fm.bottom: ${lh} line height: ${lineHeight}"

                val ss = text as Spannable
                val redSpans = ss.getSpans(0, 1, ForegroundColorSpan::class.java)
                for (redSpan in redSpans) {
                    tp.color = redSpan.foregroundColor
                }

                canvas.drawText(dropCapText, x + paddingLeft.toFloat(), moveY, tp)
                val spans = ss.getSpans(0, ss.length, LeadingMarginSpan3::class.java)
                for (span in spans) {
                    span.setMargin(tp.measureText(dropCapText).toInt() + MARGIN_PADDING)
                }
                super.onDraw(canvas)
            }
        }
    }

}

class LeadingMarginSpan3(private var margin: Int = 0, var lineCount: Int = 2) :
    LeadingMarginSpan.LeadingMarginSpan2 {

    override fun getLeadingMargin(first: Boolean): Int {
        return if (first) margin else 0
    }

    override fun getLeadingMarginLineCount(): Int {
        return lineCount
    }

    fun setMargin(size: Int) {
        margin = size
    }

    override fun drawLeadingMargin(
        c: Canvas?,
        p: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {

    }

}
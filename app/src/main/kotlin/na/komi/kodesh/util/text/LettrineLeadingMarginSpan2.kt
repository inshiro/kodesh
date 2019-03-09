package na.komi.kodesh.util.text

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import na.komi.kodesh.Prefs
import na.komi.kodesh.util.log
import na.komi.kodesh.util.page.Fonts
import na.komi.kodesh.util.sp
import kotlin.math.roundToInt


class LettrineLeadingMarginSpan2(private val str: String) :
    LeadingMarginSpan.LeadingMarginSpan2 {

    /**
     * Change detection from newline to whatever
     * So we can control where to start the margin
     */

    private var margin2 = 0
    private var PADDING_BOUNDS = 20

    override fun getLeadingMargin(first: Boolean): Int {
        return if (first) margin2 else 0
    }

    override fun getLeadingMarginLineCount(): Int {
        return 2
    }

    val bounds = Rect()
    var pp: Paint? = null
    var init = false
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
                    //.setMaxLines(1)
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

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int, text: CharSequence,
        start: Int, end: Int, first: Boolean, layout: Layout
    ) {
        //if (first && start - 2 >= 0) {
        if (!first) return

        tp.textSize = sp(Prefs.mainFontSize * 4)
        val tempLayout: StaticLayout = getStaticLayout(text.take(1).toString(), tp.textSize, layout.width)

        val lineCount = tempLayout.lineCount
        var textWidth = 0f
        for (i in 0 until lineCount) {
            textWidth += tempLayout.getLineWidth(i)
        }

        margin2 = textWidth.roundToInt() + PADDING_BOUNDS
        // if (!init)
        // Save coordinates beforehand
        c.save()

        c.translate(x.toFloat(), layout.paint.ascent() - PADDING_BOUNDS-10)
        tempLayout.draw(c)

        c.restore()

        //log d "Called drawleadingmargin $start ~ $end | DropCap: ${text.subSequence(start - 2, start ).take(1)} | margin2: $margin2"
        //}
    }

}
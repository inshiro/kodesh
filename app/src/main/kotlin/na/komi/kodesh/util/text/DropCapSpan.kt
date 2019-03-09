package na.komi.kodesh.util.text

import android.graphics.Paint
import android.text.style.LineHeightSpan
import android.graphics.Rect
import android.text.TextPaint

/**
 * Span that applies a margin after the initial string.
 * This is better that LeadingMarginSpan because that only
 * applies to the "first line of paragraph".
 *
 * This also preserves the text bounds so that Selection and
 * background color are left untouched. This prevents you from
 * having to use a DropCap + LineOverLapSpan + RestOfText
 */

// https://stackoverflow.com/a/12925719
class Height(private var mSize: Int = 0) : LineHeightSpan.WithDensity  {
    private var sProportion = 0f

    override fun chooseHeight(
        text: CharSequence, start: Int, end: Int,
        spanstartv: Int, v: Int,
        fm: Paint.FontMetricsInt
    ) {
        // Should not get called, at least not by StaticLayout.
        chooseHeight(text, start, end, spanstartv, v, fm, null)
    }

    override fun chooseHeight(
        text: CharSequence, start: Int, end: Int,
        spanstartv: Int, v: Int,
        fm: Paint.FontMetricsInt, paint: TextPaint?
    ) {
        var size = mSize
        if (paint != null) {
            size *= paint.density.toInt()
        }

        if (fm.bottom - fm.top < size) {
            fm.top = fm.bottom - size
            fm.ascent = fm.ascent - size
        } else {
            if (sProportion == 0f) {
                /*
                 * Calculate what fraction of the nominal ascent
                 * the height of a capital letter actually is,
                 * so that we won't reduce the ascent to less than
                 * that unless we absolutely have to.
                 */

                val p = Paint()
                p.textSize = 100f
                val r = Rect()
                p.getTextBounds("ABCDEFG", 0, 7, r)

                sProportion = r.top / p.ascent()
            }

            val need = Math.ceil((-fm.top * sProportion).toDouble()).toInt()

            if (size - fm.descent >= need) {
                /*
                 * It is safe to shrink the ascent this much.
                 */

                fm.top = fm.bottom - size
                fm.ascent = fm.descent - size
            } else if (size >= need) {
                /*
                 * We can't show all the descent, but we can at least
                 * show all the ascent.
                 */

                fm.ascent = -need
                fm.top = fm.ascent
                fm.descent = fm.top + size
                fm.bottom = fm.descent
            } else {
                /*
                 * Show as much of the ascent as we can, and no descent.
                 */

                fm.ascent = -size
                fm.top = fm.ascent
                fm.descent = 0
                fm.bottom = fm.descent
            }
        }
    }
}
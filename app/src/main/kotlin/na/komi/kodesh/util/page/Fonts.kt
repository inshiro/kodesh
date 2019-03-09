package na.komi.kodesh.util.page

import android.graphics.Typeface
import na.komi.kodesh.Application
import android.graphics.Paint
import android.text.TextPaint
import android.text.style.TypefaceSpan

object Fonts {
    private val fontAssets by lazy { Application.instance.assets }

    private const val Merriweather_Black_Path = "fonts/Merriweather-Black.otf"
    val Merriweather_Black: Typeface by lazy { Typeface.createFromAsset(fontAssets, Merriweather_Black_Path) }

    private const val GentiumPlus_R_Path = "fonts/GentiumPlus-R.otf"
    val GentiumPlus_R: Typeface by lazy { Typeface.createFromAsset(fontAssets, GentiumPlus_R_Path) }

    private const val GentiumPlus_I_Path = "fonts/GentiumPlus-I.otf"
    val GentiumPlus_I: Typeface by lazy { Typeface.createFromAsset(fontAssets, GentiumPlus_I_Path) }
    /*
    Typeface nunito = getResources().getFont(R.font.nunito);
        TextView text = (TextView)findViewById(R.id.nunito_programmatic);
        text.setTypeface(nunito, Typeface.BOLD_ITALIC);
     */
}


class CustomTypefaceSpan(private val newType: Typeface, family: String = "") : TypefaceSpan(family) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0

        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = tf
    }
}
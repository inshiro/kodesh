package na.komi.kodesh.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.NumberPicker
import android.annotation.TargetApi
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import na.komi.kodesh.R
import android.util.TypedValue
import na.komi.kodesh.util.log


/**
 * https://stackoverflow.com/a/34449748
 */
class NumberPicker2 : NumberPicker {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        /*

        val t = context.getPackageManager().getActivityInfo(ComponentName(context, MainActivity::class.java.name), 0).getThemeResource()
        val a = context.getTheme().obtainStyledAttributes( t, intArrayOf(R.attr.editTextColor) )

        // Get color hex code (eg, #fff)
        val intColor = a.getColor(0 /* index */, 0 /* defaultVal */)
        val hexColor = Integer.toHexString(intColor)
        a.recycle() */

        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.numberPickerDividerColor, typedValue, true)
        @ColorInt val color = typedValue.data
        setDividerColor(color)//ContextCompat.getColor(context, R.color.etc))
    }

    fun setDividerColor(@ColorInt color: Int) {
        try {
            val fDividerDrawable = NumberPicker::class.java.getDeclaredField("mSelectionDivider")
            fDividerDrawable.isAccessible = true
            val d = fDividerDrawable.get(this) as Drawable
            d.setColorFilter(color, PorterDuff.Mode.SRC)
            d.invalidateSelf()
            postInvalidate() // Drawable is dirty
        } catch (e: Exception) {
            log wtf "$e"
        }

    }
}
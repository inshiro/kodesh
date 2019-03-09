package na.komi.kodesh.util.page

import android.text.Spannable

class MySpannableFactory : Spannable.Factory(){
    override fun newSpannable(source: CharSequence?): Spannable {
        return source as? Spannable ?: super.newSpannable(source)
    }
}
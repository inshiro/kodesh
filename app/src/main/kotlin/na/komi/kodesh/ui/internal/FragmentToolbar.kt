package na.komi.kodesh.ui.internal

import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes

/**
 * https://is.gd/Brbjw8
 */
data class FragmentToolbar(
    @IdRes val toolbar: Int,
    @StringRes val title: Int = -1,
    @MenuRes val menu: Int = -1,
    @IdRes val bottomSheet: Int,
    @IdRes val navigationView: Int,
    @MenuRes val navigationViewMenu: Int = -1,
    @Suppress("ArrayInDataClass") val menuItems: IntArray? = null,
    @Suppress("ArrayInDataClass") val menuClicks: Array<MenuItem.OnMenuItemClickListener?>? = null
) {

    companion object {
        val NO_TOOLBAR = -1
    }
}

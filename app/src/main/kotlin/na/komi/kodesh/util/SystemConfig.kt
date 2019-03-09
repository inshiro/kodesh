package na.komi.kodesh.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue

/**
 * Created by kelin on 2017/5/31.
 */

class SystemConfig internal constructor(activity: Activity, private val mTranslucentStatusBar: Boolean) {
    /**
     * Get the height of the system status bar.
     *
     * @return The height of the status bar (in pixels).
     */
    val statusBarHeight: Int
    /**
     * Get the height of the action bar.
     *
     * @return The height of the action bar (in pixels).
     */
    val actionBarHeight: Int

    init {
        val res = activity.resources
        statusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME)
        actionBarHeight = getActionBarHeight(activity)
    }

    @TargetApi(14)
    private fun getActionBarHeight(context: Context): Int {
        var result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            val tv = TypedValue()
            context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
            result = context.resources.getDimensionPixelSize(tv.resourceId)
        }
        return result
    }

    private fun getInternalDimensionSize(res: Resources, key: String): Int {
        var result = 0
        val resourceId = res.getIdentifier(key, "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * Get the layout inset for any system UI that appears at the top of the screen.
     *
     * @param withActionBar True to include the height of the action bar, False otherwise.
     * @return The layout inset (in pixels).
     */
    fun getPixelInsetTop(withActionBar: Boolean): Int {
        return (if (mTranslucentStatusBar) statusBarHeight else 0) + if (withActionBar) actionBarHeight else 0
    }

    companion object {

        private val STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height"
        private val NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height"
        private val NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape"
        private val NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width"
        private val SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar"
    }

}
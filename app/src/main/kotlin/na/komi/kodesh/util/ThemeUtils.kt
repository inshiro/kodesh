package na.komi.kodesh.util

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import na.komi.kodesh.Application
import na.komi.kodesh.Prefs
import na.komi.kodesh.R

object ThemeUtils {
    private var sTheme: Int = 0
    val LIGHT = 0
    val DARK = 1
    val BLACK = 2
    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    fun changeToTheme(activity: AppCompatActivity, theme: Int) {
        Prefs.themeId = theme
        activity.recreate()
    }

    /** Set the theme of the activity, according to the configuration.  */
    fun onActivityCreateSetTheme(activity: Activity) {
        when (Prefs.themeId ) {
            DARK -> activity.setTheme(R.style.Theme_Dark)
            BLACK -> activity.setTheme(R.style.Theme_Black)
            else -> activity.setTheme(R.style.Theme_Light)
        }
    }

    val SYSTEM_UI_FLAG_LOW_PROFILE = View.SYSTEM_UI_FLAG_LOW_PROFILE
    val CLEAR_SYSTEM_UI_FLAG_LOW_PROFILE = View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
    val SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    val SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    val CLEAR_SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

    fun setBar(TYPE: Int, activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = activity.window.decorView //findViewById<CoordinatorLayout>(R.id.container_main)
            var flags = view.systemUiVisibility
            flags = flags or TYPE
            view.systemUiVisibility = flags
            //activity.window.statusBarColor = Color.WHITE
        }
    }

}

fun AppCompatActivity.setLowProfileStatusBar() = ThemeUtils.setBar(ThemeUtils.SYSTEM_UI_FLAG_LOW_PROFILE, this)
fun AppCompatActivity.clearLowProfileStatusBar() = ThemeUtils.setBar(ThemeUtils.CLEAR_SYSTEM_UI_FLAG_LOW_PROFILE, this)
fun AppCompatActivity.setLightNavBar() = ThemeUtils.setBar(ThemeUtils.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR, this)
fun AppCompatActivity.setLightStatusBar() = ThemeUtils.setBar(ThemeUtils.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, this)
fun AppCompatActivity.clearLightStatusBar() = ThemeUtils.setBar(ThemeUtils.CLEAR_SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, this)



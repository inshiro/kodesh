package na.komi.kodesh.ui.setting

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.google.android.material.navigation.NavigationView
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.BasePreferenceFragment
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.util.onClick

class SettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).let {
            it.findViewById<Toolbar>(R.id.toolbar_main).let { toolbar ->
                for (a in toolbar.menu.children)
                    if (a.itemId != R.id.ham_menu)
                        a.isVisible = false
                toolbar.title = getString(R.string.settings_title)
            }
            it.findViewById<NavigationView>(R.id.navigation_view).let { navigationView ->
                //navigationView.menu.findItem(R.id.action_search).isEnabled = false
                navigationView.menu.findItem(R.id.action_find_in_page).isEnabled = false
                navigationView.setCheckedItem(R.id.action_settings)
            }
            it.getToolbarTitleView()?.onClick {

            }
        }
        val mListPreference = preferenceManager.findPreference("THEME_ID") as ListPreference
        mListPreference.value = Prefs.themeId.toString()
        mListPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    when (newValue) {
                        "0" -> Prefs.themeId = 0
                        "1" -> Prefs.themeId = 1
                        "2" -> Prefs.themeId = 2
                    }
                    restartActivity()
                    true
                }
    }

    private fun restartActivity() {
        activity?.let {
            /*val intent = Intent(it, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            it.finish()*/
            it.recreate()
            it.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
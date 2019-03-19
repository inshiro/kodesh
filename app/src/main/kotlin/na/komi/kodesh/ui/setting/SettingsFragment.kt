package na.komi.kodesh.ui.setting

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.util.log
import na.komi.kodesh.util.onClick
import na.komi.kodesh.util.text.futureSet
import kotlin.coroutines.CoroutineContext

class SettingsFragment : PreferenceFragmentCompat(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job = SupervisorJob()

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        val bgColor = typedValue.resourceId
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), bgColor))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        log w "onActivityCreated Setings"
        (requireActivity() as MainActivity).let {
            it.getToolbar()?.let { toolbar ->
                toolbar.title = getString(R.string.settings_title)
                for (a in toolbar.menu.children)
                    a.isVisible = false
            }
            it.getToolbarTitleView()?.apply {
                futureSet(getString(R.string.settings_title))
                onClick {}
            }
            it.getNavigationView().setCheckedItem(R.id.action_settings)

        }
        val mListPreference = preferenceManager.findPreference<ListPreference>("THEME_ID") ?: return
        mListPreference.value = Prefs.themeId.toString()
        mListPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pref, newValue ->
                if (mListPreference.value != newValue) {
                    when (newValue) {
                        "0" -> Prefs.themeId = 0
                        "1" -> Prefs.themeId = 1
                        "2" -> Prefs.themeId = 2
                    }
                    restartActivity()
                    true
                } else
                    false
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
package na.komi.kodesh.ui.styling

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.BasePreferenceFragment
import na.komi.kodesh.ui.internal.ExtendedBottomSheetDialogFragment
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.ui.main.MainViewModel
import na.komi.kodesh.util.closestKatana
import na.komi.kodesh.util.setLowProfileStatusBar
import na.komi.kodesh.util.viewModel
import org.rewedigital.katana.Component
import org.rewedigital.katana.KatanaTrait

class StylingDialogFragment : ExtendedBottomSheetDialogFragment() {

    override val initialState: Int
        get() = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_container, container, false);
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (requireActivity() as MainActivity).setLowProfileStatusBar()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.content, PreferenceFragment())//.add(R.id.content, PreferenceFragment())
                .commit()
        }
    }

    /** https://is.gd/kdWs86 **/
    class PreferenceFragment : BasePreferenceFragment(), KatanaTrait {
        override val component: Component by closestKatana()

        private val viewModel:MainViewModel by viewModel()

        override fun onCreatePreferences(bundle: Bundle, rootKey: String) {
            addPreferencesFromResource(R.xml.styling_preferences)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            var state = true
            when ((preference as SwitchPreferenceCompat).key) {
                "KJVSTYLE_ID" -> {
                    Prefs.kjvStylingPref = preference.isChecked
                    viewModel.kjvStyling = preference.isChecked
                }
                "DROP_CAP_ID" -> {
                    Prefs.dropCapPref = preference.isChecked
                    viewModel.showDropCap = preference.isChecked
                }
                "RED_LETTER_ID" -> {
                    Prefs.redLetterPref = preference.isChecked
                    viewModel.showRedLetters = preference.isChecked
                }
                "VERSE_NUMBERS" -> {
                    Prefs.verseNumberPref = preference.isChecked
                    viewModel.showVerseNumbers = preference.isChecked
                }
                else -> {
                    state = super.onPreferenceTreeClick(preference)
                }
            }
            when (preference.key) {
                "KJVSTYLE_ID", "DROP_CAP_ID", "RED_LETTER_ID", "VERSE_NUMBERS" ->
                    viewModel.setAdapterUpdate(viewModel.adapterUpdate.value?.let { !it } ?: true)
            }
            return state
        }
    }
}
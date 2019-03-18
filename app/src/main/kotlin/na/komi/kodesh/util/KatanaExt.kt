package na.komi.kodesh.util

import androidx.fragment.app.Fragment
import na.komi.kodesh.ui.main.MainComponent
import org.rewedigital.katana.KatanaTrait

/**
 * Gets the component from the activity hosting the Fragment.
 */
fun <T> T.closestKatana() where T : KatanaTrait, T : Fragment = lazy { MainComponent.mainComponent }


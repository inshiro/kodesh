package na.komi.kodesh.ui.main

import android.os.Bundle
import androidx.preference.PreferenceManager
import na.komi.kodesh.R
import na.komi.kodesh.model.ApplicationDatabase
import na.komi.kodesh.ui.internal.BaseActivity
import org.rewedigital.katana.Component

class MainActivity : BaseActivity() {

    override val layout: Int = R.layout.activity_main

    val component: Component = MainComponent.mainComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: PreferenceManager.setDefaultValues(this, R.xml.styling_preferences, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFinishing) return
        ApplicationDatabase.destroyInstance()
        MainComponent.clear()
    }
}

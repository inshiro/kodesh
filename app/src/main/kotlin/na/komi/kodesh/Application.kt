package na.komi.kodesh

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.core.app.ActivityManagerCompat
import na.komi.kodesh.model.Preferences
import org.rewedigital.katana.Component
import org.rewedigital.katana.Katana
import org.rewedigital.katana.android.AndroidKatanaLogger
import org.rewedigital.katana.android.environment.AndroidEnvironmentContext
import org.rewedigital.katana.android.modules.APPLICATION_CONTEXT
import org.rewedigital.katana.android.modules.createApplicationModule
import org.rewedigital.katana.createComponent
import org.rewedigital.katana.createModule
import org.rewedigital.katana.dsl.compact.factory
import org.rewedigital.katana.dsl.get

val Prefs by lazy { na.komi.kodesh.Application.preferences }

/**
 * This module may provide Android specific classes like [Resources],
 * [android.content.SharedPreferences], system services etc.
 *
 * @see org.rewedigital.katana.android.modules.createApplicationModule
 */
val androidModule by lazy {
    createModule {
        factory { get<Context>(APPLICATION_CONTEXT).resources }
    }
}

class Application : Application() {
    companion object {
        lateinit var instance: Application
        lateinit var preferences: Preferences
        //lateinit var applicationComponent: Component
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = Preferences(this)

        setupKatana()
    }

    private fun setupKatana() {
        // Installing logger for Katana
        if (BuildConfig.DEBUG)
            Katana.logger = AndroidKatanaLogger

        // Specify Android environment for optimized usage on Android
        Katana.environmentContext = AndroidEnvironmentContext()
        //applicationComponent = createComponent(createApplicationModule(this),androidModule)
    }

}

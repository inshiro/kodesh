package na.komi.kodesh.ui.main

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import na.komi.kodesh.Application
import na.komi.kodesh.R
import na.komi.kodesh.model.ApplicationDatabase
import na.komi.kodesh.model.MainRepository
import na.komi.kodesh.ui.find.FindInPageFragment
import na.komi.kodesh.ui.internal.BaseActivity
import na.komi.kodesh.ui.internal.BottomSheetBehavior2
import na.komi.kodesh.ui.preface.PrefaceFragment
import na.komi.kodesh.util.Knavigator
import na.komi.kodesh.util.close
import na.komi.kodesh.util.viewModel
import org.rewedigital.katana.Component
import org.rewedigital.katana.Module
import org.rewedigital.katana.android.modules.ACTIVITY
import org.rewedigital.katana.android.modules.createActivityModule
import org.rewedigital.katana.createComponent
import org.rewedigital.katana.createModule
import org.rewedigital.katana.dsl.compact.factory
import org.rewedigital.katana.dsl.compact.singleton
import org.rewedigital.katana.dsl.get

/**
 * Modules do not need to be cached since MainComponents hold
 * all the instances.
 */
object Modules {
    private var _mainModule: Module? = null
    val mainModule: Module
        get() = _mainModule ?: createModule {
            singleton { ApplicationDatabase.getInstance(Application.instance) }
            singleton { get<ApplicationDatabase>().mainDao() }
            singleton { MainRepository.getInstance(get()) }
            viewModel { MainViewModel(get()) }
        }.also { _mainModule = it }

    fun destroyInstance() {
        _mainModule = null
    }
}


class MainActivity : BaseActivity() {

    val component by lazy { MainComponents.mainComponent }

    override val layout: Int = R.layout.activity_main

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (bottomSheetBehavior.state == BottomSheetBehavior2.STATE_EXPANDED) {
            val viewRect = Rect()
            bottomSheetContainer.getGlobalVisibleRect(viewRect)
            if (ev != null && !viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                bottomSheetBehavior.close()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    val mainFragment : MainFragment by MainComponents.fragComponent.inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.styling_preferences, false)
        knavigator.container = R.id.nav_main_container
        knavigator.show(mainFragment, isMainFragment = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && !isChangingConfigurations) {
            ApplicationDatabase.destroyInstance()
            MainComponents.destroyInstance()
        }
    }

}

package na.komi.kodesh.ui.main

import na.komi.kodesh.ui.about.AboutFragment
import na.komi.kodesh.ui.find.FindInPageFragment
import na.komi.kodesh.ui.preface.PrefaceFragment
import na.komi.kodesh.ui.search.SearchFragment
import na.komi.kodesh.ui.setting.SettingsFragment
import na.komi.kodesh.util.Knavigator
import org.rewedigital.katana.Component
import org.rewedigital.katana.createComponent
import org.rewedigital.katana.createModule
import org.rewedigital.katana.dsl.compact.singleton


/**
 * See [Component]
 * As long as the same Component reference is used for injection, the same
 * singleton instances are reused. Once the Component is eligible for garbage collection so are the instances hold by
 * this component. The developer is responsible for holding a Component reference and releasing it when necessary. This
 * design was chosen in contrast to other DI libraries that for instance work with a global, singleton state to prevent
 * accidental memory leaks.
 */
object MainComponents {
    private var _mainComponent: Component? = null
    val mainComponent
        get() = _mainComponent ?: createComponent(
            modules = listOf(Modules.mainModule)// + Modules.modules,
        ).also { _mainComponent = it }

    private var _navComponent: Component?=null
    val navComponent
        get()= _navComponent ?: createComponent(
            modules = listOf(
                createModule {
                    singleton { Knavigator() }
                })).also { _navComponent= it }

    private var _fragComponent: Component?=null
    val fragComponent
        get()=
            _fragComponent ?: createComponent(
                modules = listOf(
                    createModule {
                        singleton { MainFragment() }
                        singleton { PrefaceFragment() }
                        singleton { SearchFragment() }
                        singleton { FindInPageFragment() }
                        singleton { SettingsFragment() }
                        singleton { AboutFragment() }
                    })).also { _fragComponent= it }

    fun destroyInstance() {
        _mainComponent = null
        _fragComponent = null
        _navComponent = null
        Modules.destroyInstance()
    }
}
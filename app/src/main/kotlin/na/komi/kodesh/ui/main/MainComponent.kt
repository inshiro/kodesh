package na.komi.kodesh.ui.main

import org.rewedigital.katana.Component
import org.rewedigital.katana.createComponent


/**
 * See [Component]
 * As long as the same Component reference is used for injection, the same
 * singleton instances are reused. Once the Component is eligible for garbage collection so are the instances hold by
 * this component. The developer is responsible for holding a Component reference and releasing it when necessary. This
 * design was chosen in contrast to other DI libraries that for instance work with a global, singleton state to prevent
 * accidental memory leaks.
 */
object MainComponent {
    private var mmainComponent: Component? = null
    val mainComponent
        get() = mmainComponent ?: createComponent(
            modules = listOf(Modules.mainModule)// + Modules.modules,
        ).also { mmainComponent = it }

    fun clear() {
        mmainComponent = null
        Modules.clear()
    }
}
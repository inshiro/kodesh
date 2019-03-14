package na.komi.kodesh.ui.main

import na.komi.kodesh.Application
import na.komi.kodesh.model.ApplicationDatabase
import na.komi.kodesh.model.MainRepository
import na.komi.kodesh.util.viewModel
import org.rewedigital.katana.Module
import org.rewedigital.katana.createModule
import org.rewedigital.katana.dsl.compact.singleton
import org.rewedigital.katana.dsl.get

/**
 * Modules do not need to be cached since MainComponent hold
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

    fun clear() {
        _mainModule = null
    }
}
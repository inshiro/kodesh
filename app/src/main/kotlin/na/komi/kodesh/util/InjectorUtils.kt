package na.komi.kodesh.util

import android.content.Context
import na.komi.kodesh.model.ApplicationDatabase
import na.komi.kodesh.model.MainRepository
import na.komi.kodesh.ui.main.MainViewModelFactory

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {

    private fun getMainRepository(context: Context): MainRepository {
        return MainRepository.getInstance(ApplicationDatabase.getInstance(context).mainDao())
    }

    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        return MainViewModelFactory(getMainRepository(context))
    }

}

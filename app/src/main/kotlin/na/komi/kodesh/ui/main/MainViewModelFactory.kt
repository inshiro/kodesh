package na.komi.kodesh.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import na.komi.kodesh.model.MainRepository

class MainViewModelFactory(private val repository: MainRepository) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = MainViewModel(repository) as T
}

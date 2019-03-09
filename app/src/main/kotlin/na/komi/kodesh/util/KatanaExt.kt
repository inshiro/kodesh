package na.komi.kodesh.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import na.komi.kodesh.ui.main.Components
import na.komi.kodesh.ui.main.MainActivity
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.Module
import org.rewedigital.katana.dsl.ProviderDsl
import org.rewedigital.katana.dsl.compact.singleton
import org.rewedigital.katana.dsl.get
import org.rewedigital.katana.injectNow

/**
 * Need the VMFactory to give us a VM
 * Use ViewModelProviders.of to give us VM
 * This hooks is up to the LifeCycleOwner
 * keeping the ViewModel alive and paired with the scop.
 * We use VMFactory because we can pass constructors into
 * our ViewModel.
 * VMFactory ALWAYS returns a new VM.
 */

/**
 * Get Repo
 * Get VM
 * Get this VM from VMFactory (Allows the use of VM constructor). VMFactory is a {single}
 * Get the same VM scoped to the [scope] from ViewModelProviders.of
 * https://is.gd/68CHvb
 *
 */

/**
 * Gets the component from the activity hosting the Fragment.
 */
fun <T> T.closestKatana() where T : KatanaTrait, T : Fragment = lazy { (requireActivity() as MainActivity).component }//(requireActivity() as KatanaTrait).component }

/**
 * Gets repo/constructor specified in your VM, return VM.
 */

/**
 * Custom ViewModelFactory with Dependency Injection.
 *
 * Retrieve previously defined view model by tag (class name)
 * https://is.gd/68CHvb
 */
/**
 * A ViewModelFactory that works alongside dependency injection.
 *
 * @param viewModel The already injected ViewModel.
 * @return A ViewModelProvider.Factory to be used with ViewModelProviders.of
 */
class KatanaViewModelFactory(private val viewModel: ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModel as? T? ?: modelClass.newInstance()
}


/**
 * Declares a [ViewModel] dependency binding as a singleton.
 * Only one instance (per component) will be created.
 *
 * The ViewModelFactory is also created here because we need the unique class name of your [ViewModel].
 * @param body Body of binding declaration
 *
 * @see Module.factory
 * @see Module.singleton
 */
inline fun <reified T : ViewModel> Module.viewModel(crossinline body: ProviderDsl.() -> T) {
    val name : String = T::class.java.simpleName
    singleton<ViewModel>(name, body = body)
    singleton(name = "${name}Factory") { KatanaViewModelFactory(get(name)) }
}


/**
 * Inject the ViewModel declared from [Module.viewModel].
 *
 * The scope is tied to the host [Activity]. This is called from a [Fragment].
 * @return [ViewModel]
 */
inline fun <reified VM : ViewModel, T> T.viewModel(): Lazy<VM> where T : KatanaTrait, T : Fragment =
        lazy { ViewModelProviders.of(requireActivity(), injectNow("${VM::class.java.simpleName}Factory")).get(VM::class.java) }


/**
 * Inject the ViewModel declared from [Module.viewModel].
 * This is called from an [Activity].
 * @return [ViewModel]
 */
inline fun <reified VM : ViewModel, T> T.viewModel(): Lazy<VM> where T : KatanaTrait, T : AppCompatActivity =
        lazy { ViewModelProviders.of(this, injectNow("${VM::class.java.simpleName}Factory")).get(VM::class.java) }


/**
 * Inject the ViewModel declared from [Module.viewModel] with an assignment operator.
 *
 * The scope is tied to the host [Activity]. This is called from a [Fragment].
 * @return [ViewModel]
 */
// This has conflicts with the above. Use this if you're using [KatanaFragmentDelegate]
// and you're inside the onInject callback.
/* inline fun <reified VM : ViewModel,T> T.viewModel(): VM where T: KatanaTrait, T: Fragment=
   ViewModelProviders.of(requireActivity(),injectNow("${VM::class.java.simpleName}Factory")).get(VM::class.java)
*/

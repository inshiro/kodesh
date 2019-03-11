package na.komi.kodesh.util

import android.os.Handler
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


/**
 * for internal use.
 */
internal object Logger {
    private const val TAG = "KNAVIGATOR"

    infix fun d(msg: String) {
        Knavigator.logger?.d(msg)
    }

    infix fun i(msg: String) {
        Knavigator.logger?.i(msg)
    }

    infix fun w(msg: String) {
        Knavigator.logger?.w(msg)
    }

    infix fun e(msg: String) {
        Knavigator.logger?.e(msg)
    }

    infix fun v(msg: String) {
        Knavigator.logger?.v(msg)
    }

}

/**
 * implementation. for outside use.
 */
object KnavigatorLogger : Knavigator.Logger {

    private const val TAG = "KNAVIGATOR"

    override infix fun d(msg: String) {
        Log.d(TAG, msg)
    }

    override infix fun i(msg: String) {
        Log.i(TAG, msg)
    }

    override fun w(msg: String) {
        Log.w(TAG, msg)
    }

    override fun e(msg: String) {
        Log.e(TAG, msg)
    }
    override fun v(msg: String) {
        Log.v(TAG, msg)
    }
}

/**
 * An easy to use, simple yet powerful Fragment Navigator.
 */
class Knavigator {


    /**
     * Define your fragments so you only create singletons using Dependency Injections
     * Use the same fragment by calling findFragmentByTag
     */
    lateinit var fragmentManager: FragmentManager


    @IdRes
    var container: Int = -1
    private var fragInit = false
    private var mainFragment = ""
    private val animationStart = android.R.animator.fade_in
    private val animationEnd = android.R.animator.fade_out
    private val handler by lazy { Handler() }
    private val fragmentList by lazy { mutableListOf<String>() }
    private val VisibiltyTag by lazy {
        arrayOf(
            arrayOf("Adding", "Attaching", "Showing"),
            arrayOf("Removing", "Detaching", "Hiding")
        )
    }
    var defaultMode = SPARING_SINGLETON
    private val backCount
        get() = fragmentList.size

    private val Fragment.name
        get() = this::class.java.simpleName

    fun Fragment.addToBackStack() = fragmentList.add(name)
    fun Fragment.removeFromBackStack() = fragmentList.remove(name)
    fun Fragment.isInitialized() = this@Knavigator.fragmentManager.backStackEntryCount > 0 && this.isAdded

    fun getCurrentFragment() =
        if (fragmentStack.lastIndex < 0) null else fragmentManager.findFragmentByTag(fragmentStack[fragmentStack.lastIndex].name)

    fun navigate(fragment: Fragment, mode: Int = defaultMode, inBackStack: Boolean = true) {
        // If in backstack you can hide it
        if (fragmentStack.lastIndex >= 0 && fragmentStack[fragmentStack.lastIndex].inBackStack)
            hide(getCurrentFragment()!!)
        show(fragment, mode, inBackStack)

    }

    fun add(fragment: Fragment) {
        // Account for when we have no fragments. When the back stack is 0.
        //if (initialized(fragment)) return
        //if (!fragment.isAdded)
        fragmentManager.beginTransaction()
            .setCustomAnimations(animationStart, animationEnd)
            .add(container, fragment, fragment::class.java.simpleName)
            //.addToBackStack(TAG)
            .commitNowAllowingStateLoss()


    }


    private fun Fragment.addStack(enter: Int, exit: Int, inBackStack: Boolean) {
        fragmentStack.add(KnavigatorFragment(name, enter, exit, inBackStack))
    }

    private fun Fragment.removeStack() {
        for (idx in 0 until fragmentStack.size)
            if (fragmentStack[idx].name == name) {
                fragmentStack.removeAt(idx); return
            }
    }

    /**
     * Show a fragment.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param inBackStack register this fragment in the backstack
     */
    fun show(fragment: Fragment, mode: Int = defaultMode, inBackStack: Boolean = true) {
        val tempFragment = fragmentManager.findFragmentByTag(fragment.name)
        val frag = tempFragment ?: fragment
        if (!frag.isAdded){//!fragment.isAdded) { // !fragmentList.contains(fragment.name) fragmentStack.map { it.name == frag.name }.isEmpty() &&
            add(frag)
            frag.addStack(mode, mode, inBackStack)
            Logger i "${VisibiltyTag[0][0]} fragment ${frag::class.java}"
            displayFragments()
            return
        } else if (frag.isDetached) {
            fragmentManager.beginTransaction()
                .setCustomAnimations(animationStart, animationEnd)
                .attach(frag)
                .commitNowAllowingStateLoss()
            //frag.addStack(mode, mode, inBackStack)
            Logger i "${VisibiltyTag[0][1]} fragment ${frag::class.java}"
            displayFragments()
            return
        } else if (!frag.isVisible /* user hint visible*/) {
            fragmentManager.beginTransaction()
                .setCustomAnimations(animationStart, animationEnd)
                .show(frag)
                .commitNowAllowingStateLoss()
            //frag.addStack(mode, mode, inBackStack)
            Logger i "${VisibiltyTag[0][2]}  fragment ${frag::class.java}"
            displayFragments()
            return
        } else {
            Logger e "ERROR showing ${frag::class.java}"
            return
        }
    }

    /**
     * Hide a fragment.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param inBackStack register this fragment in the backstack
     */
    fun hide(fragment: Fragment) {
        val tempFragment = fragmentManager.findFragmentByTag(fragment.name)
        val frag = tempFragment ?: fragment
        if (!frag.isAdded) { // !fragmentList.contains(fragment.name)
            Logger w "Tried to hide but fragment ${frag::class.java} is NOT added"
            return
        }
        var visibiltyTag = ""
        val transaction = fragmentManager.beginTransaction()
            .setCustomAnimations(animationStart, animationEnd)
        when (getCurrentFragmentInStack().exit) {
            FACTORY -> {
                transaction.remove(frag); visibiltyTag = VisibiltyTag[1][0]
                frag.removeStack()
            }
            SPARING_SINGLETON -> {
                transaction.detach(frag); visibiltyTag = VisibiltyTag[1][1]
            }
            SINGLETON -> {
                transaction.show(frag); visibiltyTag = VisibiltyTag[1][2]
            }
        }
        transaction.commitNowAllowingStateLoss()
        Logger i "$visibiltyTag fragment ${frag::class.java}"
    }

    /**
     * PopBackStack reverts the last transaction.
     * Recalling the last fragment. If the last fragment is gone, it'll be recreated.
     * Thus we must manage our own backstack because this allows us to properly use
     * detach and attach.
     * This prevents re-adding fragments due to un-intended behavior.
     *
     * We avoid using popBackStack becuase it reverts the last transaction.
     * Transactions are pseudo code. And not concrete implementations such
     * as add, remove, detach, attach, hide, show.
     * https://stackoverflow.com/a/38305887
     */
/*
    fun show(fragment: Fragment, visibilty: Int = defaultMode, isMainFragment: Boolean = false) {
        val tempFragment = fragmentManager.findFragmentByTag(fragment.name)
        val fragment = tempFragment ?: fragment
        fragment.addToBackStack()
        if (isMainFragment) {
            mainFragment = fragment.name
            fragment.removeFromBackStack()
            // If home fragment, do not record in backstack
            // Though, we still have in our list of fragments in supportFragmentManager
        }
        if (!fragment.isInitialized() && !fragment.isVisible && tempFragment == null) {
            //fragInit = true
            add(fragment)
            return
        }

        val fragmentTransaction = fragmentManager.beginTransaction()
            .setCustomAnimations(animationStart, animationEnd)
        var visibiltyTag = ""
        when (visibilty) {
            SINGLETON -> {
                fragmentTransaction.show(fragment); visibiltyTag = VisibiltyTag[0][0]
            }
            SPARING_SINGLETON -> {
                fragmentTransaction.attach(fragment); visibiltyTag = VisibiltyTag[0][1]
            }
            FACTORY -> {
                fragmentTransaction.add(fragment, fragment.name); visibiltyTag = VisibiltyTag[0][2]
            }
        }
        fragmentTransaction.commitAllowingStateLoss()
        Logger d "$visibiltyTag fragment ${fragment.name}"
    }

    fun hide(fragment: Fragment, visibilty: Int = defaultMode) {
        // TODO SetonHideListener
        val tempFragment = fragmentManager.findFragmentByTag(fragment.name)
        val fragment = tempFragment ?: fragment
        if (!fragment.isVisible) return
        val fragmentTransaction = fragmentManager.beginTransaction()
            .setCustomAnimations(animationStart, animationEnd)
        var visibiltyTag = ""
        when (visibilty) {
            SINGLETON -> {
                fragmentTransaction.hide(fragment); visibiltyTag = VisibiltyTag[1][0]
            }
            SPARING_SINGLETON -> {
                fragmentTransaction.detach(fragment); visibiltyTag = VisibiltyTag[1][1]
            }
            FACTORY -> {
                fragmentTransaction.remove(fragment); visibiltyTag = VisibiltyTag[1][2]
            }
        }
        fragmentTransaction.commitAllowingStateLoss()
        Logger d "$visibiltyTag fragment ${fragment.name}"
        fragment.removeFromBackStack()
    }
*/
    fun canGoBack() =
        if (fragmentStack.lastIndex >= 0 && fragmentStack.size == 1 && !fragmentStack[fragmentStack.lastIndex].inBackStack) false else fragmentStack.size > 0

    fun goBack() {
        fragmentStack.joinToString(", ") { it.name }
            .also { Logger v "goBack: [$it]" }
        val fragment = getCurrentFragment()
        if (fragment != null)
            hide(fragment).also { Logger i "BackPressed. Last fragment was ${fragment.name}" }

        // Detach/remove all fragments
    }

    data class KnavigatorFragment(val name: String, val enter: Int, val exit: Int, val inBackStack: Boolean = false)

    private val fragmentStack = mutableListOf<KnavigatorFragment>()
    fun getCurrentFragmentInStack() = fragmentStack[fragmentStack.lastIndex]
    fun getFragments() = Logger i fragmentStack.toString()
    /**
     * Try not to call this. Use internal stack.
     */
    fun displayFragments() {
        handler.postDelayed({
            fragmentManager.fragments.joinToString(", ") { it::class.java.simpleName }
                .also { Logger v "Currently have: [$it]" }
        }, 100)
    }

    interface Logger {

        infix fun d(msg: String)
        infix fun i(msg: String)
        infix fun w(msg: String)
        infix fun e(msg: String)
        infix fun v(msg: String)

    }

    companion object {

        /**
         * Pass an implementation of [Logger] here to enable Katana's logging functionality
         */
        var logger: Logger? = null
        /**
         * Hide-Show = SINGLETON
         * Detach-Attach = SPARING SINGLETON
         * Add-Remove =  FACTORY
         */
        val SINGLETON: Int = 2
        val SPARING_SINGLETON: Int = 1
        val FACTORY: Int = 0
    }
}
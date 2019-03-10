package na.komi.kodesh.util

import android.os.Handler
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


object KnavigatorLogger : Knavigator.Logger {

    private const val TAG = "KNAVIGATOR"

    override infix fun d(msg: String) {
        Log.d(TAG, msg)
    }

    override infix fun i(msg: String) {
        Log.i(TAG, msg)
    }

    override fun debug(msg: String) {
        Log.d(TAG, msg)
    }

    override fun info(msg: String) {
        Log.i(TAG, msg)
    }

    override fun warn(msg: String) {
        Log.w(TAG, msg)
    }

    override fun error(msg: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(TAG, msg, throwable)
        } else {
            Log.e(TAG, msg)
        }
    }
}

class Knavigator() {

    interface Logger {

        infix fun d(msg: String)
        infix fun i(msg: String)

        fun debug(msg: String)

        fun info(msg: String)

        fun warn(msg: String)

        fun error(msg: String, throwable: Throwable? = null)

    }

    /**
     * Define your fragments so you only create singletons using Dependency Injections
     * Use the same fragment by calling findFragmentByTag
     */
    lateinit var fragmentManager: FragmentManager

    /**
     * Pass an implementation of [Logger] here to enable Katana's logging functionality
     */
    var logger: Logger = KnavigatorLogger


    @IdRes var container: Int = -1
    private var fragInit = false
    private var mainFragment = ""
    private val animationStart = android.R.animator.fade_in
    private val animationEnd = android.R.animator.fade_out
    private val handler by lazy { Handler() }
    private val fragmentList by lazy { mutableListOf<String>() }
    private val VisibiltyTag by lazy {
        arrayOf(
            arrayOf("Showing", "Attaching", "Adding"),
            arrayOf("Hiding", "Detaching", "Removing")
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

    fun add(fragment: Fragment) {
        // Account for when we have no fragments. When the back stack is 0.
        //if (initialized(fragment)) return
        fragmentManager.beginTransaction()
            .setCustomAnimations(animationStart, animationEnd)
            .add(container, fragment, fragment::class.java.simpleName)
            //.addToBackStack(TAG)
            .commitAllowingStateLoss().also { logger i "Adding fragment ${fragment::class.java}" }


    }

    fun show(fragment: Fragment, visibilty: Int = defaultMode, isMainFragment:Boolean = false) {
        val tempFragment = fragmentManager.findFragmentByTag(fragment.name)
        val fragment = tempFragment ?: fragment
        fragment.addToBackStack()
        if (isMainFragment) {
            mainFragment = fragment.name
            fragment.removeFromBackStack() // If home fragment do not record in backstack
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
        logger d "$visibiltyTag fragment ${fragment.name}"
        displayFragments()
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
    fun hide(fragment: Fragment, visibilty: Int = defaultMode) {
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
        logger d "$visibiltyTag fragment ${fragment.name}"
        fragment.removeFromBackStack()
    }

    fun canGoBack() = backCount != 0


    fun goBack() {
        displayFragments()
        val fragment = fragmentManager.findFragmentByTag(fragmentList[fragmentList.lastIndex])
        if (fragment != null)
            hide(fragment).also { logger i "OnBackPressed last fragment was ${fragment.name}" }

        // Detach/remove all fragments
    }

    fun displayFragments() {
        handler.postDelayed({
            fragmentManager.fragments.joinToString(", ") { it::class.java.simpleName }
                .also { logger i "Currently have: $it" }
        }, 100)
    }

    companion object {

        /**
         * Hide-Show = SINGLETON
         * Detach-Attach = SPARING SINGLETON
         * Add-Remove =  FACTORY
         */
        val SINGLETON: Int = 1
        val SPARING_SINGLETON: Int = 2
        val FACTORY: Int = 3
    }
}
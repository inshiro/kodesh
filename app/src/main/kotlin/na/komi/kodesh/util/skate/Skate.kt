package na.komi.kodesh.util.skate

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.parcel.Parcelize
import na.komi.kodesh.util.skate.global.SkateSingleton
import na.komi.kodesh.util.skate.log.Logger
import java.util.Stack

class Skate : Navigator {


    @IdRes
    override var container: Int = -1
    override var mode = FACTORY
    private val defaultMode = FACTORY
    private val handler by lazy { Handler() }
    private var listener: OnNavigateListener? = null
    override var fragmentManager: FragmentManager? = null
    var animationStart = android.R.animator.fade_in
    var animationEnd = android.R.animator.fade_out
    
    private val modes
        get() = SkateSingleton.modes

    override val current: Fragment?
        get() = internalFragmentManager.fragments.let { if (it.lastIndex - 1 >= 0) it[it.lastIndex - 1] else null }//currentlyVisibleFragment()

    val current2: Fragment?
        get() = internalFragmentManager.fragments.lastOrNull()

    private val internalFragmentManager: FragmentManager
        get() = fragmentManager ?: throw NullPointerException("Please set the fragment manager")

    private val Fragment.uid
        get() = this::class.java

    override val stack
        get() = SkateSingleton.stack
    
    @Parcelize
    data class SkateFragment(
        var tag: String,
        var mode: Int
    ) : Parcelable

    internal fun serializeList(list: ArrayList<SkateFragment>) {
        stack.clear()
        stack.addAll(list)
    }

    infix fun to(fragment: Fragment) = navigate(fragment)

    infix fun navigate(fragment: Fragment) {
        val list = internalFragmentManager.fragments
        list.reverse()

        currentTransaction = null
        ALLOW_COMMIT = false

        // Hide the current showing fragment
        current2?.let {
            hide(it)
        }

        ALLOW_COMMIT = true

        // Show the destination
        show(fragment)

    }

    infix fun show(fragment: Fragment) {
        checkAndCreateTransaction()
        @Suppress("NAME_SHADOWING")
        val fragment = internalFragmentManager.findFragmentByTag(fragment.uid.name) ?: fragment

        // Get the mode assigned to the fragment
        var mode = defaultMode
        modes[fragment.uid.name]?.let {
            mode = it
        } ?: modes.put(fragment.uid.name, mode)

        if (stack.firstOrNull { it.tag == fragment.uid.name } == null) {
            currentTransaction?.add(container, fragment, fragment.uid.name)
            stack.push(SkateFragment(fragment.uid.name, mode))
            Logger assert "Adding ${fragment.uid.name}"
        } else
            when (mode) {
                FACTORY -> {

                    if (stack.firstOrNull { it.tag == fragment.uid.name } == null)
                        currentTransaction?.add(
                            container,
                            fragment,
                            fragment.uid.name
                        ).also { Logger assert "Adding ${fragment.uid.simpleName}" }
                }
                SPARING -> currentTransaction?.attach(fragment).also { Logger assert "Attaching ${fragment.uid.simpleName}" }
                SINGLETON -> currentTransaction?.show(fragment).also { Logger assert "Showing ${fragment.uid.simpleName}" }
            }

        Logger info stack.toString()

        if (ALLOW_COMMIT) {
            commit()
            listener?.onShow()
        }
    }

    infix fun hide(fragment: Fragment) {
        checkAndCreateTransaction()
        @Suppress("NAME_SHADOWING")
        val fragment = internalFragmentManager.findFragmentByTag(fragment.uid.name) ?: fragment

        // Get the mode assigned to the fragment
        var mode = defaultMode
        modes[fragment.uid.name]?.let {
            mode = it
        } ?: modes.put(fragment.uid.name, mode)

        when (mode) {
            FACTORY -> {

                stack.firstOrNull { it.tag == fragment.uid.name }?.let {
                    Logger assert "Removing ${fragment.uid.simpleName}"
                    currentTransaction?.remove(fragment)
                    stack.remove(it)
                    SkateSingleton.modes.remove(it.tag)
                }
            }
            SPARING -> currentTransaction?.detach(fragment)?.also { Logger assert "Detaching ${fragment.uid.simpleName}" }
            SINGLETON -> currentTransaction?.hide(fragment)?.also { Logger assert "Hiding ${fragment.uid.simpleName}" }
        }

        //Logger warn stack.toString()

        if (ALLOW_COMMIT) {
            commit()
            listener?.onShow()
        }
    }

    private fun goBack(): Boolean {
        internalFragmentManager.fragments.let {
            it.lastOrNull()?.let { fragment ->
                hide(fragment)
                listener?.onBackPressed(false)
                if (it.size <= 1)
                    return false
            }
        }
        return true
    }
    
    /**
     * Runs a set of operations. Does not invoke callbacks.
     */
    fun operation(block: Skate.() -> Unit) = apply {
        currentTransaction = null
        ALLOW_COMMIT = false
        block()
        ALLOW_COMMIT = true
        commit()
    }

    val back: Boolean
        get() = goBack()



    private var currentTransaction: FragmentTransaction? = null

    private var ALLOW_COMMIT = true

    private fun commit() {
        currentTransaction?.commit()
        currentTransaction = null
    }

    private fun commitNow() {
        currentTransaction?.commitNow()
        currentTransaction = null
    }

    private fun checkAndCreateTransaction() {
        if (currentTransaction == null)
            currentTransaction =
                internalFragmentManager.beginTransaction().setCustomAnimations(animationStart, animationEnd)
    }

    
    private fun displayFragments() {
        handler.postDelayed({
            internalFragmentManager.fragments.joinToString(", ") { it::class.java.simpleName }
                .also { Logger verbose "Currently have: [$it]" }
        }, 100)
    }

    fun setOnNavigateListener(listener: OnNavigateListener) {
        this.listener = null
        this.listener = listener
    }

    internal fun clear() {

        fragmentManager = null
        listener = null
        SkateSingleton.clear()
    }

    companion object {
        operator fun invoke(): Skate {
            //log w "/SKATE Skate instance null?: $${SkateSingleton._instance == null}"

            if (SkateSingleton.readInstance() != null)
                throw RuntimeException("Use startSkating() method to get the single instance of this class.")

            return SkateSingleton.getInstance()
        }


        /**
         * Pass an implementation of [Logger] here to enable Katana's logging functionality
         */
        var logger: Logger? = null

        /**
         * The mode to Add-Remove.
         *
         * Saves the most memory.
         */
        val FACTORY: Int = 0


        /**
         * The mode to Detach-Attach.
         *
         * A balance between saving memory and speed.
         */
        val SPARING: Int = 1


        /**
         * The mode to Hide-Show.
         *
         * Uses the most memory but also the fastest.
         */
        val SINGLETON: Int = 2
    }

    interface ActivityLifecycleCallbacks {
        fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
        fun onActivityStarted(activity: Activity?) {}
        fun onActivityResumed(activity: Activity?) {}
        fun onActivityPaused(activity: Activity?) {}
        fun onActivityStopped(activity: Activity?) {}
        fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
        fun onActivityDestroyed(activity: Activity?) {}
    }

    interface OnNavigateListener {
        /**
         * Called after the [Fragment] is shown.
         */
        fun onShow() {}

        /**
         * Called after the [Fragment] is hidden.
         */
        fun onHide() {}

        /**
         * Called after the [Fragment] is hidden.
         */
        fun onBackPressed(isModular: Boolean) {}
    }

    interface Logger {

        fun debug(msg: String)
        fun info(msg: String)
        fun warn(msg: String)
        fun error(msg: String)
        fun verbose(msg: String)
        fun assert(msg: String)

    }

    enum class State {
        ADDED, ATTACHED, SHOWING, REMOVED, DETACHED, HIDDEN, ERROR
    }

}


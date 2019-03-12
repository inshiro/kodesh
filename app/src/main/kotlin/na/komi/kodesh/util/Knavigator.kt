package na.komi.kodesh.util

import android.os.Handler
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


/**
 * An easy to use, simple yet powerful Fragment Navigator.
 */
class Knavigator : Navigator {

    @IdRes
    override var container: Int = -1
    override var defaultMode = FACTORY
    private val animationStart = android.R.animator.fade_in
    private val animationEnd = android.R.animator.fade_out
    private val handler by lazy { Handler() }

    private var listener: OnNavigateListener? = null

    private var _fragmentManager: FragmentManager? = null

    private val fragmentManager: FragmentManager
        get() = _fragmentManager!!

    private val Fragment.name
        get() = this::class.java.simpleName

    override val current: Fragment?
        get() = fragmentManager.fragments.last()

    override val stack: MutableMap<String, KnavigatorFragment> by lazy {
        mutableMapOf<String, KnavigatorFragment>()
    }
    
    /**
     * [Fragment] wrapper to allow real state change listening.
     * @see State
     * @param inBackStack Whether this fragment added to back stack or not.
     * @param modular Hide this fragment as well on [navigate]
     */
    data class KnavigatorFragment(val state: State, val inBackStack: Boolean = true, val modular: Boolean = false)

    override infix fun setFragmentManager(fm: FragmentManager) {
        _fragmentManager = null
        _fragmentManager = fm
    }

    fun setOnHideListener(listener: OnNavigateListener) {
        this.listener = null
        this.listener = listener
        System.gc()
    }

    override infix fun navigate(fragment: Fragment) {
        /**
         * From this fragment to there
         * hide in between, including modular ones.
         * Show destination.
         */

        val list = fragmentManager.fragments
        list.reverse()

        // Get all modular fragments and hide them. Stop when we reach a non modular one.
        run loop@{
            list.forEach {
                if (stack[it.name] != null && stack[it.name]!!.modular) {
                    hide(it)
                } else {
                    return@loop
                }
            }
        }

        // Hide the current showing fragment
        goBack()

        // Show the destination
        show(fragment)
    }

    private fun Fragment.addStack(
        mode: Int = defaultMode,
        addToBackStack: Boolean = true,
        modular: Boolean = false
    ) {
        when (mode) {
            FACTORY -> stack[name] = KnavigatorFragment(State.ADDED, addToBackStack, modular)
            SPARING_SINGLETON -> stack[name] = KnavigatorFragment(State.ATTACHED, addToBackStack, modular)
            SINGLETON -> stack[name] = KnavigatorFragment(State.SHOWING, addToBackStack, modular)
        }
    }


    override infix fun add(fragment: Fragment) = fragment.addStack(0)

    override fun add(
        fragment: Fragment,
        mode: Int,
        addToBackStack: Boolean,
        modular: Boolean
    ) = fragment.addStack(mode,addToBackStack,modular)

    override fun add(index: Int, fragment: Fragment) {
    }
    
    override fun remove(fragment: Fragment) {
        hide(fragment, 0)
    }

    private fun parseState(frag: Fragment, mode: Int = defaultMode, show: Boolean = true): State {
        val tag = frag::class.java
        var state = State.REMOVED
        if (show) {
            if (!frag.isAdded) {
                when {
                    frag.isDetached -> {
                        Logger assert "Is it isDetached?"
                        when (mode) {
                            FACTORY -> state = State.ADDED.also { Logger debug "Add $tag" }
                            SPARING_SINGLETON -> state = State.ATTACHED.also { Logger debug "Attach $tag" }
                            SINGLETON -> state = State.SHOWING.also { Logger debug "Show $tag" }
                        }
                    }
                    frag.isHidden -> {
                        Logger assert "Is it hiding?"
                        when (mode) {
                            FACTORY -> state = State.ADDED.also { Logger debug "Add $tag" }
                            SPARING_SINGLETON -> state = State.ATTACHED.also { Logger debug "Attach $tag" }
                            SINGLETON -> state = State.SHOWING.also { Logger debug "Show $tag" }
                        }
                    }
                    else -> {
                        Logger assert "Is it removed?"
                        state = State.ADDED
                        Logger debug "Add $tag"
                    }
                }

            } else {
                Logger error "[Already Added] $tag"
                return State.ERROR
            }
            return state
        } else {
            if (frag.isAdded) {
                when {
                    !frag.isDetached -> {
                        Logger assert "Is it attached?"
                        when (mode) {
                            FACTORY -> state = State.REMOVED.also { Logger debug "Remove $tag" }
                            SPARING_SINGLETON -> state = State.DETACHED.also { Logger debug "Detach $tag" }
                            SINGLETON -> state = State.HIDDEN.also { Logger debug "Hide $tag" }
                        }

                    }
                    !frag.isHidden -> {
                        Logger assert "Is it showing?"
                        when (mode) {
                            FACTORY -> state = State.REMOVED.also { Logger debug "Remove $tag" }
                            SPARING_SINGLETON -> state = State.DETACHED.also { Logger debug "Detach $tag" }
                            SINGLETON -> state = State.HIDDEN.also { Logger debug "Hide $tag" }
                        }
                    }
                    else -> {
                        Logger assert "Is it added?"
                        state = State.REMOVED
                        Logger debug "Remove $tag"
                    }
                }

            } else {
                Logger error "[Not Added] $tag"
                return State.ERROR
            }
            return state
        }
    }

    override fun show(fragment: Fragment) {
        show(fragment, defaultMode)
    }

    override fun show(fragment: Fragment, mode: Int, addToBackStack: Boolean, modular: Boolean) {
        Logger verbose "== COMMENCE SHOW == "

        val frag = fragmentManager.findFragmentByTag(fragment.name) ?: fragment

        val state = parseState(frag, mode)

        if (state == State.ERROR) return

        val transaction = fragmentManager.beginTransaction().setCustomAnimations(animationStart, animationEnd)

        when (mode) {
            FACTORY -> stack[frag.name] = KnavigatorFragment(State.ADDED, addToBackStack, modular)
            SPARING_SINGLETON -> stack[frag.name] = KnavigatorFragment(State.ATTACHED, addToBackStack, modular)
            SINGLETON -> stack[frag.name] = KnavigatorFragment(State.SHOWING, addToBackStack, modular)
        }


        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (state) {
            State.ADDED -> transaction.add(container, frag, frag.name)
            State.ATTACHED -> transaction.attach(frag)
            State.SHOWING -> transaction.show(frag)
        }

        transaction.commitNow()

        listener?.onShow()
    }

    override fun hide(fragment: Fragment) {
        hide(fragment, defaultMode)
    }

    override fun hide(fragment: Fragment, mode: Int, addToBackStack: Boolean, modular: Boolean) {
        Logger verbose "== COMMENCE HIDE == "

        val frag = fragmentManager.findFragmentByTag(fragment.name) ?: fragment

        val state = parseState(frag, mode, false)

        if (state == State.ERROR) return

        val transaction = fragmentManager.beginTransaction().setCustomAnimations(animationStart, animationEnd)

        when (mode) {
            //FACTORY -> This gets removed. No need to record.
            SPARING_SINGLETON -> stack[frag.name] = KnavigatorFragment(State.DETACHED, addToBackStack, modular)
            SINGLETON -> stack[frag.name] = KnavigatorFragment(State.HIDDEN, addToBackStack, modular)
        }

        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (state) {
            State.REMOVED -> transaction.remove(frag).also { stack.remove(frag.name) }
            State.DETACHED -> transaction.detach(frag)
            State.HIDDEN -> transaction.hide(frag)
        }

        transaction.commitNow()

        listener?.onHide()

    }

    override fun goBack(): Boolean {
        // Hide the most recent Fragment added to the stack
        // Hide if visible, else ignore.
        var found = ""
        var modular = false
        // If it's in this list, it means it's visible.. cause ¯\_(ツ)_/¯
        val list = fragmentManager.fragments
        list.reverse()

        if (list.isNotEmpty()) {
            val fragment = list.first()
            val name = fragment.name
            if (stack[name] != null && stack[name]!!.inBackStack) {
                modular = stack[name]!!.modular
                found = name
                hide(fragment)
            }
        }

        listener?.onBackPressed(modular)

        // Handle back if we have any fragments in our backstack. If not call super.onBackPressed.
        return found.isNotEmpty()
    }

    @Suppress("unused")
    private fun displayFragments() {
        handler.postDelayed({
            fragmentManager.fragments.joinToString(", ") { it::class.java.simpleName }
                .also { Logger verbose "Currently have: [$it]" }
        }, 100)
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

        infix fun d(msg: String)
        infix fun i(msg: String)
        infix fun w(msg: String)
        infix fun e(msg: String)
        infix fun v(msg: String)
        infix fun a(msg: String)

    }
    
    enum class State {
        ADDED, ATTACHED, SHOWING, REMOVED, DETACHED, HIDDEN, ERROR
    }

    companion object {
        /**
         * Pass an implementation of [Logger] here to enable Katana's logging functionality
         */
        var logger: Logger? = null

        /**
         * The mode to Add-Remove.
         *
         * You should use this in conjunction with [setRetainInstance] set to `true` in your [Fragment]
         * as this saves the most memory.
         */
        val FACTORY: Int = 0


        /**
         * The mode to Detach-Attach.
         */
        val SPARING_SINGLETON: Int = 1


        /**
         * The mode to Hide-Show.
         */
        val SINGLETON: Int = 2
    }
}

/**
 * for internal use.
 */
internal object Logger {
    private const val TAG = "KNAVIGATOR"

    infix fun debug(msg: String) {
        Knavigator.logger?.d(msg)
    }

    infix fun info(msg: String) {
        Knavigator.logger?.i(msg)
    }

    infix fun warn(msg: String) {
        Knavigator.logger?.w(msg)
    }

    infix fun assert(msg: String) {
        Knavigator.logger?.a(msg)
    }

    infix fun error(msg: String) {
        Knavigator.logger?.e(msg)
    }

    infix fun verbose(msg: String) {
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

    override fun a(msg: String) {
        Log.println(Log.ASSERT, TAG, msg)
    }
}

interface Navigator {

    /**
     * add-remove (0), attach-detach (1), hide-show (2)
     */
    val defaultMode: Int

    /**
     * Get the current list of fragments added.
     */
    val stack: MutableMap<String, Knavigator.KnavigatorFragment>

    /**
     * Specify the container to house the [Fragment]
     */
    val container:Int

    /**
     * Get the current visible [Fragment].
     */
    val current:Fragment?

    /**
     * Set the [FragmentManager] scoped to the [Activity]. This changes every on configuration change.
     * Be sure to re-set it when that occurs.
     */
     fun setFragmentManager(fm: FragmentManager)

    /**
     * Add a [Fragment] to the stack.
     *
     * This is useful if you don't want to show the [Fragment] immediately.
     *
     * Since it would be put in the stack, it would still interact with [navigate].
     * @see [navigate]
     * @param fragment Target [Fragment] to add.
     */
     fun add(fragment: Fragment): Any?


    /**
     * Add a [Fragment] to the stack.
     *
     * This is useful if you don't want to show the [Fragment] immediately.
     *
     * Since it would be put in the stack, it would still interact with [navigate].
     * @see [navigate]
     * @param fragment Target [Fragment] to add.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param addToBackStack Register this [fragment] to be able to back press. Default to `true`.
     * @param modular Inclusive on navigation. (Will be hidden on [navigate])
     */
    fun add(fragment: Fragment, mode: Int = defaultMode, addToBackStack: Boolean = true, modular: Boolean = false)


    /**
     * Add a [Fragment] to the stack at the given `index`.
     *
     * This is useful if you don't want to show the [Fragment] immediately.
     *
     * Since it would be put in the stack, it would still interact with [navigate].
     * @see [add]
     * @param index Index position to insert [fragment] in.
     * @param fragment Target [Fragment] to add.
     */
    fun add(index: Int, fragment: Fragment)


    /**
     * Remove a [Fragment] from the stack.
     *
     * This essentially calls [hide] with a `mode` of [Knavigator.FACTORY].
     * @see [hide]
     * @param fragment Target [Fragment] to remove.
     */
     fun remove(fragment: Fragment)


    /**
     * Navigate to a destination, hiding any fragments in between, including ones marked with `modular`.
     * This is equivalent to [hide] then show [show].
     *
     * Preferably used with a NavigationView.
     *
     * @see show
     * @see hide
     * @param fragment [Fragment] to navigate to.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param inBackStack Register this fragment in the backstack. Default to true.
     *
     */
     fun navigate(fragment: Fragment)


    /**
     * Show a fragment.
     * @param fragment Target [Fragment] to add.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param addToBackStack Register this [fragment] to be able to back press. Default to `true`.
     * @param modular Inclusive on navigation. (Will be hidden on [navigate])
     */
     fun show(fragment: Fragment)


    /**
     * Show a fragment.
     * @param fragment Target [Fragment] to add.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param addToBackStack Register this [fragment] to be able to back press. Default to `true`.
     * @param modular Inclusive on navigation. (Will be hidden on [navigate])
     */
    fun show(fragment: Fragment, mode: Int = defaultMode, addToBackStack: Boolean = true, modular: Boolean = false)


    /**
     * Hide a fragment.
     * @param fragment Target [Fragment] to add.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param addToBackStack Register this [fragment] to be able to back press. Default to `true`.
     * @param modular Inclusive on navigation. (Will be hidden on [navigate])
     */
     fun hide(fragment: Fragment)


    /**
     * Hide a fragment.
     * @param fragment Target [Fragment] to add.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param addToBackStack Register this [fragment] to be able to back press. Default to `true`.
     * @param modular Inclusive on navigation. (Will be hidden on [navigate])
     */
    fun hide(fragment: Fragment, mode: Int = defaultMode, addToBackStack: Boolean = true, modular: Boolean = false)


    /**
     * Goes back sequentially. Skips any fragments not registered in backstack.
     *
     * Hide the most recent Fragment added to the stack.
     *
     * Handle back if we have any fragments in our backstack. If not, let system handle it.
     */
    fun goBack(): Boolean

    /**
     * PopBackStack reverts the last transaction.
     * Recalling the last fragment. If the last fragment is gone, it'll be recreated.
     * Thus we must manage our own backstack because this allows us to properly use
     * detach and attach.
     * This prevents re-adding fragments due to un-intended behavior.
     *
     * We avoid using popBackStack because it reverts the last transaction.
     * Transactions do not deal with concrete implementations such
     * as add, remove, detach, attach, hide, show.
     * https://stackoverflow.com/a/38305887
     */
}
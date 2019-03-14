package na.komi.kodesh.util.skate

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.Stack

interface Navigator {

    /**
     * add-remove (0), attach-detach (1), hide-show (2)
     */
    val defaultMode: Int

    /**
     * Get the current list of fragments added.
     */
    //val stack: SerialStack<Skate.SkateFragment>

    /**
     * Specify the container to house the [Fragment]
     */
    val container:Int

    /**
     * Get the current visible [Fragment].
     */
    val current: Fragment?

    /**
     * Set the [FragmentManager] scoped to the [Activity]. This changes every on configuration change.
     * Be sure to re-set it when that occurs.
     */
    val fragmentManager: FragmentManager?

    /**
     * Add a [Fragment] to the stack.
     *
     * This is useful if you don't want to show the [Fragment] immediately.
     *
     * Since it would be put in the stack, it would still interact with [navigate].
     * @see [navigate]
     * @param fragment Target [Fragment] to add.
     */
    infix fun add(fragment: Fragment): Any?


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
     * This essentially calls [hide] with a `mode` of [Skate.FACTORY].
     * @see [hide]
     * @param fragment Target [Fragment] to remove.
     */
    infix fun remove(fragment: Fragment)


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
    infix fun navigate(fragment: Fragment)


    /**
     * Show a fragment.
     * @param fragment Target [Fragment] to add.
     * @param mode add-remove (0), attach-detach (1), hide-show (2)
     * @param addToBackStack Register this [fragment] to be able to back press. Default to `true`.
     * @param modular Inclusive on navigation. (Will be hidden on [navigate])
     */
    infix fun show(fragment: Fragment)


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
    infix fun hide(fragment: Fragment)


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
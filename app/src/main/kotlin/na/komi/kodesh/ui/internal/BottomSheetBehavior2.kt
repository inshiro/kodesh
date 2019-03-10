package na.komi.kodesh.ui.internal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import na.komi.kodesh.util.log
import kotlin.math.max
import kotlin.math.min


/**
 * Descendant of BottomSheetBehavior that adds a few features and conveniences
 * from AppBarLayout.ScrollingViewBehavior
 *
 */
class BottomSheetBehavior2<V : View>(context: Context, attrs: AttributeSet) :
    BottomSheetBehavior<V>(context, attrs) {

    private var height = 0f
    private var baseTranslationY = 0f
    private var currentState = STATE_SCROLLED_UP
    private var currentAnimator: ViewPropertyAnimator? = null
    var snapEnabled: Boolean = true
    var scrollForViewBelow: Boolean = true
    private var onStopScroll: Boolean = false
    private var prevScrollY: Int = 0

    @ViewCompat.NestedScrollType
    private var lastStartedType: Int = 0


    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        /**
         * Gets the child immediately below the container that has this behaviour.
         * This is the only difference from HideBehavior.kt because a BottomSheet usually
         * has more than one view. In our case, the first view is a toolbar so we want to
         * apply this HideBehavior to that.
         */
        val c = if(scrollForViewBelow) (child as ViewGroup)[0] else child
        val paramsCompat = c.layoutParams as ViewGroup.MarginLayoutParams
        height = (c.measuredHeight + paramsCompat.bottomMargin).toFloat()
        baseTranslationY = c.translationY
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        lastStartedType = type
        onStopScroll = false
        if (type == ViewCompat.TYPE_TOUCH) {
            prevScrollY = getScrollY(target)
        }

        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, type: Int) {
        if (!snapEnabled) return
        // add snap behaviour
        // Logic here borrowed from AppBarLayout onStopNestedScroll code
        onStopScroll = true
        //if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
        // find nearest seam
        val currTranslation = child.translationY
        val childHalfHeight = height * 0.5
        // translate down
        if (currTranslation >= childHalfHeight) {
            slideDown(child)// animateBarVisibility(child, isVisible = false)
        }
        // translate up
        else {
            slideUp(child)//animateBarVisibility(child, isVisible = true)
        }
        //}
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
    }

    fun getScrollY(target: View) :Int{
        return if (target is RecyclerView)
            target.computeVerticalScrollOffset()
        else target.scrollY
    }
    
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        //log d "onNestedPreScroll targetscrollY: ${getScrollY(target)} dy: $dy"

        /**
         * start from 0
         * on scroll up -> translate down [maximum is height]
         * on scroll down -> translate up [max upTo 0]
         */

        if (!onStopScroll || !snapEnabled) {
            if ((getScrollY(target) - prevScrollY) > 0) {
                if (child.translationY in 0f..height) {
                    currentState = STATE_SCROLLED_UP
                    currentAnimator =
                        child.animate()
                            .translationY(
                                min(
                                    height,
                                    child.translationY + dy.toFloat()
                                ).let { if (it < 0f) 0f else if (it > height) height else it })
                            .setDuration(0)
                    //child.translationY = min(height.toFloat(), child.translationY + dy.toFloat()).toFloat()
                }
            } else if ((getScrollY(target) - prevScrollY) < 0) {
                if (child.translationY in 0f..height) {
                    currentState = STATE_SCROLLED_UP
                    currentAnimator =
                        child.animate()
                            .translationY(
                                max(
                                    0f,
                                    child.translationY + dy
                                ).let { if (it < 0f) 0f else if (it > height) height else it })
                            .setDuration(0)
                    //child.translationY = max(0f, child.translationY + dy)
                }
            }
            //log d "y: ${child.translationY}"
        }

        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateSnackbar(child, dependency)
        } else if (dependency is ConstraintLayout)
            updateSearchBar(child,dependency)
        return super.layoutDependsOn(parent, child, dependency)
    }

    private fun updateSearchBar(child: View, searchLayout: ConstraintLayout) {
        if (searchLayout.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = searchLayout.layoutParams as CoordinatorLayout.LayoutParams

            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            searchLayout.layoutParams = params
        }
    }
    private fun updateSnackbar(child: View, snackbarLayout: Snackbar.SnackbarLayout) {
        if (snackbarLayout.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = snackbarLayout.layoutParams as CoordinatorLayout.LayoutParams

            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            snackbarLayout.layoutParams = params
        }
    }

    /**
     * Perform an animation that will slide the child from it's current position to be totally on the
     * screen.
     */
    fun slideUp(child: V) {
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_UP
        animateChildTo(
            child, 0f, ENTER_ANIMATION_DURATION.toLong(), AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
        )
    }

    /**
     * Perform an animation that will slide the child from it's current position to be totally off the
     * screen.
     */
    fun slideDown(child: V) {
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_DOWN
        animateChildTo(
            child, height, EXIT_ANIMATION_DURATION.toLong(), AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
        )
    }

    private fun animateChildTo(child: V, targetY: Float, duration: Long, interpolator: TimeInterpolator) {
        currentAnimator = child
            .animate()
            .translationY(targetY)
            .setInterpolator(interpolator)
            .setDuration(duration)
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }
                })
    }

    private var callback: BottomSheetCallback? = null

    /**
     * Sets a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    fun setBottomSheetCallback(callback: BottomSheetCallback) {
        this.callback = callback
    }

    /** Callback for monitoring events about bottom sheets.  */
    abstract class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState The new state. This will be one of [.STATE_DRAGGING], [     ][.STATE_SETTLING], [.STATE_EXPANDED], [.STATE_COLLAPSED], [     ][.STATE_HIDDEN], or [.STATE_HALF_EXPANDED].
         */
        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset increases
         * as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
         * expanded states and from -1 to 0 it is between hidden and collapsed states.
         */
        abstract fun onSlide(bottomSheet: View, slideOffset: Float)
    }

    companion object {

        /** The bottom sheet is dragging.  */
        val STATE_DRAGGING = 1

        /** The bottom sheet is settling.  */
        val STATE_SETTLING = 2

        /** The bottom sheet is expanded.  */
        val STATE_EXPANDED = 3

        /** The bottom sheet is collapsed.  */
        val STATE_COLLAPSED = 4

        /** The bottom sheet is hidden.  */
        val STATE_HIDDEN = 5

        /** The bottom sheet is half-expanded (used when mFitToContents is false).  */
        val STATE_HALF_EXPANDED = 6

        /**
         * Peek at the 16:9 ratio keyline of its parent.
         *
         *
         * This can be used as a parameter for [.setPeekHeight]. [.getPeekHeight]
         * will return this when the value is set.
         */
        val PEEK_HEIGHT_AUTO = -1

        private val HIDE_THRESHOLD = 0.5f

        private val HIDE_FRICTION = 0.1f

        private val CORNER_ANIMATION_DURATION = 500

        private val DEF_STYLE_RES = R.style.Widget_Design_BottomSheet_Modal

        /**
         * A utility function to get the [BottomSheetBehavior] associated with the `view`.
         *
         * @param view The [View] with [BottomSheetBehavior].
         * @return The [BottomSheetBehavior] associated with the `view`.
         */
        fun <V : View> from(view: V): BottomSheetBehavior2<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            val behavior = params.behavior as? BottomSheetBehavior2<*>
                ?: throw IllegalArgumentException("The view is not associated with BottomSheetBehavior")
            @Suppress("UNCHECKED_CAST")
            return behavior as BottomSheetBehavior2<V>
        }

        protected val ENTER_ANIMATION_DURATION = 225
        protected val EXIT_ANIMATION_DURATION = 175

        private val STATE_SCROLLED_DOWN = 1
        private val STATE_SCROLLED_UP = 2
    }
}
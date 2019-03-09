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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
import androidx.core.view.ViewCompat
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min


/**
 * The [Behavior] for a View within a [CoordinatorLayout] to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 *
 * From: com.google.android.material.behavior.HideBottomViewOnScrollBehavior
 * and: https://stackoverflow.com/a/44778453
 * help from article: https://is.gd/hbPFIo
 */
class HideBehavior<V : View>(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<V>(context, attrs) {

    private var height = 0f
    private var baseTranslationY = 0f
    private var currentState = STATE_SCROLLED_UP
    private var currentAnimator: ViewPropertyAnimator? = null
    private var snapEnabled: Boolean = true
    private var onStopScroll: Boolean = false
    private var prevScrollY: Int = 0

    @ViewCompat.NestedScrollType
    private var lastStartedType: Int = 0

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        val paramsCompat = child.layoutParams as ViewGroup.MarginLayoutParams
        height = (child.measuredHeight + paramsCompat.bottomMargin).toFloat()
        baseTranslationY = child.translationY
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
            prevScrollY = target.scrollY
        }
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
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

        /**
         * start from 0
         * on scroll up -> translate down [maximum is height]
         * on scroll down -> translate up [max upTo 0]
         */

        if (!onStopScroll || !snapEnabled) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            if ((target.scrollY - prevScrollY) > 0) {
                if (child.translationY in 0f..height) {
                    currentState = STATE_SCROLLED_UP
                    currentAnimator =
                        child.animate()
                            .translationY(min(height, child.translationY + dy.toFloat()).let { if (it < 0f) 0f else if (it > height) height else it })
                            .setDuration(0)
                    //child.translationY = min(height.toFloat(), child.translationY + dy.toFloat()).toFloat()
                }
            } else if ((target.scrollY - prevScrollY) < 0) {
                if (child.translationY in 0f..height) {
                    currentState = STATE_SCROLLED_UP
                    currentAnimator =
                        child.animate()
                            .translationY(max(0f, child.translationY + dy).let { if (it < 0f) 0f else if (it > height) height else it })
                            .setDuration(0)
                    //child.translationY = max(0f, child.translationY + dy)
                }
            }
            //log d "y: ${child.translationY}"
        }


    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateSnackbar(child, dependency)
        }
        return super.layoutDependsOn(parent, child, dependency)
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

    companion object {

        protected val ENTER_ANIMATION_DURATION = 225
        protected val EXIT_ANIMATION_DURATION = 175

        private val STATE_SCROLLED_DOWN = 1
        private val STATE_SCROLLED_UP = 2
    }
}

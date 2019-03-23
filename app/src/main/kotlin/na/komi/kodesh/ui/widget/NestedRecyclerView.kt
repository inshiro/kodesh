package na.komi.kodesh.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView

class NestedRecyclerView : RecyclerView, NestedScrollingChild3, GestureDetector.OnGestureListener {
    constructor(context: Context) : super(context){ initialize() }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ initialize() }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initialize() }

    init {
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }

    private fun initialize() {
        mDetector = GestureDetectorCompat(context, this)
    }
    private var mNestedScrollingChildHelper : NestedScrollingChildHelper
   private lateinit var mDetector : GestureDetectorCompat

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        @Suppress("UNNECESSARY_SAFE_CALL")
        mNestedScrollingChildHelper?.isNestedScrollingEnabled = true
    }

    override fun isNestedScrollingEnabled(): Boolean {

        return mNestedScrollingChildHelper.isNestedScrollingEnabled;
    }

    override fun startNestedScroll(axes: Int): Boolean {

        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow,type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {

        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow,type);
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {

        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mNestedScrollingChildHelper.onDetachedFromWindow();
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        super.dispatchTouchEvent(ev)

        val handled = mDetector.onTouchEvent(ev);
        if (!handled && ev?.action == MotionEvent.ACTION_UP)
            stopNestedScroll()

        return true
    }

        override fun onShowPress(e: MotionEvent?) {
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            dispatchNestedPreScroll(distanceX.toInt(), distanceY.toInt(), null, null);
            dispatchNestedScroll(distanceX.toInt(),  distanceY.toInt(), 0, 0, null);
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
        }


}
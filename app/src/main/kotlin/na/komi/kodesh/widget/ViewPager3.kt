package na.komi.kodesh.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.annotation.Px
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import na.komi.kodesh.Prefs
import na.komi.kodesh.ui.internal.LinearLayoutManager2
import na.komi.kodesh.ui.main.MainChildAdapter
import kotlin.math.abs

class ViewPager3 : RecyclerView {

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    init {
        layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        PagerSnapHelperVerbose().also { SnapHelper = it }.attachToRecyclerView(this)
    }

    private lateinit var SnapHelper: PagerSnapHelperVerbose

    interface OnPageChangeCallback {
        fun onPageScrolled(pagesState: List<VisiblePageState>) {}

        fun onPageSelected(position: Int) {}

        fun onPageScrollStateChanged(state: Int) {}
    }

    fun registerOnPageChangeCallback(callback: OnPageChangeCallback) {
        mCallback = callback
    }

    @Suppress("unused")
    var orientation: Int
        get() = lm.orientation
        set(value) {
            lm.orientation = value
        }

    val lm
        get() = (layoutManager as LinearLayoutManager)
    private var mCallback: OnPageChangeCallback? = null
    private var downX = 0f
    private var downY = 0f
    private var intercept = false
    private var hasNestedRecyclerView = true
    private val X_SWIPE_BUFFER_AREA = 15
    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        /**
         * true -> touch to parent -> onTouchEvent() method of the parent.
         * false -> touch to child
         */
        when (e?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.x
                downY = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                val diffX = e.x - downX
                val diffY = e.y - downY
                intercept = abs(diffY) < abs(diffX) - X_SWIPE_BUFFER_AREA
            }
        }
        return super.onInterceptTouchEvent(e) && intercept
    }

    data class VisiblePageState(
            var index: Int,
            var view: View,
            @Px var viewCenterX: Int,
            @Px var distanceToSettledPixels: Int,
            var distanceToSettled: Float
    )

    private val maxPages: Int = 3
    var pageStates: MutableList<VisiblePageState> = ArrayList(maxPages)
    var pageStatesPool = List(maxPages) {
        VisiblePageState(0, this@ViewPager3, 0, 0, 0f)
    }

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private var defaultSize: Float = 25f
    private var zoomLimit = 5.0f
    private var zoomEnabled = true
    private fun initialize() {
        defaultSize = 5f
        if (hasNestedRecyclerView)
            mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    }


    /***
     *
     * @param zoomLimit
     *
     * Default value is 3, 3 means text can zoom 3 times the default size
     */
    fun setZoomLimit(zoomLimit: Float) {
        this.zoomLimit = zoomLimit
    }

    /*Scale Gesture listener class,

    mScaleFactor is getting the scaling value

    and mScaleFactor is mapped between 1.0 and and zoomLimit

    that is 4.0 by default. You can also change it. 4.0 means text

    can zoom to 4 times the default value.*/


    var firstVisibleChildView: Int? = -1
    var childRV: RecyclerView? = null
    var firstVisibleItem: Int? = -1
    var lastVisibleItem: Int? = -1
    var fontSize = -1f
    // var childViewHolder: MainChildAdapter.ViewHolder? = null

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        val childLM
            get() = (childRV?.layoutManager as? LinearLayoutManager2)
        private var prevScaleFactor = 0f
        private var threshHold = false

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            mScaleFactor *= detector.scaleFactor

            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, defaultSize + zoomLimit))

            // Do not scale on minuscule touches
            if (Math.abs(prevScaleFactor - mScaleFactor) in 0f..0.25f && !threshHold) {
                threshHold = false
                return false
            } else {
                // Reset the scale factor to initial touch so we keep the scale smooth
                if (!threshHold) mScaleFactor = prevScaleFactor

                // Set threshHold to true so we skip the
                // threshold check and scale normally
                threshHold = true
            }

            //(getChildAt(0) as AppCompatTextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, defaultSize * mScaleFactor)


            if (firstVisibleChildView == -1)
                firstVisibleChildView = lm.findFirstVisibleItemPosition()

            if (childRV == null && firstVisibleChildView != null)
                childRV = lm.findViewByPosition(firstVisibleChildView!!) as? RecyclerView

            if (firstVisibleItem == -1)
                firstVisibleItem = childLM?.findFirstVisibleItemPosition()

            if (lastVisibleItem == -1)
                lastVisibleItem = childLM?.findLastVisibleItemPosition()


            fontSize = defaultSize * mScaleFactor

            if (firstVisibleItem != null && lastVisibleItem != null)
                for (i in firstVisibleItem!!..lastVisibleItem!!) {

                    // Change text size on pinch
                    (childLM?.findViewByPosition(i) as? LayoutedTextView)?.let {
                        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
                    }
                }

            //(adapter as? MainChildAdapter)?.setFontSize(defaultSize * mScaleFactor, firstVisibleItem,lastVisibleItem)


            //listener.onScale()
            //Log.e("kodesh", mScaleFactor.toString())

            return true

        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            //log e "onScaleBegin"

            //log e "previous: $mScaleFactor current: ${Prefs.scaleFactor}"
            mScaleFactor = Prefs.scaleFactor
            prevScaleFactor = mScaleFactor
            threshHold = false

            firstVisibleChildView = lm.findFirstVisibleItemPosition().also { if (it>=0) childRV = lm.findViewByPosition(it) as? RecyclerView}

            firstVisibleItem = childLM?.findFirstVisibleItemPosition()
            lastVisibleItem = childLM?.findLastVisibleItemPosition()

            return super.onScaleBegin(detector)

        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
            //log e "mScaleFactor $mScaleFactor fontSize: $fontSize"
            Prefs.scaleFactor = mScaleFactor

            Prefs.mainFontSize = fontSize

            childRV?.adapter?.let {

                // Dont update if we are seeing the drop cap. Because we are updating it by setTExtSize
                // This prevents the relayout adjustment after calling notifydatasetcahnged
                if (firstVisibleItem != 0) {
                    it.notifyItemRangeChanged(lastVisibleItem!!, childLM!!.itemCount)

                    childLM?.setScrollBarHeight(0)
                    childLM?.setCurrentScroll(childLM!!.computeCurrentScroll())

                } else {
                    it.notifyDataSetChanged()

                    childLM?.setScrollBarHeight(0)
                    childLM?.setCurrentScroll(childLM!!.computeCurrentScroll())
                }

            }
            childRV = null
            firstVisibleChildView = null
            firstVisibleItem = null
            lastVisibleItem = null
            //}


            //adapter?.notifyDataSetChanged()

            // Set so font size changes apply to the next RecyclerView child.


        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        super.dispatchTouchEvent(ev)

        //Only listen if we have 2 or more touches
        if (ev != null && ev.pointerCount > 1)
            mScaleDetector?.onTouchEvent(ev)

        return true
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val layoutManager = layoutManager as LinearLayoutManager

        val firstPos = layoutManager.findFirstVisibleItemPosition()
        val lastPos = layoutManager.findLastVisibleItemPosition()

        val screenEndX = context.resources.displayMetrics.widthPixels
        val midScreen = (screenEndX / 2)

        for (position in firstPos..lastPos) {
            val view = layoutManager.findViewByPosition(position)!!
            val viewWidth = view.measuredWidth
            val viewStartX = view.x
            val viewEndX = viewStartX + viewWidth
            if (viewEndX >= 0 && viewStartX <= screenEndX) {
                val viewHalfWidth = view.measuredWidth / 2f

                val pageState = pageStatesPool[position - firstPos]
                pageState.index = position
                pageState.view = view
                pageState.viewCenterX = (viewStartX + viewWidth / 2f).toInt()
                pageState.distanceToSettledPixels = (pageState.viewCenterX - midScreen)
                pageState.distanceToSettled = (pageState.viewCenterX + viewHalfWidth) / (midScreen + viewHalfWidth)
                pageStates.add(pageState)
            }
        }
        mCallback?.onPageScrolled(pageStates)

        // Clear this in advance so as to avoid holding refs to views.
        pageStates.clear()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        mCallback?.onPageScrollStateChanged(state)
    }

    inner class PagerSnapHelperVerbose : PagerSnapHelper(),
            ViewTreeObserver.OnGlobalLayoutListener {

        var lastPage = RecyclerView.NO_POSITION

        init {
            viewTreeObserver.addOnGlobalLayoutListener(this)
        }

        override fun onGlobalLayout() {
            val position = lm.findFirstCompletelyVisibleItemPosition()
            if (position != RecyclerView.NO_POSITION) {
                notifyNewPageIfNeeded(position)
                if (Build.VERSION.SDK_INT < 16) {
                    @Suppress("DEPRECATION")
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        }

        override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
            val view = super.findSnapView(layoutManager) ?: return null
            notifyNewPageIfNeeded(getChildAdapterPosition(view))
            return view
        }

        override fun findTargetSnapPosition(
                layoutManager: RecyclerView.LayoutManager?,
                velocityX: Int,
                velocityY: Int
        ): Int {
            val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)

            if (position < adapter!!.itemCount) { // Making up for a "bug" in the original snap-helper.
                notifyNewPageIfNeeded(position)
            }

            return position
        }

        private fun notifyNewPageIfNeeded(page: Int) {
            if (page != lastPage) {
                mCallback?.onPageSelected(page)
                lastPage = page
            }
        }
    }

}


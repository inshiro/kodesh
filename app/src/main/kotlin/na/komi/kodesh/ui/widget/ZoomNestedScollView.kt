package na.komi.kodesh.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import android.view.ScaleGestureDetector
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView


class ZoomNestedScollView : NestedScrollView {

    constructor(context: Context) : super(context) {initialize()}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initialize()}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initialize()}

    private val TAG = "ZoomNestedScollView"
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private var defaultSize: Float = 0.toFloat()
    private var zoomLimit = 4.0f
    private var zoomEnabled = true

    private fun initialize() {
        defaultSize = 5f
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

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
         super.onTouchEvent(ev)


        mScaleDetector?.onTouchEvent(ev)
        return true
    }

    /*Scale Gesture listener class,

    mScaleFactor is getting the scaling value

    and mScaleFactor is mapped between 1.0 and and zoomLimit

    that is 4.0 by default. You can also change it. 4.0 means text

    can zoom to 4 times the default value.*/


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            mScaleFactor *= detector.scaleFactor

            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, zoomLimit))

            (getChildAt(0) as AppCompatTextView).setTextSize(TypedValue.COMPLEX_UNIT_PT, defaultSize * mScaleFactor)

            //Log.e(TAG, mScaleFactor.toString())

            return true

        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            Log.e(TAG, "onScaleBegin")
            return super.onScaleBegin(detector)

        }
        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
            Log.e(TAG, "onScaleEnd")


        }

    }
}
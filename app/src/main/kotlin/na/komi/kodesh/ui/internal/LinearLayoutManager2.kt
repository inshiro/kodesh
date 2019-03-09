package na.komi.kodesh.ui.internal

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import na.komi.kodesh.model.Bible
import na.komi.kodesh.util.createStaticLayout
import na.komi.kodesh.util.log

/**
 * A LinearLayoutManager that attempts to fix the erratic
 * scrollbars while scrolling.
 * https://is.gd/hzNV4s
 * https://is.gd/qQ0xqv
 */
object ScrollListener {
    var scrollY = 0f
    var internalY = 0f
    var width = 0
    var height = 0
    var last = -1
    fun reset() {
        scrollY = 0f
        internalY = 0f
        width = 0
        height = 0
        last = -1
    }
}

open class LinearLayoutManager2 : LinearLayoutManager {

    constructor(context: Context) : super(context)

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        isSmoothScrollbarEnabled = true
    }

    private var rv: RecyclerView? = null
    var currentList: MutableList<*>? = null

    private var mTotalScrolled = computeCurrentScroll()

    fun computeCurrentScroll() :Int = rv?.computeVerticalScrollOffset() ?: 0

    var currentListHeight = 0

    fun setScrollBarHeight(height: Int) {
        currentListHeight = height
    }

    fun getCurrentScroll() = mTotalScrolled

    fun setCurrentScroll(pos: Int) {
        mTotalScrolled = pos
    }

    fun resetScroll() {
        currentListHeight = 0
        mTotalScrolled = 0
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        if (view != null) {
            rv = view
            rv!!.removeOnScrollListener(scrollListener)
            rv!!.addOnScrollListener(scrollListener)
        }
    }

    // This retrieves a general guess scroll position. dy listens to fling events.
    private val scrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                mTotalScrolled += dy
            }
        }
    }


    private val textPaint
        get() = (findViewByPosition(findFirstVisibleItemPosition() + 1) as? AppCompatTextView)?.paint
    var bounds = Rect()
    open fun computeList(): CharSequence? {

        if (!currentList.isNullOrEmpty()) {
            val sb = StringBuilder()
            if (currentList!![0] is Bible) {
                for (i in 0 until currentList!!.lastIndex) {
                    sb.append("${(currentList!![i] as Bible).verseText}\n")
                }
            } else if (currentList!![0] is CharSequence) {
                for (i in 0 until currentList!!.lastIndex) {
                    sb.append("${currentList!![i]}\n")
                }

            }
            return sb
        } else return null

    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        // Call immediately to calculate the correct height
        // and prevent the initial delay of computeVerticalScrollRange
        getListHeight()
    }

    var itemPadding = 0
    fun getListHeight(): Int {
       // log d "currentListHeight: $currentListHeight"
        if (currentListHeight != 0) return currentListHeight
        //if (mTotalScrolled != cScroll) mTotalScrolled = cScroll
        val text = computeList()?.toString()
        return if (textPaint != null && !text.isNullOrEmpty()) {
            createStaticLayout(text, width, textPaint!!).height.also {

                // Calculate Height + Padding
                currentListHeight =
                    it + (rv!!.paddingBottom * 3 + rv!!.paddingTop
                +(findViewByPosition(findFirstVisibleItemPosition())?.let { first ->
                    first.paddingBottom + first.paddingTop
                } ?: 0)).also { padding -> itemPadding = padding - rv!!.paddingBottom }

                //log d "Created Static Layout. Current height: $currentListHeight"
            }
        } else 0
        //textPaint?.getTextBounds(text, 0, text.length, bounds)
    }

    //Computes the vertical size of the scrollbar indicator (thumb)
    /*override fun computeVerticalScrollExtent(state: RecyclerView.State): Int {
        return if (childCount > 0) {
            SMOOTH_VALUE * 3
        } else 0
    }*/

    //Computes the vertical size of all the content (scrollbar track)
    /*override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        return if (ScrollListener.last != -1) ScrollListener.last else Math.max(
            (itemCount - 1) * SMOOTH_VALUE,
            0
        ) //+ ScrollListener.scrollY.toInt()
    }*/
    override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        return getListHeight()
    }

    // map of child adapter position to its height.
    //Computes the vertical distance from the top of the screen (scrollbar position)v
    /*  override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {

          val count = childCount

          if (count <= 0) {
              return 0
          }

          if (findLastCompletelyVisibleItemPosition() == itemCount - 1) {
              ScrollListener.last = ScrollListener.scrollY.toInt()
              return ScrollListener.scrollY.toInt()
              //return Math.max((itemCount - 1) * SMOOTH_VALUE, 0)
          }


          val heightOfScreen: Int
          val firstPos = findFirstVisibleItemPosition()
          if (firstPos == RecyclerView.NO_POSITION) {
              return 0
          }

          val view = findViewByPosition(firstPos) ?: return 0

          // Top of the view in pixels
          val top = getDecoratedTop(view)
          val height = getDecoratedMeasuredHeight(view)
          heightOfScreen = if (height <= 0) 0 else Math.abs(SMOOTH_VALUE * top / height)

          return if (heightOfScreen == 0 && firstPos > 0) {
              SMOOTH_VALUE * firstPos - 1
          } else SMOOTH_VALUE * firstPos + heightOfScreen
      }
  */

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {

        val count = childCount

        if (count <= 0 || mTotalScrolled < 0) {
            mTotalScrolled = 0
            return 0
        }

        if (findFirstCompletelyVisibleItemPosition() == 0) {
            mTotalScrolled = 0
            return 0
        }

        if (findLastCompletelyVisibleItemPosition() == itemCount - 1) {
            currentListHeight= mTotalScrolled  + itemPadding//return Math.max((getItemCount() - 1) * SMOOTH_VALUE, 0);
        }

        return mTotalScrolled//.also { log d "computeVerticalScrollOffset: $it" }
    }


    companion object {
        private val SMOOTH_VALUE = 100
    }
}
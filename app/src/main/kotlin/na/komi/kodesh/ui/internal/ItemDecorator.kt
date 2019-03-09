package na.komi.kodesh.ui.internal

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * https://stackoverflow.com/q/40677024
 */
class ItemDecorator(private val mSpace: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        //val position = parent.getChildAdapterPosition(view)
        val position = parent.getChildViewHolder(view).adapterPosition
        //val position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
        val isLast = position == state.itemCount - 1

        if (isLast) {
            // Bottom margin because RecyclerView has a bottomPadding bug
            outRect.bottom = 400
        } else {
            outRect.bottom = 0
        }
    }

}
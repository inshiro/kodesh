package na.komi.kodesh.ui.internal

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseAdapter.ViewHolder>() {
    open val currentList: MutableList<T> by lazy { mutableListOf<T>() }

    open fun submitList(newList: MutableList<T>)  {
        currentList.clear()
        currentList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = currentList[position].hashCode().toLong().let { if (it==0L) position.toLong() else it }
    override fun getItemCount(): Int = currentList.size
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    abstract override fun onBindViewHolder(holder: ViewHolder, position: Int)
    abstract class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}
package na.komi.kodesh.ui.search

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.recyclerview_content_search.*
import na.komi.kodesh.Application
import na.komi.kodesh.R
import na.komi.kodesh.model.Bible
import na.komi.kodesh.util.page.Fonts
import na.komi.kodesh.util.snackbar
import na.komi.kodesh.util.text.futureSet


class SearchAdapter : PagedListAdapter<Bible, SearchAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Bible>() {

    override fun areItemsTheSame(oldItem: Bible, newItem: Bible): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Bible, newItem: Bible): Boolean =
        oldItem == newItem
}) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id?.toLong() ?: position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recyclerview_content_search,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //GlobalScope.launch(Dispatchers.IO) {
        val bible = getItem(position)
        bible?.let { holder.bind(it) }
        //}
    }

    fun View.getActivity(): Activity? {
        var context = this.context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            @Suppress("USELESS_CAST")
            context = (context as ContextWrapper).baseContext
        }
        return null
    }

    private val clipboard by lazy {
        Application.instance.applicationContext.getSystemService(
            Context.CLIPBOARD_SERVICE
        ) as ClipboardManager?
    }
    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val cardView: CardView = item_card_view
        val title: AppCompatTextView = card_search_title
        val content: AppCompatTextView = card_search_content

        init {

            title.typeface = Fonts.Merriweather_Black
            content.typeface = Fonts.GentiumPlus_R
            cardView.setOnLongClickListener {
                val title = title.text
                it.getActivity()?.findViewById<CoordinatorLayout>(R.id.container_main)?.let {coordinatorLayout ->
                    Snackbar.make(coordinatorLayout,"Selected $title",Snackbar.LENGTH_SHORT).setAction("Copy") {
                        clipboard?.primaryClip = ClipData.newPlainText("Search text", "$title\n${content.text}")
                        Toast.makeText(coordinatorLayout.context,"Copied verse", Toast.LENGTH_SHORT).show()
                    }.show()
                }
                false
            }

        }

        fun bind(item: Bible) {
            title.futureSet("${item.bookName!!} ${item.chapterId!!}:${item.verseId!!}")
            content.futureSet(item.verseText!!.replace("[", "").replace("]", ""))
        }
    }

}
package na.komi.kodesh.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.card_item.*
import kotlinx.android.synthetic.main.fragment_about.view.*
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.*
import na.komi.kodesh.util.inflateView
import na.komi.kodesh.util.log
import na.komi.kodesh.util.snackbar
import na.komi.kodesh.ui.widget.NestedRecyclerView
import na.komi.kodesh.util.text.futureSet
import java.security.InvalidParameterException


class AboutFragment : BaseFragment2() {
    override val layout: Int = R.layout.fragment_about

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getToolbar()?.post {
            getToolbar()?.let {
                it.title = getString(R.string.about_title)
                for (a in it.menu.children)
                    a.isVisible = false
            }
            getToolbarTitleView()?.futureSet(getString(R.string.about_title))
        }
        getNavigationView().setCheckedItem(R.id.action_about)

        val tabLayout = view?.tab_layout
        val libraries = listOf(
            Library(
                "Android support libraries",
                "The Android support libraries offer a number of features that are " +
                        "not built into the framework.",
                "https://developer.android.com/topic/libraries/support-library",
                "https://avatars.githubusercontent.com/u/32689599",
                false
            ),
            Library(
                "android-ktx",
                "A set of Kotlin extensions for Android app development.",
                "https://android.googlesource.com/platform/frameworks/support/",
                "https://avatars.githubusercontent.com/u/32689599",
                false
            ),
            Library(
                "Katana",
                "A lightweight, minimalistic dependency injection library for Kotlin on the JVM.",
                "https://github.com/rewe-digital/katana",
                "",
                false
            )
        )

        val uiModel =
            AboutUiModel(
                null,
                null,
                LibrariesUiModel(libraries) {
                    onLibraryClick(it)
                })

        val viewPager = view?.about_view_pager
        viewPager?.apply {
            tabLayout?.setupWithViewPager(this)
            adapter = AboutAdapter(uiModel)
        }


    }

    internal fun onLibraryClick(library: Library) {
        log d "Click on ${library.name}"
        view?.snackbar("Click on ${library.name}")
        val url = library.link
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
        //_navigationTarget.value = Event(library.link)
    }
}

/**
 * Hold values displayed in the about Ui.
 */
internal data class AboutUiModel(
    val appAboutText: CharSequence? = null,
    val iconAboutText: CharSequence? = null,
    val librariesUiModel: LibrariesUiModel
)

internal typealias OnClick = (library: Library) -> Unit

/**
 * * Hold values displayed in the libraries Ui.
 */
internal data class LibrariesUiModel(val libraries: List<Library>, val onClick: OnClick)

/**
 * Models an open source library we want to credit
 */
internal data class Library(
    val name: String,
    val description: String,
    val link: String,
    val imageUrl: String,
    val circleCrop: Boolean
)

/**
 * To prevent creating 2 RecyclerViews with 2 Adapters.
 * We create 1 RecyclerViewPager, that creates n amount
 * of Child RecyclerViews and Adapters.
 */
internal class AboutAdapter(private val uiModel: AboutUiModel) : PagerAdapter() {
    private var aboutMain: View? = null
    private var aboutLibs: View? = null

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        return getPage(position, collection).also {
            collection.addView(it)
        }
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int = 2

    override fun isViewFromObject(view: View, obj: Any) = view === obj

    private fun getPage(position: Int, parent: ViewGroup): View {
        return when (position) {
            0 -> getAboutMainPage(parent)
            1 -> aboutLibs ?: (LayoutInflater.from(parent.context).inflate(
                R.layout.recyclerview_child,
                parent,
                false
            ) as NestedRecyclerView).apply {
                //(itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                isVerticalScrollBarEnabled = false
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false).apply {
                    isItemPrefetchEnabled = true
                }
                adapter = AboutAdapterLibs(uiModel.librariesUiModel)
                aboutLibs = this
            }
            else -> throw InvalidParameterException()
        }
    }

    private fun getAboutMainPage(parent: ViewGroup): View {
        return aboutMain ?: parent.inflateView(R.layout.recyclerview_child).apply {
            findViewById<NestedRecyclerView>(R.id.child_recycler_view).apply {
                adapter = AboutAdapterMain()
            }
            aboutMain = this
        }
    }

    private fun getAboutLibsPage(parent: ViewGroup): View {
        return aboutMain ?: (parent.inflateView(R.layout.recyclerview_child) as NestedRecyclerView).apply {
            layoutManager = LinearLayoutManager2(context, RecyclerView.VERTICAL, false).apply {
                isItemPrefetchEnabled = true
            }
            adapter = AboutAdapterLibs(uiModel.librariesUiModel)
            aboutLibs = this
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Info"
            else -> "Licences"
        }
    }
}

class AboutAdapterMain : BaseAdapter<String>() {

    // Elements in list are irrelevant in this parent RecyclerView. We just need the list size.
    //override val currentList: MutableList<String> = mutableListOf("About", "Licenses")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false))
    }

    override fun onBindViewHolder(holder: BaseAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    inner class ViewHolder(override val containerView: View) : BaseAdapter.ViewHolder(containerView) {
        //val crv = child_recycler_view

        fun bind(position: Int) {
            if (position == 0) {
                // (crv.adapter as AboutChildAdapter).submitList(list)
            } else {

            }
        }
    }
}

internal class AboutAdapterLibs(val uiModel: LibrariesUiModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LibraryHolder).bind(uiModel.libraries[position])
    }

    override fun getItemCount(): Int = uiModel.libraries.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LibraryHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false),
            uiModel.onClick
        )
    }

    inner class LibraryHolder(
        override val containerView: View,
        private val onClick: OnClick
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private var library: Library? = null

        private var image: ImageView = library_image
        private var name: TextView = library_name
        private var description: TextView = library_description
        private var license: TextView = sub_item
        private var link: MaterialButton = library_link
        private var katana: Drawable? = null

        init {
            katana ?: image.also { katana = ContextCompat.getDrawable(image.context, R.drawable.ic_inject) }
            View.OnClickListener { library?.let { onClick(it) } }.apply {
                //itemView.setOnClickListener(this)
                link.setOnClickListener(this)
            }
            View.OnClickListener {
                license.visibility = if (license.visibility == View.GONE)
                    View.VISIBLE.also { link.visibility = View.GONE }
                else
                    View.GONE.also { link.visibility = View.VISIBLE }
                log d "adapterPosition: $adapterPosition"
                notifyDataSetChanged()
                //notifyItemChanged(adapterPosition)
            }.apply {
                itemView.setOnClickListener(this)
                //link.setOnClickListener(this)
            }
        }

        @SuppressLint("CheckResult")
        fun bind(lib: Library) {
            library = lib
            name.text = lib.name
            description.text = lib.description
            if (lib.name == "Katana") {
                image.setImageDrawable(katana)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    image.imageTintList = null
                }
            }

            /*val request = GlideApp.with(image.context)
                .load(lib.imageUrl)
                .transition(withCrossFade())
                .placeholder(appR.drawable.avatar_placeholder)
            if (lib.circleCrop) {
                request.circleCrop()
            }
            request.into(image)*/
        }
    }
}











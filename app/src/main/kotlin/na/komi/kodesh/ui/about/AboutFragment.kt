package na.komi.kodesh.ui.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.children
import androidx.core.view.updatePadding
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.card_item.*
import kotlinx.android.synthetic.main.fragment_about.view.*
import na.komi.kodesh.BuildConfig
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.BaseAdapter
import na.komi.kodesh.ui.internal.BaseFragment2
import na.komi.kodesh.ui.widget.BaselineGridTextView
import na.komi.kodesh.util.log
import na.komi.kodesh.util.onClick
import na.komi.kodesh.util.text.futureSet
import na.komi.kodesh.util.text.withSpan
import java.security.InvalidParameterException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AboutFragment : BaseFragment2() {
    override val layout: Int = R.layout.fragment_about

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
            ),
            Library(
                "Skate",
                "A simple, seamless and lightweight, fragment stack controller for Android with Kotlin.",
                "https://github.com/inshiro/skate",
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

        val viewPager = view?.findViewById<ViewPager>(R.id.about_view_pager)
        viewPager?.apply {
            tabLayout?.setupWithViewPager(this)
            adapter = AboutAdapter(uiModel)
        }
        viewPager?.post {

            getToolbar()?.let {
                it.title = getString(R.string.about_title)
                for (a in it.menu.children)
                    a.isVisible = false
            }
            getToolbarTitleView()?.futureSet(getString(R.string.about_title))
            getNavigationView().setCheckedItem(R.id.action_about)
        }


    }

    internal fun onLibraryClick(library: Library) {
        log d "Click on ${library.name}"
        //view?.snackbar("Click on ${library.name}")
        val url = library.link
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
        //_navigationTarget.value = Event(library.websiteLink)
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
            1 -> getAboutLibsPage(parent)
            else -> throw InvalidParameterException()
        }
    }

    private fun getFormattedBuildTime(): String {
        return try {
            val inputDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.CANADA)
            inputDf.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputDf.parse(BuildConfig.BUILD_TIME)

            val outputDf = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()
            )
            outputDf.timeZone = TimeZone.getDefault()

            outputDf.format(date)
        } catch (e: ParseException) {
            BuildConfig.BUILD_TIME
        }
    }

    private fun getAboutMainPage(parent: ViewGroup): View {
        return aboutMain ?: (LayoutInflater.from(parent.context).inflate(
            R.layout.card_item_single,
            parent,
            false
        )).apply {
            val versionText = findViewById<BaselineGridTextView>(R.id.version_text)
            val buildText = findViewById<BaselineGridTextView>(R.id.build_text)
            val s = SpannableStringBuilder("Version\n")
            s.withSpan(TextAppearanceSpan(versionText.context, R.style.TextAppearance_AppCompat_Caption)) {
                append(BuildConfig.VERSION_NAME)
            }
            versionText.text = s
            s.clear()
            s.clearSpans()
            s.append("Build time\n")
            s.withSpan(TextAppearanceSpan(versionText.context, R.style.TextAppearance_AppCompat_Caption)) {
                append(getFormattedBuildTime())
            }
            buildText.text = s
            findViewById<BaselineGridTextView>(R.id.source_code_text)?.apply {
                onClick {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse("https://github.com/inshiro/kodesh")
                    context.startActivity(i)

                }
            }
            findViewById<BaselineGridTextView>(R.id.feedback_text)?.apply {
                setTextViewDrawableColor()
                onClick {
                    val address = "inshirodev@gmail.com"
                    val model = android.os.Build.MODEL
                    val manufacturer = android.os.Build.MANUFACTURER
                    val api = android.os.Build.VERSION.SDK_INT
                    val androidVersion = android.os.Build.VERSION.RELEASE
                    val body =
                        "\n\nMy device info:\n$manufacturer $model / API level $api, version $androidVersion\nApp version: ${BuildConfig.VERSION_NAME}"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$address") // only email apps should handle this
                        //type = "*/*"
                        //putExtra(Intent.EXTRA_EMAIL, address)
                        putExtra(Intent.EXTRA_SUBJECT, "[Kodesh] Feedback")
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
            }

            findViewById<BaselineGridTextView>(R.id.rate_text)?.apply {
                setTextViewDrawableColor()
                onClick {
                    val appPackageName = BuildConfig.APPLICATION_ID
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                        if (intent.resolveActivity(context.packageManager) != null)
                            context.startActivity(intent)
                    } catch (anfe: ActivityNotFoundException) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                        if (intent.resolveActivity(context.packageManager) != null)
                            context.startActivity(intent)
                    }
                }

            }
            aboutMain = this
        }
    }

    private var tv: TypedValue? = null
    private val AppCompatTextView.textColor: Int
        get() {
            if (tv == null) {
                tv = TypedValue()
                context.theme.resolveAttribute(R.attr.textColor, tv, true)
            }
            return tv!!.resourceId
        }

    private fun AppCompatTextView.setTextViewDrawableColor(color: Int? = null) {
        val c = color ?: this.textColor
        for (drawable: Drawable? in this.compoundDrawables) {
            drawable?.colorFilter =
                PorterDuffColorFilter(getColor(this@setTextViewDrawableColor.context, c), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun getAboutLibsPage(parent: ViewGroup): View {
        return aboutLibs ?: (LayoutInflater.from(parent.context).inflate(
            R.layout.recyclerview_standard,
            parent,
            false
        ) as RecyclerView).apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            isVerticalScrollBarEnabled = false
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            clipToPadding = false
            adapter = AboutAdapterLibs()
            updatePadding(bottom = 500)
            aboutLibs = this
        }.also { (it.adapter as AboutAdapterLibs).setModel(uiModel.librariesUiModel) }
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

internal class AboutAdapterLibs : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var uiModel: LibrariesUiModel

    fun setModel(model: LibrariesUiModel) {
        uiModel = model
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LibraryHolder).bind(uiModel.libraries[position])
    }

    override fun getItemCount(): Int = if (!::uiModel.isInitialized) 0 else uiModel.libraries.size

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

        private val cardItem: CardView = card_item
        private val image: AppCompatImageView = library_image
        private val name: TextView = library_name
        private val description: TextView = library_description
        private val license: AppCompatTextView = sub_item
        private val websiteLink: MaterialButton = library_link

        private var katana: Drawable? = null
        private var skate: Drawable? = null
        private var androidIcon: Drawable? = null
        private var androidSupportApache = ""
        private var androidKTXApache = ""
        private var apache = ""
        private var mit = ""

        private val textColor by lazy {
            val typedValue = TypedValue()
            name.context.theme.resolveAttribute(R.attr.textColor, typedValue, true)
            typedValue.resourceId
        }

        private fun AppCompatTextView.setTextViewDrawableColor(color: Int) {
            for (drawable: Drawable? in this.compoundDrawables) {
                drawable?.colorFilter = PorterDuffColorFilter(
                    getColor(this@setTextViewDrawableColor.context, color),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }

        init {
            katana ?: image.also { katana = ContextCompat.getDrawable(it.context, R.drawable.ic_inject) }
            skate ?: image.also { skate = ContextCompat.getDrawable(it.context, R.drawable.ic_skateboard) }
            androidIcon ?: image.also { androidIcon = ContextCompat.getDrawable(it.context, R.drawable.ic_android) }
            if (androidSupportApache.isEmpty()) {
                androidSupportApache = licenseApache("Copyright 2017 The Android Open Source Project")
            }
            if (androidKTXApache.isEmpty()) {
                androidKTXApache = licenseApache("Copyright 2018 The Android Open Source Project")
            }
            if (apache.isEmpty()) {
                apache = licenseApache("Copyright 2019 inshiro")
            }
            if (mit.isEmpty()) {
                mit = licenseMIT("Copyright (c) 2019 REWE Digital GmbH")
            }
            View.OnClickListener { library?.let { onClick(it) } }.apply {
                //itemView.setOnClickListener(this)
                websiteLink.setOnClickListener(this)
            }
            View.OnClickListener {
                license.visibility = if (license.visibility == View.GONE)
                    View.VISIBLE.also { websiteLink.visibility = View.GONE }
                else
                    View.GONE.also { websiteLink.visibility = View.VISIBLE }
                log d "adapterPosition: $adapterPosition"
                notifyDataSetChanged()
                //notifyItemChanged(adapterPosition)
            }.apply {
                cardItem.setOnClickListener(this)
                //websiteLink.setOnClickListener(this)
            }
        }

        @SuppressLint("CheckResult")
        fun bind(lib: Library) {
            library = lib
            name.text = lib.name
            description.text = lib.description
            when {
                lib.name == "Katana" -> {
                    ImageViewCompat.setImageTintList(
                        image,
                        ContextCompat.getColorStateList(image.context, textColor)
                    )
                    image.setImageDrawable(katana)
                    license.text = mit
                }
                lib.name == "Skate" -> {
                    ImageViewCompat.setImageTintList(image, null)
                    image.setImageDrawable(skate)
                    license.text = apache
                }
                else -> {
                    ImageViewCompat.setImageTintList(
                        image,
                        ContextCompat.getColorStateList(image.context, android.R.color.holo_green_light)
                    )
                    image.setImageDrawable(androidIcon)
                    if (lib.name == "Android support libraries")
                        license.text = androidSupportApache
                    else if (lib.name == "android-ktx")
                        license.text = androidKTXApache
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

        fun licenseApache(tag: String): String {
            return "$tag\n" +
                    "\n" +
                    "Licensed under the Apache License, Version 2.0 (the \"License\"); " +
                    "you may not use this file except in compliance with the License. " +
                    "You may obtain a copy of the License at\n" +
                    "\n" +
                    "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "Unless required by applicable law or agreed to in writing, software " +
                    "distributed under the License is distributed on an \"AS IS\" BASIS, " +
                    "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. " +
                    "See the License for the specific language governing permissions and " +
                    "limitations under the License."
        }

        fun licenseMIT(tag: String): String {
            return "The MIT license (MIT)\n" +
                    "\n" +
                    "$tag\n" +
                    "\n" +
                    "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated " +
                    "documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the " +
                    "rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit " +
                    "persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
                    "\n" +
                    "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the " +
                    "Software.\n" +
                    "\n" +
                    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE " +
                    "WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR " +
                    "COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR " +
                    "OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE."
        }
    }
}











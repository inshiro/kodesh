package na.komi.kodesh.ui.internal

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.ui.find.FindInPageFragment
import na.komi.kodesh.ui.main.Components
import na.komi.kodesh.util.*
import na.komi.kodesh.widget.LayoutedTextView
import kotlin.coroutines.CoroutineContext

/**
 *
 * Base activity for any activity that would have extended [AppCompatActivity]
 *
 * Ensures that some singleton methods are called.
 * This is simply a convenience class;
 * you can always copy and paste this to your own class.
 *
 * This also implements [CoroutineScope] that adheres to the activity lifecycle.
 * Note that by default, [SupervisorJob] is used, to avoid exceptions in one child from affecting that of another.
 * The default job can be overridden within [defaultJob]
 */
abstract class BaseActivity : AppCompatActivity(), CoroutineScope, TitleListener {

    //open lateinit var job: Job
    open val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = ContextHelper.dispatcher + job

    // open fun defaultJob(): Job = SupervisorJob()

    abstract val layout: Int
    //abstract val navigationView: NavigationView
    // abstract val toolbar: Toolbar
    private lateinit var navigationView: NavigationView

    fun getNavigationView() = navigationView

    private var toolbar: Toolbar? = null
    fun getToolbar(): Toolbar? = toolbar ?: findViewById<Toolbar>(R.id.toolbar_main).also { toolbar = it }

    fun getToolbarTitleView(): AppCompatTextView? {
        //var toolbarTitle:AppCompatTextView? = null
        if (toolbarTitle != null) return toolbarTitle
        getToolbar()?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChildAt(i)
                if (child is AppCompatTextView || child is LayoutedTextView || child is TextView) {
                    return child as? AppCompatTextView
                    //break
                }
            }
        }
        return toolbarTitle
    }

    private var toolbarTitle: AppCompatTextView? = null
    private val knavigator: Knavigator  by Components.navComponent.inject()
    private val findInPageFragment: FindInPageFragment by Components.fragComponent.inject()

    private val isLargeLayout
        get() = resources.getBoolean(R.bool.large_layout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (Prefs.themeId) {
            1 -> setTheme(R.style.Theme_Shrine_Dark)
            2 -> setTheme(R.style.Theme_Shrine_Black)
            else -> setTheme(R.style.AppTheme)
        }
        setContentView(layout)

        val navController = findNavController(R.id.nav_host_fragment)
        navigationView = findViewById(R.id.navigation_view)

        mBottomSheetContainer = findViewById(R.id.main_bottom_container)
        toolbar?.inflateMenu(R.menu.toolbar_menu_main)

        mBottomSheetBehavior = BottomSheetBehavior2.from(mBottomSheetContainer)
        mBottomSheetBehavior.peekHeight = toolbar?.measuredHeight?.let { if (it <= 0) 147 else it } ?: 147
        mBottomSheetBehavior.state = BottomSheetBehavior2.STATE_COLLAPSED

        getToolbar()?.menu?.findItem(R.id.ham_menu)?.setOnMenuItemClickListener {
            mBottomSheetBehavior.toggle()
        }

        knavigator.fragmentManager = supportFragmentManager
        knavigator.container = R.id.container_main

        // When we press back it pops it
        supportFragmentManager.addOnBackStackChangedListener {
            val l = supportFragmentManager.fragments
            val current = l[l.lastIndex]::class.java.simpleName
            supportFragmentManager.fragments.map { it::class.java.simpleName }.joinToString(",").also { log d it }
        }
        /**
         * https://stackoverflow.com/a/37873884
         * https://stackoverflow.com/a/36793341
         */
        navigationView.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                it.setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
                    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
                        return insets
                    }
                })
            }

            it.setNavigationItemSelectedListener { item ->
                mBottomSheetBehavior.close()
                setLowProfileStatusBar()
                when (item.itemId) {
                    R.id.action_find_in_page -> {
                        knavigator.show(findInPageFragment)
                        //item.isEnabled = false
                    }
                    R.id.action_about -> knavigator.hide(findInPageFragment)

                }
                !item.isChecked
            }
            /*     it.setNavigationItemSelectedListener { item ->
                     mBottomSheetBehavior.close()
                     setLowProfileStatusBar()
                     // Prevent pressing self
                     if (!item.isChecked) {
                         displayFindInPage(false)
                         it.postDelayed({
                             if (navController.currentDestination?.id != R.id.mainFragment)
                                 navController.popBackStack(R.id.mainFragment, false)
                             when (item.itemId) {
                                 R.id.action_read -> {}
                                 R.id.action_find_in_page -> {
                                     if (!findInPageFragment.isAdded)
                                         supportFragmentManager.beginTransaction()
                                             .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                             .add(R.id.container_main, findInPageFragment, FindInPageFragment::class.java.simpleName)
                                             .addToBackStack(FindInPageFragment::class.java.simpleName)
                                             .commit()
                                     else displayFindInPage()
                                     item.isEnabled = false
                                 }
                                 R.id.action_preface -> navController.navigate(R.id.toPreface)
                                 R.id.action_search -> navController.navigate(R.id.toSearch)
                                 R.id.action_settings -> navController.navigate(R.id.toSettings)
                                 R.id.action_about -> navController.navigate(R.id.toAbout)
                             }
                         }, 200)
                         true
                     } else {
                         //log d "pressed checked item"
                         false
                     }
                 }
            */

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    val bottomSheetBehavior by lazy { mBottomSheetBehavior }
    val bottomSheetContainer by lazy { mBottomSheetContainer }
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior2<ConstraintLayout>
    private lateinit var mBottomSheetContainer: ConstraintLayout


    /*
        private fun getBitmapFromView(view: View, activity: AppCompatActivity, callback: (Bitmap) -> Unit) {
            activity.window?.let { window ->
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val locationOfViewInWindow = IntArray(2)
                view.getLocationInWindow(locationOfViewInWindow)
                try {
                    PixelCopy(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1], locationOfViewInWindow[0] + view.width, locationOfViewInWindow[1] + view.height), bitmap, { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        }
                        // possible to handle other result codes ...
                    }, Handler())
                } catch (e: IllegalArgumentException) {
                    // PixelCopy may throw IllegalArgumentException, make sure to handle it
                    e.printStackTrace()
                }
            }
        }
        fun runCircleAnimation() {
            val size = ViewGroup.LayoutParams.MATCH_PARENT
            val decorView = getWindow().getDecorView() as ViewGroup
            val v1 = getBitmapFromView(decorView)
            val imageView = ImageView(this)
            imageView.setImageDrawable(BitmapDrawable(resources, v1))
            decorView.addView(imageView, ViewGroup.LayoutParams(size, size))
            val animator = imageView.animate().alpha(0).setDuration(300)
            animator.setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    decorView.removeView(imageView)
                }
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                }
            })
            animator.start()
        }*/
    override fun onStart() {
        super.onStart()
        val a =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) //findFragmentByTag(MainFragment::class.java.name)
        val v = a!!.view!!
        //ViewCompat.setBackground(v, ColorDrawable(ContextCompat.getColor(this, R.color.default_background_color)))
        mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            val min = 0.5f
            val multiplier = 1f + min

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val alpha = Math.abs(slideOffset - 1f)
                v.alpha = (alpha + min) / multiplier // Prevent from hitting 0, then normalize
            }

        })
    }

    override fun onBackPressed() {
        //  if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.mainFragment)
        //     bottomSheetContainer.invalidate()
        if (mBottomSheetBehavior.state == BottomSheetBehavior2.STATE_EXPANDED)
            mBottomSheetBehavior.setState(BottomSheetBehavior2.STATE_COLLAPSED)
        else if (knavigator.canGoBack())
            knavigator.goBack()
        else
            super.onBackPressed()
    }

    fun setupStatusBar() {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.navigationBarColor =
        //    ContextCompat.getColor(this, R.color.colorAccent)
        /*TranslucentBarManager(this).transparent(this)
        setLowProfileStatusBar()
        setLightStatusBar()
        setLightNavBar()
    */
    }

}


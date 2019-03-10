package na.komi.kodesh.ui.internal

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.ui.about.AboutFragment
import na.komi.kodesh.ui.find.FindInPageFragment
import na.komi.kodesh.ui.main.MainComponents
import na.komi.kodesh.ui.main.MainFragment
import na.komi.kodesh.ui.preface.PrefaceFragment
import na.komi.kodesh.ui.search.SearchFragment
import na.komi.kodesh.ui.setting.SettingsFragment
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
    val knavigator: Knavigator  by MainComponents.navComponent.inject()
    private val findInPageFragment: FindInPageFragment by MainComponents.fragComponent.inject()
    private val prefaceFragment: PrefaceFragment by MainComponents.fragComponent.inject()
    private val searchFragment: SearchFragment by MainComponents.fragComponent.inject()
    private val settingsFragment: SettingsFragment by MainComponents.fragComponent.inject()
    private val aboutFragment: AboutFragment by MainComponents.fragComponent.inject()

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

        //val navController = findNavController(R.id.nav_host_fragment)
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
                knavigator.container = R.id.nav_main_container
                when (item.itemId) {
                    R.id.action_read -> {}
                    R.id.action_find_in_page -> {
                        knavigator.container = R.id.container_main
                        knavigator.show(findInPageFragment)
                        //item.isEnabled = false
                    }
                    R.id.action_preface -> knavigator.navigate(prefaceFragment)
                    R.id.action_search -> knavigator.navigate(searchFragment)
                    R.id.action_settings -> knavigator.navigate(settingsFragment)
                    R.id.action_about -> knavigator.navigate(aboutFragment)

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
       override fun onStart() {
           super.onStart()
           val a = supportFragmentManager.findFragmentByTag(MainFragment::class.java.simpleName)
           val v = a?.view
           v?.setBackgroundColor(Color.BLACK)
       }

       override fun onStart() {
           super.onStart()
           val a =
               supportFragmentManager.findFragmentByTag(MainFragment::class.java.name) //.findFragmentById(R.id.nav_host_fragment) //
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
       }*/

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


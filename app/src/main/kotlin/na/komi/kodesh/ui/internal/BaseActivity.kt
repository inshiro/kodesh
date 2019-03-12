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
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import na.komi.kodesh.util.knavigator.Knavigator
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
    abstract val layout: Int
    open val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = ContextHelper.dispatcher + job
    val knavigator: Knavigator  by MainComponents.navComponent.inject()

    val bottomSheetBehavior: BottomSheetBehavior2<ConstraintLayout>
        get() = BottomSheetBehavior2.from(bottomSheetContainer)
    val bottomSheetContainer: ConstraintLayout
        get() = findViewById(R.id.main_bottom_container)


    private val isLargeLayout
        get() = resources.getBoolean(R.bool.large_layout)

    fun getNavigationView(): NavigationView = findViewById(R.id.navigation_view)
    fun getToolbar(): Toolbar? = findViewById(R.id.toolbar_main)

    fun getToolbarTitleView(): AppCompatTextView? {
        getToolbar()?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChildAt(i)
                if (child is AppCompatTextView || child is LayoutedTextView || child is TextView) {
                    return child as? AppCompatTextView
                    //break
                }
            }
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (Prefs.themeId) {
            1 -> setTheme(R.style.Theme_Shrine_Dark)
            2 -> setTheme(R.style.Theme_Shrine_Black)
            else -> setTheme(R.style.AppTheme)
        }
        setContentView(layout)

        //val navController = findNavController(R.id.nav_host_fragment)
        val navigationView: NavigationView = getNavigationView()
        val toolbar = getToolbar()
        val mBottomSheetContainer: ConstraintLayout = findViewById(R.id.main_bottom_container)
        toolbar?.inflateMenu(R.menu.toolbar_menu_main)

        val mBottomSheetBehavior = BottomSheetBehavior2.from(mBottomSheetContainer)
        mBottomSheetBehavior.peekHeight = toolbar?.measuredHeight?.let { if (it <= 0) 147 else it } ?: 147
        mBottomSheetBehavior.state = BottomSheetBehavior2.STATE_COLLAPSED

        getToolbar()?.menu?.findItem(R.id.ham_menu)?.setOnMenuItemClickListener {
            mBottomSheetBehavior.toggle()
        }

        val mainFragment by lazy {
            supportFragmentManager.findFragmentByTag(MainFragment::class.java.simpleName) as? MainFragment
                ?: MainFragment()
        }
        knavigator setFragmentManager supportFragmentManager
        if (savedInstanceState == null) {
            knavigator.container = R.id.nav_main_container
            knavigator.show(mainFragment, addToBackStack = false)
        }
        val findInPageFragment by lazy {
            supportFragmentManager.findFragmentByTag(FindInPageFragment::class.java.simpleName) as? FindInPageFragment
                ?: FindInPageFragment()
        }
        val prefaceFragment by lazy {
            supportFragmentManager.findFragmentByTag(PrefaceFragment::class.java.simpleName) as? PrefaceFragment
                ?: PrefaceFragment()
        }
        val searchFragment by lazy {
            supportFragmentManager.findFragmentByTag(SearchFragment::class.java.simpleName) as? SearchFragment
                ?: SearchFragment()
        }
        val settingsFragment by lazy {
            supportFragmentManager.findFragmentByTag(SettingsFragment::class.java.simpleName) as? SettingsFragment
                ?: SettingsFragment()
        }
        val aboutFragment by lazy {
            supportFragmentManager.findFragmentByTag(AboutFragment::class.java.simpleName) as? AboutFragment
                ?: AboutFragment()
        }
        var prevTitle: CharSequence? = null
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

                if (knavigator.current is MainFragment)
                    prevTitle = getToolbarTitleView()?.text

                // Prevent pressing self
                if (!item.isChecked) {
                    when (item.itemId) {
                        R.id.action_read -> {
                            /*
                            val f = knavigator.current
                            if (f != null && f::class.java.simpleName != MainFragment::class.java.simpleName)
                                knavigator.hide(f)*/
                            knavigator navigate mainFragment
                            navigationView.menu.findItem(R.id.action_find_in_page).isEnabled = true
                            getToolbar()?.title = prevTitle
                        }
                        R.id.action_find_in_page -> {
                            knavigator.container = R.id.container_main
                            knavigator.show(findInPageFragment, modular = true)
                            item.isEnabled = false
                        }
                        R.id.action_preface -> {
                            knavigator navigate prefaceFragment
                        }
                        R.id.action_search -> knavigator navigate searchFragment
                        R.id.action_settings -> knavigator navigate settingsFragment
                        R.id.action_about -> knavigator navigate aboutFragment

                    }
                }
                !item.isChecked
            }

            knavigator.setOnHideListener(object : Knavigator.OnNavigateListener {
                override fun onBackPressed(isModular: Boolean) {
                    val fragment = knavigator.current
                    if (fragment is MainFragment) {
                        navigationView.setCheckedItem(R.id.action_read)
                        navigationView.menu.findItem(R.id.action_find_in_page).isEnabled = true
                        getToolbar()?.title = prevTitle
                    }
                }
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    /*
       override fun onStart() {
           super.onStart()
           val a = supportFragmentManager.findFragmentByTag(MainFragment::class.java.simpleName)
           val v = a?.view
           v?.setBackgroundColor(Color.BLACK)
       }*/

    override fun onStart() {
        super.onStart()
        //ViewCompat.setBackground(v, ColorDrawable(ContextCompat.getColor(this, R.color.default_background_color)))
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            val min = 0.5f
            val multiplier = 1f + min

            var f = knavigator.current?.let {
                if (it is MainFragment) it
                else null
            }
            var v = f?.view
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    knavigator.current?.let {
                        if (it is MainFragment) f = it
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val alpha = Math.abs(slideOffset - 1f)
                v?.alpha = (alpha + min) / multiplier // Prevent from hitting 0, then normalize
            }

        })
    }

    override fun onBackPressed() {
        //  if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.mainFragment)
        //     bottomSheetContainer.invalidate()
        val mBottomSheetBehavior = bottomSheetBehavior
        if (mBottomSheetBehavior.state == BottomSheetBehavior2.STATE_EXPANDED)
            mBottomSheetBehavior.state = BottomSheetBehavior2.STATE_COLLAPSED
        else if (!knavigator.goBack())
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


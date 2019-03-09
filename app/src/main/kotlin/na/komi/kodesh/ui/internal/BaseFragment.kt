package na.komi.kodesh.ui.internal

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import na.komi.kodesh.Application
import na.komi.kodesh.R
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.util.close
import na.komi.kodesh.util.onClick
import na.komi.kodesh.util.toggle
import org.rewedigital.katana.Component
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.android.fragment.KatanaFragmentDelegate
import org.rewedigital.katana.android.fragment.fragmentDelegate
import kotlin.coroutines.CoroutineContext


interface TitleListener {
    fun onToolbarTitleClick() {}
}

interface InjectListener {
    fun onInject(activity: Activity) {}
}

abstract class BaseFragment : Fragment(), CoroutineScope, TitleListener, InjectListener {

    private val fragmentDelegate: KatanaFragmentDelegate<BaseFragment>

    init {
        fragmentDelegate = fragmentDelegate { activity -> onInject(activity) }
    }

    open val job = SupervisorJob()

    // open fun defaultJob(): Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job//ContextHelper.dispatcher + job

    private var toolbar: Toolbar? = null
    private var toolbarTitle: AppCompatTextView? = null
    private var bottomSheetContainer: ConstraintLayout? = null
    private var navigationView: NavigationView? = null
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior2<ConstraintLayout>

    abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    private val imm by lazy { Application.instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    fun showKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun View.hideKeyboard() {
        imm.hideSoftInputFromWindow(this.windowToken, 0);
    }

    /**
     * getBottomSheetBehavior
     * getToolbarTitleView
     * getNavigationView
     */
    fun getBottomSheetContainer(): ConstraintLayout? = act()?.bottomSheetContainer

    fun getBottomSheetBehavior(): BottomSheetBehavior2<ConstraintLayout>? = act()?.bottomSheetBehavior

    fun getToolbarTitleView(): AppCompatTextView? = act()?.getToolbarTitleView()

    fun getNavigationView(): NavigationView = navigationView!!

    fun getToolbar(): Toolbar? = act()?.getToolbar()

    private fun act() = (activity as? MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    //protected abstract fun onToolbarTitleClick() {}

    protected abstract fun ToolbarBuilder(): FragmentToolbar
    private val builder by lazy { ToolbarBuilder() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.apply {
            bottomSheetContainer = findViewById(builder.bottomSheet)
            navigationView = findViewById(builder.navigationView)

        }
        getToolbar()?.let {
            if (builder.title != -1)
                it.title = getString(builder.title)
            if (builder.menu != -1) {
                it.menu.clear()
                it.inflateMenu(builder.menu)
            }
        }


        if (builder.menuItems != null && builder.menuClicks != null && toolbar != null)
            if (builder.menuItems!!.isNotEmpty() && builder.menuClicks!!.isNotEmpty()) {
                val menu = toolbar!!.menu
                for ((index, menuItemId) in builder.menuItems!!.withIndex()) {
                    menu.findItem(menuItemId)?.setOnMenuItemClickListener(builder.menuClicks!![index])
                }
            }

        getToolbar()?.let {
            for (a in it.menu.children)
                a.isVisible = true
        }
        getNavigationView().let {
            it.setCheckedItem(R.id.action_read)
            for (a in it.menu.children)
                a.isEnabled = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentDelegate.onActivityCreated()
        getToolbarTitleView()?.onClick {
            onToolbarTitleClick()
        }
    }

    fun closeBottomSheet() = getBottomSheetBehavior()?.close()

    fun toggleBottomSheet() = getBottomSheetBehavior()?.toggle()

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

}
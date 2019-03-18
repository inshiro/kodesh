package na.komi.kodesh.ui.internal

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import na.komi.kodesh.R
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.util.close
import na.komi.kodesh.util.onClick
import na.komi.kodesh.util.toggle
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment2 : Fragment(), CoroutineScope, TitleListener, InjectListener {

    open val job by lazy { SupervisorJob() }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job//ContextHelper.dispatcher + job

    abstract val layout: Int

    init {
        /**
         * This retains the state on config change and does not call oncreate again.
         * https://stackoverflow.com/a/18681837
         */
        retainInstance = true
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    /**
     * getBottomSheetBehavior
     * getToolbarTitleView
     * getNavigationView
     */
    fun getBottomSheetContainer(): ConstraintLayout? = act?.bottomSheetContainer

    fun getBottomSheetBehavior(): BottomSheetBehavior2<ConstraintLayout>? = act?.bottomSheetBehavior

    fun getToolbarTitleView(): AppCompatTextView? = act?.getToolbarTitleView()

    fun getNavigationView(): NavigationView =  act?.getNavigationView()!!

    fun getToolbar(): Toolbar? = act?.getToolbar()

    private val act
            get()= (activity as? MainActivity)

    /*
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getToolbar()?.let {
            for (a in it.menu.children)
                a.isVisible = true
        }
        getNavigationView().let {
            it.setCheckedItem(R.id.action_read)
            for (a in it.menu.children)
                a.isEnabled = true
        }
        getToolbarTitleView()?.onClick {
            onToolbarTitleClick()
        }
    }*/

    fun closeBottomSheet() = getBottomSheetBehavior()?.close()

    fun toggleBottomSheet() = getBottomSheetBehavior()?.toggle()

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

}
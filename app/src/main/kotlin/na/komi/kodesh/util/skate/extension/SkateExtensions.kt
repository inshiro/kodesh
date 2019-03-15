package na.komi.kodesh.util.skate.extension

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import na.komi.kodesh.util.skate.Skate
import na.komi.kodesh.util.skate.global.SkateSingleton
import na.komi.kodesh.util.skate.lifecycle.SkateLifecycleCallbacks

internal fun getLifecycle(savedInstanceState: Bundle?, skate: Skate) = SkateLifecycleCallbacks(object : Skate.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity?) {
        super.onActivityStarted(activity)
        //Logger info "onActivityStarted"

        activity?.run {
            if (!isChangingConfigurations)
                savedInstanceState?.getParcelableArrayList<Skate.SkateFragment>("LIST")?.let { d ->
                    skate.serializeList(d)
                    //Logger assert skate.stack.toString()
                }
        }

    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        super.onActivitySaveInstanceState(activity, outState)
        activity?.run {
            //Logger info  "onActivitySaveInstanceState"

            if (isChangingConfigurations) return

            val list = arrayListOf<Skate.SkateFragment>()
            list.addAll(skate.stack)

            outState?.putParcelableArrayList("LIST", list)
            savedInstanceState?.putParcelableArrayList("LIST", list)

        }
    }

    override fun onActivityDestroyed(activity: Activity?) {
        if (activity != null && activity.isFinishing) {
            //Logger info "OnDestroy AppCompatActivity"
            skate.clear()
        }
        super.onActivityDestroyed(activity)
    }
})

fun Activity.startSkating(savedInstanceState:Bundle?) :Skate  {
    val skate = SkateSingleton.getInstance()
    application.registerActivityLifecycleCallbacks(getLifecycle(savedInstanceState,skate))
    return skate
}


fun Fragment.startSkating(savedInstanceState:Bundle?) = lazy {
    val skate = SkateSingleton.getInstance()
    activity?.application?.registerActivityLifecycleCallbacks(getLifecycle(savedInstanceState,skate))
    skate
}

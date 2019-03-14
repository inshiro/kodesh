package na.komi.kodesh.util.skate.extension

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import na.komi.kodesh.BuildConfig
import na.komi.kodesh.util.skate.Skate
import na.komi.kodesh.util.skate.lifecycle.SkateLifecycleCallbacks
import na.komi.kodesh.util.skate.log.Logger
import na.komi.kodesh.util.skate.log.SkateLogger

fun Activity.startSkating(savedInstanceState: Bundle?): Skate {
    val skate = Skate()
    application.registerActivityLifecycleCallbacks(SkateLifecycleCallbacks(object : Skate.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity?) {
            super.onActivityStarted(activity)
            //Logger info "AppCompatActivity onActivityStarted"

            if (BuildConfig.DEBUG && Skate.logger == null)
                Skate.logger = SkateLogger

            activity?.run {
                if (!isChangingConfigurations)
                    savedInstanceState?.getParcelableArrayList<Skate.SkateFragment>("LIST")?.let { d ->
                        skate.serializeList(d)
                        Logger assert skate.stack.toString()
                    }
            }

        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            super.onActivitySaveInstanceState(activity, outState)
            activity?.run {
                //Logger info  "AppCompatActivity onActivitySaveInstanceState"

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
    }))
    return skate
}


fun Fragment.startSkating(savedInstanceState: Bundle?) = lazy {
    val skate = Skate()
    activity?.application?.registerActivityLifecycleCallbacks(SkateLifecycleCallbacks(object :
        Skate.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            super.onActivityCreated(activity, savedInstanceState)
            Logger debug "Fragment savedInstanceState null? ${savedInstanceState == null}"

        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            super.onActivitySaveInstanceState(activity, outState)
            activity?.let {
                //Logger debug "Fragment onActivitySaveInstanceState"
                //skate.onSaveInstanceState(outState)
                //Logger debug "$outState"
            }
        }

        override fun onActivityDestroyed(activity: Activity?) {
            if (activity != null && activity.isFinishing) {
                //Logger debug "OnDestroy Fragment"
                //skate.clear()
                // Logger debug (activity as AppCompatActivity).supportFragmentManager.fragments.toString()
            }
            super.onActivityDestroyed(activity)
        }
    }))
    skate
}

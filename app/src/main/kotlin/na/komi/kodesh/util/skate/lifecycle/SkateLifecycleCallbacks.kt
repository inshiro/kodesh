package na.komi.kodesh.util.skate.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import na.komi.kodesh.util.skate.Skate

class SkateLifecycleCallbacks(private val callback: Skate.ActivityLifecycleCallbacks) : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {
        callback.onActivityPaused(activity)
    }

    override fun onActivityResumed(activity: Activity?) {
        callback.onActivityResumed(activity)
    }

    override fun onActivityStarted(activity: Activity?) {
        callback.onActivityStarted(activity)
    }

    override fun onActivityDestroyed(activity: Activity?) {
        callback.onActivityDestroyed(activity)
        activity?.application?.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        callback.onActivitySaveInstanceState(activity, outState)
    }

    override fun onActivityStopped(activity: Activity?) {
        callback.onActivityStopped(activity)
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        callback.onActivityCreated(activity, savedInstanceState)
    }

}
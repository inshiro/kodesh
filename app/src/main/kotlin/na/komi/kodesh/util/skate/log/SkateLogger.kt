package na.komi.kodesh.util.skate.log

import android.util.Log
import na.komi.kodesh.util.skate.Skate


/**
 * implementation. for outside use.
 */
object SkateLogger : Skate.Logger {

    private const val TAG = "SKATE"

    override infix fun debug(msg: String) {
        Log.d(TAG, msg)
    }

    override infix fun info(msg: String) {
        Log.i(TAG, msg)
    }

    override fun warn(msg: String) {
        Log.w(TAG, msg)
    }

    override fun error(msg: String) {
        Log.e(TAG, msg)
    }

    override fun verbose(msg: String) {
        Log.v(TAG, msg)
    }

    override fun assert(msg: String) {
        Log.println(Log.ASSERT, TAG, msg)
    }
}

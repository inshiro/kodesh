package na.komi.kodesh.util.knavigator.log

import na.komi.kodesh.util.knavigator.Knavigator

/**
 * for internal use.
 */
internal object Logger {
    private const val TAG = "KNAVIGATOR"

    infix fun debug(msg: String) {
        Knavigator.logger?.debug(msg)
    }

    infix fun info(msg: String) {
        Knavigator.logger?.info(msg)
    }

    infix fun warn(msg: String) {
        Knavigator.logger?.warn(msg)
    }

    infix fun assert(msg: String) {
        Knavigator.logger?.assert(msg)
    }

    infix fun error(msg: String) {
        Knavigator.logger?.error(msg)
    }

    infix fun verbose(msg: String) {
        Knavigator.logger?.verbose(msg)
    }

}
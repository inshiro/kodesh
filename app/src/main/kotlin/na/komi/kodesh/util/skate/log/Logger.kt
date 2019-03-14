package na.komi.kodesh.util.skate.log

import na.komi.kodesh.util.skate.Skate

/**
 * for internal use.
 */
internal object Logger {

    infix fun debug(msg: String) {
        Skate.logger?.debug(msg)
    }

    infix fun info(msg: String) {
        Skate.logger?.info(msg)
    }

    infix fun warn(msg: String) {
        Skate.logger?.warn(msg)
    }

    infix fun assert(msg: String) {
        Skate.logger?.assert(msg)
    }

    infix fun error(msg: String) {
        Skate.logger?.error(msg)
    }

    infix fun verbose(msg: String) {
        Skate.logger?.verbose(msg)
    }

}
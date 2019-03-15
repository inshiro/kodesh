package na.komi.kodesh.util.skate.global

import na.komi.kodesh.util.skate.Skate
import java.util.Stack

internal object SkateSingleton {
    @Volatile
    internal var _instance: Skate? = null

    @Synchronized
    fun getInstance() = _instance ?: synchronized(Skate::class.java) { _instance ?: Skate().also { _instance = it } }

    @Suppress("unused")
    private fun readResolve() = getInstance()

    var _stack: Stack<Skate.SkateFragment>? = Stack()

    fun clear() {
        _instance = null
        _stack = null
    }

}
package na.komi.kodesh.util.skate.global

import na.komi.kodesh.util.skate.Skate
import java.util.Stack

internal object SkateSingleton {
    @Volatile
    private var _instance: Skate? = null

    @Synchronized
    fun getInstance() = _instance ?: synchronized(Skate::class.java) { _instance ?: Skate().also { _instance = it } }

    @Suppress("unused")
    private fun readResolve() = getInstance()

    fun readInstance() = _instance

    private var _stack: Stack<Skate.SkateFragment>?  = null
    val stack: Stack<Skate.SkateFragment>
        get() = _stack ?: Stack<Skate.SkateFragment>().also { _stack = it }

    private var _myStack: Stack<Class<*>>?  = null
    val myStack: Stack<Class<*>>
        get() = _myStack ?: Stack<Class<*>>().also { _myStack = it }


    private var _modeMap: MutableMap<Class<*>,Int>? = null
    val modes: MutableMap<Class<*>,Int>
        get() = _modeMap ?: mutableMapOf<Class<*>,Int>().also { _modeMap = it }

    fun clear() {
        _instance = null
        _stack = null
    }

}
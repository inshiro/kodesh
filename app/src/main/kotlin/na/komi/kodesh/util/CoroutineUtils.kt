package na.komi.kodesh.util

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.android.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

object ContextHelper : CoroutineScope {

    val looper = Looper.getMainLooper()

    val handler = Handler(looper)

    /**
     * Creating dispatcher from main handler to avoid IO
     * See https://github.com/Kotlin/kotlinx.coroutines/issues/878
     */
    val dispatcher = handler.asCoroutineDispatcher("kod-main")

    override val coroutineContext: CoroutineContext get() = dispatcher
}

object Coroutines {
    fun io(work: suspend (() -> Unit)): Job =
            CoroutineScope(Dispatchers.IO).launch {
                work()
            }

    fun <T : Any> ioThenMain(work: suspend (() -> T?), callback: ((T?) -> Unit)): Job =
            CoroutineScope(Dispatchers.Main).launch {
                val data = CoroutineScope(Dispatchers.IO).async rt@{
                    return@rt work()
                }.await()
                callback(data)
            }
    val cachedExecutor by lazy {  Executors.newCachedThreadPool()}
    val executorService by lazy {  Executors.newFixedThreadPool(100) }
    val coroutineDispatcher = executorService.asCoroutineDispatcher()
}

val UI by lazy { Dispatchers.Main }
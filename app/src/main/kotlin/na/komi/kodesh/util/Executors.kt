package na.komi.kodesh.util

import java.util.concurrent.Executors

val IO_EXECUTOR by lazy { Executors.newSingleThreadExecutor() }
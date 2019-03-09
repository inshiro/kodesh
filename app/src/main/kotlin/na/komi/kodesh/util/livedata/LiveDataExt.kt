package na.komi.kodesh.util.livedata

import android.os.Looper
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent

// Returns non-null LiveEvent
val <T> LiveData<T>.raw
    get() = this.value!!

// LiveEvent with initializer
// SingleLiveEvent
fun <T> LiveEvent(initialValue: T): LiveEvent<T> = LiveEvent<T>().apply {
    if (Looper.myLooper() == Looper.getMainLooper())
        setValue(initialValue)
    else
        postValue(initialValue)
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
    val result = LiveEvent<T>()
    result.addSource(this) {
        result.postValue(it)
    }
    return result
}
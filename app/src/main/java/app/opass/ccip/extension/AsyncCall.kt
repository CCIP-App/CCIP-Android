package app.opass.ccip.extension

import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Call<T>.asyncExecute() = suspendCancellableCoroutine<Response<T>> {
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            it.resumeWithException(t)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            it.resume(response)
        }
    })

    it.invokeOnCancellation { cancel() }
}

suspend fun okhttp3.Call.asyncExecute() = suspendCancellableCoroutine<okhttp3.Response> {
    enqueue(object : okhttp3.Callback {
        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            it.resume(response)
        }

        override fun onFailure(call: okhttp3.Call, e: IOException) {
            it.resumeWithException(e)
        }
    })

    it.invokeOnCancellation { cancel() }
}

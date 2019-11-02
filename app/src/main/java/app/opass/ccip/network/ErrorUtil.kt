package app.opass.ccip.network

import app.opass.ccip.model.Error
import app.opass.ccip.util.JsonUtil
import retrofit2.Response
import java.io.IOException

class ErrorUtil {
    companion object {
        fun parseError(response: Response<*>): Error {
            return try {
                val body = response.errorBody()?.string() ?: return Error()
                JsonUtil.fromJson(body, Error::class.java)
            } catch (e: IOException) {
                Error()
            }
        }
    }
}

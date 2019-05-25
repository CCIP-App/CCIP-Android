package app.opass.ccip.network

import app.opass.ccip.model.Error
import retrofit2.Response
import java.io.IOException

class ErrorUtil {
    companion object {
        fun parseError(response: Response<*>): Error {
            val converter = CCIPClient.retrofit.responseBodyConverter<Error>(Error::class.java, arrayOfNulls(0))

            return try {
                converter.convert(response.errorBody())!!
            } catch (e: IOException) {
                Error()
            }
        }
    }
}

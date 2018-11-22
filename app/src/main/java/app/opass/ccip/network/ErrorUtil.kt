package app.opass.ccip.network

import app.opass.ccip.model.Error
import retrofit2.Response
import java.io.IOException

object ErrorUtil {

    fun parseError(response: Response<*>): Error {
        val converter = CCIPClient.getRetrofit().responseBodyConverter<Error>(Error::class.java, arrayOfNulls(0))

        val error: Error

        try {
            error = converter.convert(response.errorBody())
        } catch (e: IOException) {
            return Error()
        }

        return error
    }
}

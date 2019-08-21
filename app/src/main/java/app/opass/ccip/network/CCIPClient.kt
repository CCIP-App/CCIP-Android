package app.opass.ccip.network

import app.opass.ccip.model.Announcement
import app.opass.ccip.model.Attendee
import app.opass.ccip.util.JsonUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class CCIPClient {
    companion object {
        private const val API_BASE_URL = "https://ccip.opass.app"

        var retrofit: Retrofit =
            Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(JsonUtil.GSON))
                .build()

        private var sCCIPService = retrofit.create(CCIPService::class.java)

        fun get(): CCIPService = sCCIPService

        fun setBaseUrl(baseUrl: String) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(JsonUtil.GSON))
                .build()
            sCCIPService = retrofit.create(CCIPService::class.java)
        }

        interface CCIPService {
            @GET("/status")
            fun status(
                @Query("token") token: String?
            ): Call<Attendee>

            @GET("/use/{scenario}")
            fun use(
                @Path("scenario") scenario: String,
                @Query("token") token: String?
            ): Call<Attendee>

            @GET("/announcement")
            fun announcement(): Call<List<Announcement>>
        }
    }
}

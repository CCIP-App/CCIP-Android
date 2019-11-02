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
        private val cache = mutableMapOf<String, CCIPService>()

        fun withBaseUrl(url: String): CCIPService {
            cache[url]?.let { return@withBaseUrl it }
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(JsonUtil.GSON))
                .build()
            val service = retrofit.create(CCIPService::class.java)
            cache[url] = service
            return service
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
            fun announcement(
                @Query("token") token: String?
            ): Call<List<Announcement>>
        }
    }
}

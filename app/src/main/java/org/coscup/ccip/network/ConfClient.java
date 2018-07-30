
package org.coscup.ccip.network;

import org.coscup.ccip.model.Submission;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ConfClient {

    public static final String API_BASE_URL = "https://coscup.org";

    private static Retrofit retrofit;
    private static ConfService sConfService;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static ConfService get() {
        if (sConfService == null) {
            sConfService = getRetrofit().create(ConfService.class);
        }

        return sConfService;
    }

    public interface ConfService {
        @GET("/2018-assets/json/submissions.json")
        Call<List<Submission>> submission();
    }
}

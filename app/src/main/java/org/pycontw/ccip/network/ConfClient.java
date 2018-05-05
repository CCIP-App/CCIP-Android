
package org.pycontw.ccip.network;

import org.pycontw.ccip.model.Submission;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ConfClient {

    public static final String API_BASE_URL = "http://sitcon.org/2018/static/";

    private static Retrofit retrofit;
    private static COSCUPService sCOSCUPService;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static COSCUPService get() {
        if (sCOSCUPService == null) {
            sCOSCUPService = getRetrofit().create(COSCUPService.class);
        }

        return sCOSCUPService;
    }

    public interface COSCUPService {
        @GET("json/submissions.json")
        Call<List<Submission>> submission();
    }
}

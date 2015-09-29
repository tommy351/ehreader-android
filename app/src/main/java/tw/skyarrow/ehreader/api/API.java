package tw.skyarrow.ehreader.api;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.model.GalleryId;
import tw.skyarrow.ehreader.model.GalleryIdTypeAdapter;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class API {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(GalleryId.class, new GalleryIdTypeAdapter())
            .create();

    private APIService service;

    public API(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .client(getOkHttpClient(context))
                .addConverter(String.class, new StringConverter())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

//        retrofit.client().interceptors().add(new APIInterceptor(context));

        this.service = retrofit.create(APIService.class);
    }

    public APIService getService() {
        return service;
    }

    public static APIService getService(Context context) {
        return new API(context).getService();
    }

    public OkHttpClient getOkHttpClient(Context context){
        OkHttpClient client = new OkHttpClient();

        return client;
    }

    public static Gson getGson() {
        return gson;
    }
}

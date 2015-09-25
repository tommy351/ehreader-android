package tw.skyarrow.ehreader.api;

import java.util.Map;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import tw.skyarrow.ehreader.model.GalleryDataRequest;
import tw.skyarrow.ehreader.model.GalleryDataResponse;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public interface APIService {
    @POST("api.php")
    Call<GalleryDataResponse> getGalleryData(@Body GalleryDataRequest body);

    @GET("/")
    Call<String> getIndex(@Query("page") int page);
}

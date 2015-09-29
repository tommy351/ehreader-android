package tw.skyarrow.ehreader.api;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Url;
import rx.Observable;
import tw.skyarrow.ehreader.model.GalleryDataRequest;
import tw.skyarrow.ehreader.model.GalleryDataResponse;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public interface APIService {
    @POST("api.php")
    Observable<GalleryDataResponse> getGalleryData(@Body GalleryDataRequest body);

    @GET
    Observable<String> getIndex(@Url String url, @Query("page") int page);

    @GET("g/{id}/{token}")
    Observable<String> getGalleryPage(@Path("id") long galleryId, @Path("token") String galleryToken, @Query("p") int page);

    @GET("s/{token}/{id}-{page}")
    Observable<String> getPhotoPage(@Path("id") long galleryId, @Path("token") String photoToken, @Path("page") int page);
}

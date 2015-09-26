package tw.skyarrow.ehreader.api;

import android.content.Context;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class APIInterceptor implements Interceptor {
    public APIInterceptor(Context context) {
        //
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}

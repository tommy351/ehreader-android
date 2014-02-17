package tw.skyarrow.ehreader.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class HttpRequestHelper {
    public static HttpResponse get(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);

        return response;
    }

    public static HttpResponse post(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = httpClient.execute(httpPost);

        return response;
    }

    public static String readResponse(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            return EntityUtils.toString(entity);
        } else {
            return null;
        }
    }

    public static String getString(String url) throws IOException {
        HttpResponse response = get(url);

        return readResponse(response);
    }

    public static String postString(String url) throws IOException {
        HttpResponse response = post(url);

        return readResponse(response);
    }
}

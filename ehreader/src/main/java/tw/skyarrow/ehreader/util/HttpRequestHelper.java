package tw.skyarrow.ehreader.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
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

package tw.skyarrow.ehreader.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class RequestHelper {
    private static RequestHelper mInstance;
    private RequestQueue mQueue;

    public RequestHelper(Context context){
        mQueue = Volley.newRequestQueue(context);
    }

    public static synchronized RequestHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new RequestHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    public static RequestQueue getRequestQueue(Context context){
        return getInstance(context).getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        return mQueue;
    }

    public void addToRequestQueue(Request request){
        getRequestQueue().add(request);
    }

    public void addToRequestQueue(Request request, Object tag){
        request.setTag(tag);
        addToRequestQueue(request);
    }

    public void cancelAllRequests(Object tag){
        getRequestQueue().cancelAll(tag);
    }

    public void cancelAllRequests(){
        getRequestQueue().cancelAll(new Filter() {
            @Override
            public boolean isLoggable(LogRecord logRecord) {
                return true;
            }
        });
    }
}

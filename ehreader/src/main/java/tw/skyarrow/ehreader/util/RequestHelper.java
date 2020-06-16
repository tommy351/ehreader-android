package tw.skyarrow.ehreader.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestHelper {
    private static RequestHelper mInstance;
    private RequestQueue mQueue;

    private RequestHelper(Context context){
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
        getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}

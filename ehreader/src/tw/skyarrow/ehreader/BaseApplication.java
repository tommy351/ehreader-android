package tw.skyarrow.ehreader;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class BaseApplication extends Application {
    public static final String TAG = "BaseApplication";

    private static BaseApplication mInstance;
    private RequestQueue mQueue;

    public static synchronized BaseApplication getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public RequestQueue getRequestQueue(){
        if (mQueue == null){
            synchronized (BaseApplication.class){
                if (mQueue == null) mQueue = Volley.newRequestQueue(getApplicationContext());
            }
        }

        return mQueue;
    }

    public void addToRequestQueue(Request request){
        if (request.getTag() == null){
            request.setTag(TAG);
        }

        getRequestQueue().add(request);
    }

    public void cancelAllRequests(Object tag){
        getRequestQueue().cancelAll(tag);
    }

    public void cancelAllRequests(){
        cancelAllRequests(TAG);
    }
}

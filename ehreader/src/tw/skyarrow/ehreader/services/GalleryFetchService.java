package tw.skyarrow.ehreader.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.events.FetchIndexEvent;
import tw.skyarrow.ehreader.models_old.Gallery;
import tw.skyarrow.ehreader.models_old.Tag;
import tw.skyarrow.ehreader.util.EHAPIRequest;
import tw.skyarrow.ehreader.util.L;

// TODO: use a normal Service instead. Because volley run in another thread and the IntentService will
// be terminated once the handleIntent is done
public class GalleryFetchService extends IntentService {
    public static final String TAG = "GalleryListFetchService";

    private static final String ACTION_FETCH_INDEX = "tw.skyarrow.ehreader.services.action.FETCH_INDEX";
    private static final String ACTION_FETCH_GALLERY = "tw.skyarrow.ehreader.services.action.FETCH_GALLERY";

    private static final String EXTRA_INDEX_URL = "EXTRA_INDEX_URL";
    private static final String EXTRA_GALLERY_ID = "EXTRA_GALLERY_ID";

    private static final String IPB_MEMBER_ID = "ipb_member_id";
    private static final String IPB_PASS_HASH = "ipb_pass_hash";
    private static final String IPB_SESSION_ID = "ipb_session_id";

    public static final Pattern pGalleryUrl = Pattern.compile("http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)");
    public static final Pattern pPhotoUrl = Pattern.compile("http://(g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    public static final Pattern pShowkey = Pattern.compile("var showkey.*=.*\"([\\w-]+?)\";");
    public static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"(.+)/(.+?)\"");
    public static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    private RequestQueue mQueue;

    public GalleryFetchService() {
        super(TAG);
    }

    public static void startFetchIndex(Context context, String url){
        Intent intent = new Intent(context, GalleryFetchService.class);
        intent.setAction(ACTION_FETCH_INDEX);
        intent.putExtra(EXTRA_INDEX_URL, url);
        context.startService(intent);
    }

    public static void startFetchGallery(Context context, int id){
        Intent intent = new Intent(context, GalleryFetchService.class);
        intent.setAction(ACTION_FETCH_GALLERY);
        intent.putExtra(EXTRA_GALLERY_ID, id);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        L.d("onDestroy");

        if (mQueue != null){
            mQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;

        final String action = intent.getAction();

        if (ACTION_FETCH_INDEX.equals(action)){
            final String url = intent.getStringExtra(EXTRA_INDEX_URL);
            handleFetchIndex(url);
        } else if (ACTION_FETCH_GALLERY.equals(action)){
            final int id = intent.getIntExtra(EXTRA_GALLERY_ID, 0);
            handleFetchGallery(id);
        }
    }

    private void postFetchIndexSuccessEvent(String url, List<Gallery> list){
        EventBus.getDefault().post(new FetchIndexEvent(FetchIndexEvent.EVENT_SUCCESS, url, list));
    }

    private void postFetchIndexFailedEvent(Exception e, String url){
        L.e(e);
        EventBus.getDefault().post(new FetchIndexEvent(FetchIndexEvent.EVENT_FAILED, url));
    }

    private void handleFetchIndex(final String url){
        L.d("handleFetchIndex: %s", url);

        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                L.d("handleFetchIndex success: %s", s);
                handleFetchIndexResponse(url, s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                L.e(volleyError.getMessage());
                postFetchIndexFailedEvent(volleyError, url);
            }
        });

        mQueue.add(req);
    }

    private void handleFetchGallery(int id){
        //
    }

    private void handleFetchIndexResponse(final String url, String s){
        L.d("handleFetchIndexResponse: %s", url);

        JSONArray gidlist = new JSONArray();
        Matcher matcher = pGalleryURL.matcher(s);

        while (matcher.find()){
            int id = Integer.parseInt(matcher.group(2));
            String token = matcher.group(3);
            JSONArray arr = new JSONArray();

            arr.put(id);
            arr.put(token);

            gidlist.put(arr);
        }

        if (gidlist.length() == 0){
            postFetchIndexSuccessEvent(url, null);
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("method", "gdata");
            obj.put("gidlist", gidlist);

            EHAPIRequest req = new EHAPIRequest(Constant.API_URL, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    handleGalleryListResponse(url, jsonObject);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    postFetchIndexFailedEvent(volleyError, url);
                }
            });

            mQueue.add(req);
        } catch (JSONException e){
            postFetchIndexFailedEvent(e, url);
        }
    }

    private void handleGalleryListResponse(String url, JSONObject json){
        L.d("handleGalleryListResponse: %s", url);

        Realm realm = null;

        try {
            JSONArray list = json.getJSONArray("gmetadata");
            List<Gallery> result = new ArrayList<>();
            realm = Realm.getInstance(this);
            realm.beginTransaction();

            for (int i = 0, len = list.length(); i < len; i++){
                JSONObject data = list.getJSONObject(i);
                int id = data.getInt("gid");

                if (data.has("error")){
                    // TODO: error handling
                    continue;
                }

                Gallery gallery = findGalleryById(realm, id);

                if (gallery == null){
                    gallery = realm.createObject(Gallery.class);

                    gallery.setId(id);
                    gallery.setStarred(false);
                    gallery.setProgress(0);
                }

                setGalleryFromJson(realm, gallery, json);
                result.add(realm.copyToRealmOrUpdate(gallery));
            }

            realm.commitTransaction();
            postFetchIndexSuccessEvent(url, result);
        } catch (JSONException e){
            postFetchIndexFailedEvent(e, url);
            if (realm != null) realm.cancelTransaction();
        } finally {
            if (realm != null) realm.close();
        }
    }

    private Gallery findGalleryById(Realm realm, int id){
        return realm.where(Gallery.class).equalTo("id", id).findFirst();
    }

    private void setGalleryFromJson(Realm realm, Gallery gallery, JSONObject data) throws JSONException {
        gallery.setToken(data.getString("token"));
        gallery.setTitle(data.getString("title"));
        gallery.setSubtitle(data.getString("title_jpn"));
        gallery.setCategory(data.getString("category"));
        gallery.setThumbnail(data.getString("thumb"));
        gallery.setCount(data.getInt("filecount"));
        gallery.setRating((float) data.getDouble("rating"));
        gallery.setUploader(data.getString("uploader"));
        gallery.setCreated(new Date(data.getLong("posted") * 1000));
        gallery.setSize(Long.parseLong(data.getString("filesize")));

        RealmList<Tag> tags = new RealmList<>();
        JSONArray tagArray = data.getJSONArray("tags");

        for (int i = 0, len = tagArray.length(); i < len; i++){
            Tag tag = realm.createObject(Tag.class);
            tag.setName(tagArray.getString(i));
            tags.add(realm.copyToRealmOrUpdate(tag));
        }

        gallery.setTags(tags);
    }
}

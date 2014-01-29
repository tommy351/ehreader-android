package tw.skyarrow.ehreader.util;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/27.
 */
public class GalleryAjaxCallback extends AjaxCallback<JSONObject> {
    public GalleryAjaxCallback() {
        url(Constant.API_URL);
        type(JSONObject.class);
        header("Accept", "application/json");
        header("Content-Type", "application/json");
    }

    public void setGalleryList(List<Gallery> list) {
        try {
            JSONArray gidlist = new JSONArray();

            for (Gallery item : list) {
                JSONArray arr = new JSONArray();

                arr.put(item.getId());
                arr.put(item.getToken());

                gidlist.put(arr);
            }

            JSONObject json = new JSONObject();

            json.put("method", "gdata");
            json.put("gidlist", gidlist);

            String jsonString = json.toString();
            StringEntity entity = new StringEntity(jsonString);

            param(AQuery.POST_ENTITY, entity);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

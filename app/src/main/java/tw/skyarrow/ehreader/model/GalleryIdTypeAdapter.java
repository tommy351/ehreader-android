package tw.skyarrow.ehreader.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class GalleryIdTypeAdapter implements JsonSerializer<GalleryId>, JsonDeserializer<GalleryId> {
    @Override
    public JsonElement serialize(GalleryId src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray element = new JsonArray();

        element.add(new JsonPrimitive(src.getId()));
        element.add(new JsonPrimitive(src.getToken()));

        return element;
    }

    @Override
    public GalleryId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray arr = json.getAsJsonArray();
        return new GalleryId(arr.get(0).getAsLong(), arr.get(1).getAsString());
    }
}

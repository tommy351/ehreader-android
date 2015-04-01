package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

public class BitmapHelper {
    public static Bitmap blur(Context context, Bitmap bitmap, float radius){
        Bitmap.Config config = bitmap.getConfig();

        if (config == Bitmap.Config.RGB_565){
            return blurRGB565(context, bitmap, radius);
        } else {
            return blurARGB8888(context, bitmap, radius);
        }
    }

    // https://gist.github.com/Mariuxtheone/903c35b4927c0df18cf8
    public static Bitmap blurARGB8888(Context context, Bitmap bitmap, float radius){
        Bitmap out = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = createRenderScript(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));;
        Allocation tmpIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, out);

        theIntrinsic.setRadius(radius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(out);
        // bitmap.recycle();
        rs.destroy();

        return out;
    }

    public static Bitmap blurRGB565(Context context, Bitmap bitmap, float radius){
        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        Bitmap out = blurARGB8888(context, bitmap8888, radius);

        return out;
    }

    public static RenderScript createRenderScript(Context context){
        return RenderScript.create(context);
    }
}

package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.Set;

public class PrefHelper {
    private Context mContext;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    private PrefHelper(Context context){
        mContext = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PrefHelper newInstance(Context context){
        return new PrefHelper(context);
    }

    private String getStringResource(int res){
        return mContext.getString(res);
    }

    public boolean getBoolean(int res, boolean def){
        return mPref.getBoolean(getStringResource(res), def);
    }

    public boolean getBoolean(int res){
        return getBoolean(res, false);
    }

    public float getFloat(int res, float def){
        return mPref.getFloat(getStringResource(res), def);
    }

    public float getFloat(int res){
        return getFloat(res, 0f);
    }

    public int getInt(int res, int def){
        return mPref.getInt(getStringResource(res), def);
    }

    public int getInt(int res){
        return getInt(res, 0);
    }

    public long getLong(int res, long def){
        return mPref.getLong(getStringResource(res), def);
    }

    public long getLong(int res){
        return getLong(res, 0);
    }

    public String getString(int res, String def){
        return mPref.getString(getStringResource(res), def);
    }

    public String getString(int res){
        return getString(res, "");
    }

    public Set<String> getStringSet(int res, Set<String> def){
        return mPref.getStringSet(getStringResource(res), def);
    }

    public Set<String> getStringSet(int res){
        return getStringSet(res, Collections.<String>emptySet());
    }

    private void checkEditor() {
        if (mEditor == null){
            throw new EditorException("You must call edit() before modifying preferences.");
        }
    }

    public PrefHelper edit(){
        if (mEditor == null){
            mEditor = mPref.edit();
        }

        return this;
    }

    public void commit(){
        checkEditor();
        mEditor.commit();
        mEditor = null;
    }

    public void apply(){
        checkEditor();
        mEditor.apply();
        mEditor = null;
    }

    public PrefHelper putBoolean(int res, boolean value){
        checkEditor();
        mEditor.putBoolean(getStringResource(res), value);
        return this;
    }

    public PrefHelper putFloat(int res, float value){
        checkEditor();
        mEditor.putFloat(getStringResource(res), value);
        return this;
    }

    public PrefHelper putInt(int res, int value){
        checkEditor();
        mEditor.putInt(getStringResource(res), value);
        return this;
    }

    public PrefHelper putLong(int res, long value){
        checkEditor();
        mEditor.putLong(getStringResource(res), value);
        return this;
    }

    public PrefHelper putString(int res, String value){
        checkEditor();
        mEditor.putString(getStringResource(res), value);
        return this;
    }

    public PrefHelper putStringSet(int res, Set<String> value){
        checkEditor();
        mEditor.putStringSet(getStringResource(res), value);
        return this;
    }

    public PrefHelper remove(int res){
        checkEditor();
        mEditor.remove(getStringResource(res));
        return this;
    }

    public static class EditorException extends RuntimeException {
        public EditorException(String detailMessage) {
            super(detailMessage);
        }

        public EditorException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public EditorException(Throwable throwable) {
            super(throwable);
        }
    }
}

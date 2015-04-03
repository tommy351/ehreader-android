package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.github.zafarkhaja.semver.Version;

public class UpdateHelper {
    private Context mContext;
    private String mVersionCode;
    private Version mVersion;

    public UpdateHelper(Context context){
        mContext = context;

        try {
            PackageManager pm = context.getPackageManager();
            mVersionCode = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            mVersion = Version.valueOf(mVersionCode);
        } catch (PackageManager.NameNotFoundException e){
            L.e(e);
        }
    }

    public String getVersionCode() {
        return mVersionCode;
    }

    public Version getVersion() {
        return mVersion;
    }
}

package tw.skyarrow.ehreader.event;

import com.github.zafarkhaja.semver.Version;

/**
 * Created by SkyArrow on 2014/2/21.
 */
public class UpdateCheckEvent {
    private int code;
    private Version version;

    public UpdateCheckEvent(int code, Version version) {
        this.code = code;
        this.version = version;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}

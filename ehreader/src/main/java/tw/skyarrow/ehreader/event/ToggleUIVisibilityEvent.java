package tw.skyarrow.ehreader.event;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class ToggleUIVisibilityEvent {
    private boolean visibility;

    public ToggleUIVisibilityEvent(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}

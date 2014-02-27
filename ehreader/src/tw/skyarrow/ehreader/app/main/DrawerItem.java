package tw.skyarrow.ehreader.app.main;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public class DrawerItem {
    private String name;
    private boolean selected;

    public DrawerItem(String name) {
        this(name, false);
    }

    public DrawerItem(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

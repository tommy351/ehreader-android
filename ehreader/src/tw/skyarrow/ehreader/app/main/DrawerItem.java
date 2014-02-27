package tw.skyarrow.ehreader.app.main;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public class DrawerItem {
    private String name;
    private boolean selected;
    private int icon;

    public DrawerItem(String name, int icon) {
        this(name, icon, false);
    }

    public DrawerItem(String name, int icon, boolean selected) {
        this.name = name;
        this.icon = icon;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

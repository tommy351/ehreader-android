package tw.skyarrow.ehreader.app.drawer;

public class MenuItem {
    private String title;
    private boolean selected;
    private int icon;
    private int selectedIcon;

    public MenuItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setIcon(int icon, int selectedIcon){
        setIcon(icon);
        setSelectedIcon(selectedIcon);
    }

    public int getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(int selectedIcon) {
        this.selectedIcon = selectedIcon;
    }
}

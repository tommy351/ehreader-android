package tw.skyarrow.ehreader.event;

public class SearchFilterEvent {
    private boolean[] chosenCategories;

    public SearchFilterEvent(boolean[] chosenCategories) {
        this.chosenCategories = chosenCategories;
    }

    public boolean[] getChosenCategories() {
        return chosenCategories;
    }

    public void setChosenCategories(boolean[] chosenCategories) {
        this.chosenCategories = chosenCategories;
    }
}

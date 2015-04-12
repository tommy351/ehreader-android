package tw.skyarrow.ehreader.app.search;

import android.app.SearchManager;
import android.database.Cursor;

public class SearchSuggestionItem {
    private String title;

    public SearchSuggestionItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static SearchSuggestionItem fromCursor(Cursor cursor){
        int index = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
        String title = cursor.getString(index);
        SearchSuggestionItem item = new SearchSuggestionItem(title);

        return item;
    }
}

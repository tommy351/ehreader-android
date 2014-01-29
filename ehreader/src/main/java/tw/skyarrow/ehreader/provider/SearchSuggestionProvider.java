package tw.skyarrow.ehreader.provider;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "tw.skyarrow.ehreader.provider.SearchSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

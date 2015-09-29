package tw.skyarrow.ehreader.provider;

import android.content.SearchRecentSuggestionsProvider;

import tw.skyarrow.ehreader.BuildConfig;

/**
 * Created by SkyArrow on 2015/9/28.
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.SearchSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}


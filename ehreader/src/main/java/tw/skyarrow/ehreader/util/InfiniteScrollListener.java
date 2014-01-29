package tw.skyarrow.ehreader.util;

import android.widget.AbsListView;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class InfiniteScrollListener implements AbsListView.OnScrollListener {
    private int threshold = 2;
    private int page = 0;
    private boolean isEnd = false;
    private boolean isLoading = false;
    private OnScrollToEndListener onScrollToEndListener;

    public interface OnScrollToEndListener {
        void onScrollToEnd(int page);
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setOnScrollToEndListener(OnScrollToEndListener onScrollToEndListener) {
        this.onScrollToEndListener = onScrollToEndListener;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (onScrollToEndListener != null && !isLoading && !isEnd && totalItemCount - visibleItemCount <= firstVisibleItem + threshold) {
            onScrollToEndListener.onScrollToEnd(++page);
        }
    }
}

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
    private OnScrollStateChangedListener onScrollStateChangedListener;

    public static final int SCROLL_STATE_FLING = AbsListView.OnScrollListener.SCROLL_STATE_FLING;
    public static final int SCROLL_STATE_IDLE = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    public static final int SCROLL_STATE_TOUCH_SCROLL = AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

    public interface OnScrollToEndListener {
        void onScrollToEnd(int page);
    }

    public interface OnScrollStateChangedListener {
        void onScrollStateChanged(int state);
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

    public void setOnScrollStateChangedListener(OnScrollStateChangedListener onScrollStateChangedListener) {
        this.onScrollStateChangedListener = onScrollStateChangedListener;
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
    public void onScrollStateChanged(AbsListView absListView, int state) {
        if (onScrollStateChangedListener != null) {
            onScrollStateChangedListener.onScrollStateChanged(state);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (onScrollToEndListener != null && !isLoading && !isEnd && totalItemCount - visibleItemCount <= firstVisibleItem + threshold) {
            onScrollToEndListener.onScrollToEnd(++page);
        }
    }
}

package tw.skyarrow.ehreader.view;

import android.support.v7.widget.RecyclerView;

/**
 * Created by SkyArrow on 2015/9/27.
 */
public class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private ScrollListener listener;

    public InfiniteScrollListener(ScrollListener listener) {
        this.listener = listener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (listener != null && recyclerView.canScrollVertically(1)){
            listener.onScrollToEnd();
        }
    }

    public interface ScrollListener {
        void onScrollToEnd();
    }
}

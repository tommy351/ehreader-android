package tw.skyarrow.ehreader.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager mLayoutManager;

    public RecyclerViewScrollListener() {
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }
}

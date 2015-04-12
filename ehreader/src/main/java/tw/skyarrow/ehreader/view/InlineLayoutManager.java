package tw.skyarrow.ehreader.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.LayoutParams;

public class InlineLayoutManager extends RecyclerView.LayoutManager {
    private Context mContext;
    private List<List<View>> mLines;

    public InlineLayoutManager(Context context) {
        mContext = context;
        mLines = new ArrayList<>();
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        mLines.clear();

        int width = getWidth();
        int height = getHeight();
        int paddingTop = getPaddingTop();
        int childCount = getChildCount();
        int lineWidth = 0;
        int lineHeight = 0;

        for (int i = 0; i < childCount; i++){
            View child = recycler.getViewForPosition(i);
        }
/*
        for (int i = 0; i < childCount; i++){
            View child = getChildAt(i);

            // Ignore gone views
            if (child.getVisibility() == View.GONE) continue;

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
            int childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;

            if (lineWidth + childWidth > width){
                //
            }

            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
        }*/
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}

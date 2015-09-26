package tw.skyarrow.ehreader.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by SkyArrow on 2015/9/25.
 */
public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    private View childView;
    private int childViewPosition;

    public RecyclerViewItemClickListener(Context context, OnItemClickListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        childView = rv.findChildViewUnder(e.getX(), e.getY());
        childViewPosition = rv.getChildAdapterPosition(childView);

        return childView != null && gestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongPress(View view, int position);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (childView != null && listener != null) {
                listener.onItemClick(childView, childViewPosition);
                return true;
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (childView != null && listener != null) {
                listener.onItemLongPress(childView, childViewPosition);
            }
        }
    }
}

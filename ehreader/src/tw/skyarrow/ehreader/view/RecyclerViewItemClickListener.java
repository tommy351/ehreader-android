package tw.skyarrow.ehreader.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

// https://gist.github.com/lnikkila/d9493a0626e89059c6aa
public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;

    @Nullable
    private View childView;
    private int childViewPosition;

    public RecyclerViewItemClickListener(Context context, OnItemClickListener listener){
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent event) {
        childView = view.findChildViewUnder(event.getX(), event.getY());
        childViewPosition = view.getChildAdapterPosition(childView);

        return childView != null && mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    public static interface OnItemClickListener {
        public void onItemClick(View childView, int position);
        public void onItemLongPress(View childView, int position);
    }

    public static abstract class SimpleOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(View childView, int position) {
            //
        }

        @Override
        public void onItemLongPress(View childView, int position) {
            //
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (childView != null) {
                mListener.onItemClick(childView, childViewPosition);
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            if (childView != null) {
                mListener.onItemLongPress(childView, childViewPosition);
            }
        }

        @Override
        public boolean onDown(MotionEvent event) {
            // Best practice to always return true here.
            // http://developer.android.com/training/gestures/detector.html#detect
            return true;
        }
    }
}

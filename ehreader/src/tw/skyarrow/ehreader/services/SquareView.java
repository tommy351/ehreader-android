package tw.skyarrow.ehreader.services;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareView extends LinearLayout {
    public SquareView(Context context) {
        this(context, null);
    }

    public SquareView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);

        setMeasuredDimension(size, size);
    }
}

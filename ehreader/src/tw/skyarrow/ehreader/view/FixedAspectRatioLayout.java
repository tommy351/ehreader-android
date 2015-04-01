package tw.skyarrow.ehreader.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import tw.skyarrow.ehreader.R;

// http://stackoverflow.com/a/10772572
public class FixedAspectRatioLayout extends RelativeLayout {
    private static final int DEFAULT_ASPECT_RATIO_WIDTH = 16;
    private static final int DEFAULT_ASPECT_RATIO_HEIGHT = 9;

    private int mAspectRatioWidth;
    private int mAspectRatioHeight;

    public FixedAspectRatioLayout(Context context) {
        this(context, null);
    }

    public FixedAspectRatioLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedAspectRatioLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioLayout);
        mAspectRatioWidth = arr.getInt(R.styleable.FixedAspectRatioLayout_aspectRatioWidth, DEFAULT_ASPECT_RATIO_WIDTH);
        mAspectRatioHeight = arr.getInt(R.styleable.FixedAspectRatioLayout_aspectRatioHeight, DEFAULT_ASPECT_RATIO_HEIGHT);

        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int receivedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int receivedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth;
        int measuredHeight;
        boolean widthDynamic;

        if (heightMode == MeasureSpec.EXACTLY) {
            if (widthMode == MeasureSpec.EXACTLY) {
                widthDynamic = receivedWidth == 0;
            } else {
                widthDynamic = true;
            }
        } else if (widthMode == MeasureSpec.EXACTLY) {
            widthDynamic = false;
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if (widthDynamic) {
            // Width is dynamic.
            int w = receivedHeight * mAspectRatioWidth / mAspectRatioHeight;
            measuredWidth = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
            measuredHeight = heightMeasureSpec;
        } else {
            // Height is dynamic.
            measuredWidth = widthMeasureSpec;
            int h = receivedWidth * mAspectRatioHeight / mAspectRatioWidth;
            measuredHeight = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
        }

        super.onMeasure(measuredWidth, measuredHeight);
    }
}

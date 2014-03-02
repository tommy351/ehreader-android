package tw.skyarrow.ehreader.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class CoverActionBarButton extends LinearLayout {
    private static Typeface typeface;

    public CoverActionBarButton(Context context) {
        this(context, null);
    }

    public CoverActionBarButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverActionBarButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setBackgroundResource(R.drawable.cover_actionbar_btn_bg);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        TextView textView = (TextView) getChildAt(1);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.margin_small), 0, 0, 0);
        textView.setTextColor(getResources().getColor(R.color.cover_actionbar_text_color));
        textView.setLayoutParams(layoutParams);

        super.onLayout(changed, l, t, r, b);
    }
}

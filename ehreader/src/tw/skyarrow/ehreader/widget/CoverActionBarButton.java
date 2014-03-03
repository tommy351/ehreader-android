package tw.skyarrow.ehreader.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class CoverActionBarButton extends LinearLayout {
    private static Typeface typeface;

    private TextView titleView;
    private ImageView iconView;

    public CoverActionBarButton(Context context) {
        this(context, null);
    }

    public CoverActionBarButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverActionBarButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CoverActionBarButton);
        Drawable icon = array.getDrawable(R.styleable.CoverActionBarButton_android_icon);
        String title = array.getString(R.styleable.CoverActionBarButton_android_title);
        titleView = new TextView(context);
        iconView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        if (typeface == null) {
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
        }

        layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.margin_small), 0, 0, 0);
        titleView.setTextColor(getResources().getColor(R.color.cover_actionbar_text_color));
        titleView.setTypeface(typeface);
        titleView.setLayoutParams(layoutParams);

        setGravity(Gravity.CENTER);
        setBackgroundResource(R.drawable.cover_actionbar_btn_bg);
        addView(iconView);
        addView(titleView);

        setIcon(icon);
        setTitle(title);
    }

    public void setIcon(int res) {
        setIcon(getContext().getResources().getDrawable(res));
    }

    public void setIcon(Drawable drawable) {
        getIconView().setImageDrawable(drawable);
    }

    public void setTitle(String title) {
        getTitleView().setText(title);
    }

    public ImageView getIconView() {
        return iconView;
    }

    public TextView getTitleView() {
        return titleView;
    }
}

package tw.skyarrow.ehreader.app.drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.view.RecyclerViewAdapter;

public class BaseMenuAdapter extends RecyclerViewAdapter {
    private Context mContext;
    private List<MenuItem> menuItems;

    public BaseMenuAdapter(Context context, List<MenuItem> items){
        mContext = context;
        menuItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.drawer_row, parent, false);

        return new ContentViewHolder(view);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        ContentViewHolder vh = (ContentViewHolder) holder;

        vh.titleView.setText(item.getTitle());
        int icon = item.getIcon();

        if (item.isSelected()){
            vh.titleView.setTextColor(mContext.getResources().getColor(R.color.accent));

            if (item.getSelectedIcon() != 0){
                icon = item.getSelectedIcon();
            }
        } else {
            vh.titleView.setTextColor(mContext.getResources().getColor(R.color.primary_text));
        }

        if (icon != 0){
            vh.iconView.setVisibility(View.VISIBLE);
            vh.iconView.setImageResource(icon);
        } else {
            vh.iconView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getContentItemCount() {
        return menuItems.size();
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.icon)
        public ImageView iconView;

        @InjectView(R.id.title)
        public TextView titleView;

        public ContentViewHolder(View view){
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

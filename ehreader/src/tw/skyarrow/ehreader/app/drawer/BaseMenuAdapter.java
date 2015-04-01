package tw.skyarrow.ehreader.app.drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.view.RecyclerViewAdapter;

public class BaseMenuAdapter extends RecyclerViewAdapter {
    private MenuItem[] menuItems;

    public BaseMenuAdapter(MenuItem[] items){
        menuItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.drawer_row, parent, false);

        return new ContentViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        MenuItem item = menuItems[position];
        ContentViewHolder vh = (ContentViewHolder) holder;

        vh.titleView.setText(item.getTitle());

        if (item.getIcon() == 0){
            vh.iconView.setVisibility(View.GONE);
        } else {
            vh.iconView.setImageResource(item.getIcon());
        }
    }

    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public int getContentItemCount() {
        return menuItems.length;
    }

    @Override
    public int getFooterItemCount() {
        return 0;
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

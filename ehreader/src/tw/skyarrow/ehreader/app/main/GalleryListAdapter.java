package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder> {
    private List<Gallery> mGalleryList;
    private Context mContext;

    public GalleryListAdapter(Context context){
        mContext = context;
    }

    public GalleryListAdapter(Context context, List<Gallery> galleryList){
        mContext = context;
        mGalleryList = galleryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_list_item, parent, false);

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mGalleryList == null) return;

        Gallery data = mGalleryList.get(position);

        holder.coverView.setImageUrl(data.getThumbnail(), ImageLoaderHelper.getImageLoader(mContext));
        holder.titleView.setText(data.getTitle());
        holder.categoryView.setText(mContext.getResources().getString(data.getCategoryString()));
        holder.categoryView.setTextColor(mContext.getResources().getColor(data.getCategoryColor()));
    }

    @Override
    public long getItemId(int position) {
        if (mGalleryList == null) return 0;

        Gallery data = mGalleryList.get(position);

        return data == null ? 0 : data.getId();
    }

    @Override
    public int getItemCount() {
        return mGalleryList == null ? 0 : mGalleryList.size();
    }

    public void setGalleryList(List<Gallery> galleryList){
        mGalleryList = galleryList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.cover)
        public NetworkImageView coverView;

        @InjectView(R.id.title)
        public TextView titleView;

        @InjectView(R.id.category)
        public TextView categoryView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

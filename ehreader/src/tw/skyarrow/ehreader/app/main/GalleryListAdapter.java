package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.content.res.Resources;
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
import tw.skyarrow.ehreader.view.FixedAspectRatioLayout;

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder> {
    private List<Gallery> mGalleryList;
    private Context mContext;

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
        Gallery data = mGalleryList.get(position);
        Resources res = mContext.getResources();
        String categoryString = res.getString(data.getCategoryString());
        int categoryColor = res.getColor(data.getCategoryColor());

        holder.titleView.setText(data.getTitle());
        holder.categoryView.setText(categoryString);
        holder.categoryView.setTextColor(categoryColor);
        holder.countView.setText(String.format("%dP", data.getCount()));

        // Set the size of ImageView
        int[] thumbnailSize = data.getThumbnailSize();
        holder.coverContainer.setAspectRatioWidth(thumbnailSize[0]);
        holder.coverContainer.setAspectRatioHeight(thumbnailSize[1]);
        holder.coverView.setImageUrl(data.getThumbnail(), ImageLoaderHelper.getImageLoader(mContext));
    }

    @Override
    public long getItemId(int position) {
        Gallery data = mGalleryList.get(position);
        return data.getId();
    }

    @Override
    public int getItemCount() {
        return  mGalleryList.size();
    }

    public void setGalleryList(List<Gallery> galleryList){
        mGalleryList = galleryList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.cover)
        public NetworkImageView coverView;

        @InjectView(R.id.cover_container)
        public FixedAspectRatioLayout coverContainer;

        @InjectView(R.id.title)
        public TextView titleView;

        @InjectView(R.id.category)
        public TextView categoryView;

        @InjectView(R.id.count)
        public TextView countView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

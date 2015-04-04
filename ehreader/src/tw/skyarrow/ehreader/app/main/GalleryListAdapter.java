package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.view.FixedAspectRatioLayout;
import tw.skyarrow.ehreader.view.RecyclerViewAdapter;

public class GalleryListAdapter extends RecyclerViewAdapter {
    private List<Gallery> mGalleryList;
    private Context mContext;

    public GalleryListAdapter(Context context, List<Gallery> galleryList){
        mContext = context;
        mGalleryList = galleryList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_list_item, parent, false);

        return new ContentViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_list_footer, parent, false);

        return new FooterViewHolder(view);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentViewHolder vh = (ContentViewHolder) holder;
        Gallery data = mGalleryList.get(position);
        Resources res = mContext.getResources();
        String categoryString = res.getString(data.getCategoryString());
        int categoryColor = res.getColor(data.getCategoryColor());

        vh.titleView.setText(data.getTitle());
        vh.categoryView.setText(categoryString);
        vh.categoryView.setTextColor(categoryColor);
        vh.countView.setText(String.format("%dP", data.getCount()));

        // Set the size of ImageView
        int[] thumbnailSize = data.getThumbnailSize();
        vh.coverContainer.setAspectRatioWidth(thumbnailSize[0]);
        vh.coverContainer.setAspectRatioHeight(thumbnailSize[1]);
        vh.coverView.setImageUrl(data.getThumbnail(), ImageLoaderHelper.getImageLoader(mContext));
    }

    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {
        FooterViewHolder vh = (FooterViewHolder) holder;
    }

    @Override
    public long getContentItemId(int position) {
        Gallery data = mGalleryList.get(position);
        return data.getId();
    }

    @Override
    public int getContentItemCount() {
        return mGalleryList.size();
    }

    @Override
    public int getFooterItemCount() {
        return 1;
    }

    public void setGalleryList(List<Gallery> galleryList){
        mGalleryList = galleryList;
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
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

        public ContentViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.loading)
        public ProgressBar loadingView;

        public FooterViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

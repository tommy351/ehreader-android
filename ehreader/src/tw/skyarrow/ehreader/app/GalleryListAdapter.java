package tw.skyarrow.ehreader.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.models_old.Gallery;

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder> {
    private List<Gallery> mGalleryList;

    public GalleryListAdapter(List<Gallery> galleryList){
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

        holder.titleView.setText(data.getTitle());
        holder.ratingBar.setRating(data.getRating());
    }

    @Override
    public long getItemId(int position) {
        Gallery data = mGalleryList.get(position);

        return data.getId();
    }

    @Override
    public int getItemCount() {
        return mGalleryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverView;
        public TextView titleView;
        public RatingBar ratingBar;
        public TextView metaView;

        public ViewHolder(View view) {
            super(view);

            coverView = (ImageView) view.findViewById(R.id.cover);
            titleView = (TextView) view.findViewById(R.id.title);
            ratingBar = (RatingBar) view.findViewById(R.id.rating);
            metaView = (TextView) view.findViewById(R.id.meta);
        }
    }
}

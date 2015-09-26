package tw.skyarrow.ehreader.app.photo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.service.PhotoFetchService;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {
    private Context context;
    private Gallery gallery;
    private Map<Integer, Photo> photoMap;

    public PhotoListAdapter(Context context, Gallery gallery, Map<Integer, Photo> photoMap) {
        this.context = context;
        this.gallery = gallery;
        this.photoMap = photoMap;
    }

    @Override
    public int getItemCount() {
        return gallery.getCount();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int photoPage = position + 1;
        Intent intent = PhotoFetchService.intent(context, gallery.getId(), photoPage);

        holder.pageNumber.setText(String.format("%d", photoPage));

        if (photoMap.containsKey(photoPage)){
            Photo photo = photoMap.get(photoPage);

            if (!photo.shouldReload()){
                holder.photo.setAspectRatio((float) photo.getWidth() / photo.getHeight());
                holder.photo.setImageURI(Uri.parse(photo.getSrc()));
                return;
            }
        }

        holder.photo.setAspectRatio(1f);
        context.startService(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.page_number)
        TextView pageNumber;

        @InjectView(R.id.photo)
        SimpleDraweeView photo;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}

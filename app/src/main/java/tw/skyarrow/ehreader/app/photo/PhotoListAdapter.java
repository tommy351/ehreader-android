package tw.skyarrow.ehreader.app.photo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.service.PhotoFetchService;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RxBus;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {
    private Context context;
    private Gallery gallery;
    private Map<Integer, Photo> photoMap;
    private RxBus<ImageLoadEvent> eventBus = new RxBus<>();

    public PhotoListAdapter(Context context, Gallery gallery, Map<Integer, Photo> photoMap) {
        this.context = context;
        this.gallery = gallery;
        this.photoMap = photoMap;
    }

    public Observable<ImageLoadEvent> getEventBus(){
        return eventBus.tObservable();
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
            final Photo photo = photoMap.get(photoPage);

            if (photo.getDownloaded() != null && photo.getDownloaded()){
                File file = photo.getFile(context);
                String path = file.getAbsolutePath();

                L.d("Load photo from file: %s", path);
                holder.photo.setAspectRatio((float) photo.getWidth() / photo.getHeight());
                holder.photo.setImageURI(Uri.parse("file://" + path));
                return;
            }

            if (!photo.shouldReload()){
                L.d("Photo load started: %s", photo.getSrc());

                ControllerListener listener = new BaseControllerListener<ImageInfo>(){
                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        L.d("Photo load failed: %s", photo.getSrc());
                        eventBus.send(new ImageLoadEvent(photo, throwable));
                    }
                };

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(photo.getSrc()))
                        .setOldController(holder.photo.getController())
                        .setControllerListener(listener)
                        .build();

                holder.photo.setAspectRatio((float) photo.getWidth() / photo.getHeight());
                holder.photo.setController(controller);
                return;
            }
        }

        L.d("Call PhotoFetchService to load photo: %d", photoPage);

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

    public static class ImageLoadEvent {
        public final Throwable error;
        public final Photo photo;

        public ImageLoadEvent(Photo photo, Throwable error) {
            this.error = error;
            this.photo = photo;
        }
    }
}

package tw.skyarrow.ehreader.app.photo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.PhotoListAdapterEvent;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.util.L;

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {
    public static final String TAG = PhotoListAdapter.class.getSimpleName();

    private Context mContext;
    private List<Photo> mPhotoList;
    private ImageLoader imageLoader;

    public PhotoListAdapter(Context context, List<Photo> photoList){
        mContext = context;
        mPhotoList = photoList;
        imageLoader = ImageLoaderHelper.getImageLoader(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_page, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, int position) {
        Photo photo = mPhotoList.get(position);
        int page = photo.getPage();

        ViewGroup.LayoutParams params = vh.imageView.getLayoutParams();
        DisplayMetrics metrics = getScreenMetrics();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;

        if (photo.getSrc() != null && !photo.getSrc().isEmpty()){
            if (vh.imageContainer != null){
                vh.imageContainer.cancelRequest();
                vh.imageContainer = null;
            }

            if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
                params.width = metrics.widthPixels;
                params.height = metrics.widthPixels * photo.getHeight() / photo.getWidth();
            }

            L.d("Loading photo: %s", photo.getSrc());

            vh.imageContainer = imageLoader.get(photo.getSrc(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    vh.imageView.setImageBitmap(imageContainer.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    // TODO: error handling
                    L.e(volleyError);
                }
            }, params.width, params.height);
        } else {
            PhotoListAdapterEvent event = new PhotoListAdapterEvent(PhotoListAdapterEvent.ACTION_NEED_SRC, position);
            EventBus.getDefault().post(event);
        }

        vh.imageView.setLayoutParams(params);
        vh.imageView.setImageBitmap(null);
        vh.numberView.setText(Integer.toString(page));
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        if (holder.imageContainer != null){
            holder.imageContainer.cancelRequest();
            holder.imageContainer = null;
        }

        if (holder.imageView != null && holder.imageView.getDrawable() != null){
            Bitmap bitmap = ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap();
            if (bitmap != null) bitmap.recycle();
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    public void setPhotoList(List<Photo> photoList){
        mPhotoList = photoList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        ImageView imageView;

        @InjectView(R.id.number)
        TextView numberView;

        ImageLoader.ImageContainer imageContainer;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    private int getScreenOrientation(){
        return mContext.getResources().getConfiguration().orientation;
    }

    private DisplayMetrics getScreenMetrics(){
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics;
    }

    private int getScreenWidth(){
        return getScreenMetrics().widthPixels;
    }

    private int getScreenHeight(){
        return getScreenMetrics().heightPixels;
    }
}

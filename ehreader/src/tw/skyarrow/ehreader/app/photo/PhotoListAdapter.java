package tw.skyarrow.ehreader.app.photo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.GalleryHelper;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.model.PhotoHelper;
import tw.skyarrow.ehreader.model.PhotoPageData;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RequestHelper;

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {
    public static final String TAG = PhotoListAdapter.class.getSimpleName();

    private Context mContext;
    private Gallery mGallery;
    private List<Photo> mPhotoList;
    private List<Integer> mPhotoListReqQueue;
    private List<Integer> mPhotoPageReqQueue;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;

    public PhotoListAdapter(Context context, Gallery gallery, List<Photo> photoList){
        mContext = context;
        mGallery = gallery;
        mPhotoList = photoList;
        mPhotoListReqQueue = new ArrayList<>();
        mPhotoPageReqQueue = new ArrayList<>();

        // Set up database
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase(mContext);
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_page, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Photo photo = mPhotoList.get(position);
        int page = photo.getPage();

        ViewGroup.LayoutParams params = vh.imageView.getLayoutParams();
        DisplayMetrics metrics = getScreenMetrics();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;

        if (photo.getId() == null){
            loadPhotoList(page);
        } else {
            if (photo.getSrc() == null || photo.getSrc().isEmpty()){
                loadPhotoSrc(photo);
            } else {
                loadPhoto(vh, photo);
            }
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

    public void cancelAllRequests(){
        RequestHelper.getInstance(mContext).cancelAllRequests(TAG);
    }

    private void loadPhotoList(int page){
        int galleryPage = page / mGallery.getPhotoPerPage();

        // Don't send request if the same page is in the queue
        if (mPhotoListReqQueue.contains(galleryPage)) return;

        String url = mGallery.getURL(galleryPage);
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                handlePhotoListResponse(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO: error handling
                L.e(volleyError);
            }
        });

        mPhotoListReqQueue.add(galleryPage);
        addRequestToQueue(req);
    }

    private void addRequestToQueue(Request req){
        RequestHelper.getInstance(mContext).addToRequestQueue(req, TAG);
    }

    private void handlePhotoListResponse(String html){
        List<Photo> list = GalleryHelper.findPhotosInGallery(html);

        // TODO: error handling
        if (list.size() == 0) return;

        Photo firstPhoto = list.get(0);
        int firstPage = firstPhoto.getPage();
        int galleryPage = firstPage / mGallery.getPhotoPerPage();

        photoDao.insertInTx(list);

        for (Photo photo : list){
            mPhotoList.set(photo.getPage() - 1, photo);
        }

        mPhotoListReqQueue.remove(galleryPage);
        notifyItemRangeChanged(firstPage - 1, list.size());
    }

    private void loadPhotoSrc(final Photo photo){
        // Don't send request if the same page is in the queue
        if (mPhotoPageReqQueue.contains(photo.getPage())) return;

        String url = photo.getURL();
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                handlePhotoPageResponse(photo, s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO: error handling
                L.e(volleyError);
            }
        });

        mPhotoPageReqQueue.add(photo.getPage());
        addRequestToQueue(req);
    }

    private void handlePhotoPageResponse(Photo photo, String html){
        PhotoPageData data = PhotoHelper.readPhotoPage(html);

        photo.setRetryId(data.getRetryId());
        photo.setSrc(data.getSrc());
        photo.setWidth(data.getWidth());
        photo.setHeight(data.getHeight());
        photoDao.updateInTx(photo);

        mGallery.setShowkey(data.getShowkey());
        galleryDao.updateInTx(mGallery);

        notifyItemChanged(photo.getPage() - 1);
    }

    private void loadPhoto(final ViewHolder vh, final Photo photo){
        String src = photo.getSrc();

        if (vh.imageContainer != null){
            if (vh.imageContainer.getRequestUrl().equals(src)){
                return;
            }

            vh.imageContainer.cancelRequest();
            vh.imageContainer = null;
        }

        DisplayMetrics metrics = getScreenMetrics();
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;

        ImageLoader imageLoader = ImageLoaderHelper.getImageLoader(mContext);
        vh.imageContainer = imageLoader.get(src, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
                    ViewGroup.LayoutParams params = vh.imageView.getLayoutParams();
                    params.width = screenWidth;
                    params.height = screenWidth * photo.getHeight() / photo.getWidth();

                    vh.imageView.setLayoutParams(params);
                }

                vh.imageView.setImageBitmap(imageContainer.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO: error handling
                L.e(volleyError);
            }
        }, screenWidth, screenHeight);
    }
}

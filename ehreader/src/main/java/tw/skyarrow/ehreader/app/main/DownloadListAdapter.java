package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;

public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {
    private Context mContext;
    private List<Download> mDownloadList;
    private Map<Long, Gallery> mGalleryList;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;

    public DownloadListAdapter(Context context, List<Download> downloadList){
        mContext = context;
        mDownloadList = downloadList;

        mGalleryList = new HashMap<>();
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.open();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
    }
    
    @Override
    public int getItemCount() {
        return mDownloadList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Download download = mDownloadList.get(position);
        Gallery gallery = getGallery(download.getId());

        holder.coverView.setImageUrl(gallery.getThumbnail(), ImageLoaderHelper.getImageLoader(mContext));
        holder.titleView.setText(gallery.getTitle());

        switch (download.getStatus()){
            case Download.STATUS_PENDING:
                holder.progressBar.setIndeterminate(true);
                holder.statusView.setText("Pending");
                break;

            case Download.STATUS_DOWNLOADING:
                holder.progressBar.setIndeterminate(false);
                holder.progressBar.setMax(gallery.getCount());
                holder.progressBar.setProgress(download.getProgress());
                holder.statusView.setText(String.format("Downloading (%d / %d)", download.getProgress(), gallery.getCount()));
                break;

            case Download.STATUS_SUCCESS:
                holder.progressBar.setIndeterminate(false);
                holder.statusView.setText("Success");
                break;

            case Download.STATUS_ERROR:
                holder.progressBar.setIndeterminate(false);
                holder.statusView.setText("Error");
                break;

            case Download.STATUS_PAUSED:
                holder.progressBar.setIndeterminate(false);
                holder.statusView.setText("Paused");
                break;
        }
    }

    private Gallery getGallery(long id){
        if (mGalleryList.containsKey(id)){
            return mGalleryList.get(id);
        } else {
            Gallery gallery = galleryDao.load(id);
            mGalleryList.put(id, gallery);
            return gallery;
        }
    }

    @Override
    public long getItemId(int position) {
        return mDownloadList.get(position).getId();
    }

    public void close(){
        dbHelper.close();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.cover)
        NetworkImageView coverView;

        @InjectView(R.id.title)
        TextView titleView;

        @InjectView(R.id.progress)
        ProgressBar progressBar;

        @InjectView(R.id.status)
        TextView statusView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

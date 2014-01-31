package tw.skyarrow.ehreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.GalleryDownload;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class DownloadListAdapter extends BaseAdapter {
    private static final boolean MEM_CACHE = true;
    private static final boolean FILE_CACHE = true;

    private Context context;
    private LayoutInflater inflater;
    private List<GalleryDownload> list;

    public DownloadListAdapter(Context context, List<GalleryDownload> list) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public GalleryDownload getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        GalleryDownload galleryDownload = getItem(i);
        Gallery gallery = galleryDownload.getGallery();

        if (view == null) {
            view = inflater.inflate(R.layout.download_list_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        AQuery aq = new AQuery(view);
        int progress = galleryDownload.getDownloadProgress();
        int total = gallery.getCount();

        holder.title.setText(gallery.getTitle());
        aq.id(holder.cover).image(gallery.getThumbnail(), MEM_CACHE, FILE_CACHE);
        holder.progressBar.setMax(total);
        holder.progressBar.setProgress(progress);

        switch (gallery.getDownloadStatus()) {
            case GalleryDownloadService.STATUS_DOWNLOADING:
                holder.progressText.setText(String.format("%d / %d (%.2f%%)", progress, total,
                        progress * 100f / total));
                break;

            case GalleryDownloadService.STATUS_PAUSED:
                holder.progressText.setText("Paused");
                break;

            case GalleryDownloadService.STATUS_ERROR:
                holder.progressText.setText("Error");
                break;

            case GalleryDownloadService.STATUS_SUCCESS:
                holder.progressText.setText("Download success");
                break;
        }

        return view;
    }

    static final class ViewHolder {
        @InjectView(R.id.cover)
        ImageView cover;

        @InjectView(R.id.title)
        TextView title;

        @InjectView(R.id.progress_bar)
        ProgressBar progressBar;

        @InjectView(R.id.progress_text)
        TextView progressText;

        @InjectView(R.id.feature)
        ImageButton featureBtn;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

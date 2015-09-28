package tw.skyarrow.ehreader.app.entry;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.model.Gallery;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {
    private Context context;
    private List<Download> list;

    public DownloadListAdapter(Context context, List<Download> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Download download = list.get(position);
        Gallery gallery = download.getGallery();

        holder.cover.setImageURI(Uri.parse(gallery.getThumbnail()));
        holder.title.setText(gallery.getTitle());
        holder.progressBar.setIndeterminate(false);
        holder.progressBar.setMax(0);
        holder.progressBar.setProgress(0);

        switch (download.getStatus()){
            case Download.STATUS_DOWNLOADING:
                holder.status.setText(R.string.download_in_progress);
                holder.progressBar.setMax(gallery.getCount());
                holder.progressBar.setProgress(download.getProgress());
                break;

            case Download.STATUS_PENDING:
                holder.status.setText(R.string.download_pending);
                holder.progressBar.setIndeterminate(true);
                break;

            case Download.STATUS_SUCCESS:
                holder.status.setText(R.string.download_success);
                break;

            case Download.STATUS_PAUSED:
                holder.status.setText(R.string.download_paused);
                break;

            case Download.STATUS_ERROR:
                holder.status.setText(R.string.download_failed);
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.cover)
        SimpleDraweeView cover;

        @InjectView(R.id.title)
        TextView title;

        @InjectView(R.id.progress)
        ProgressBar progressBar;

        @InjectView(R.id.status)
        TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}

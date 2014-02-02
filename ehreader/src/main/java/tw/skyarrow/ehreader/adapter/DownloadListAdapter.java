package tw.skyarrow.ehreader.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
import tw.skyarrow.ehreader.activity.DownloadContextMenu;
import tw.skyarrow.ehreader.activity.GalleryActivity;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class DownloadListAdapter extends BaseAdapter {
    private static final boolean MEM_CACHE = true;
    private static final boolean FILE_CACHE = true;

    private FragmentActivity activity;
    private LayoutInflater inflater;
    private List<Download> list;

    public DownloadListAdapter(FragmentActivity activity, List<Download> list) {
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Download getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        Download download = getItem(i);
        Gallery gallery = download.getGallery();

        if (view == null) {
            view = inflater.inflate(R.layout.download_list_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
            view.setClickable(true);
            view.setOnClickListener(new OnClickListener(download));
            view.setOnLongClickListener(new OnLongClickListener(download));
        } else {
            holder = (ViewHolder) view.getTag();
        }

        AQuery aq = new AQuery(view);
        int progress = download.getProgress();
        int total = gallery.getCount();

        holder.title.setText(gallery.getTitle());
        aq.id(holder.cover).image(gallery.getThumbnail(), MEM_CACHE, FILE_CACHE);
        holder.progressBar.setMax(total);
        holder.progressBar.setProgress(progress);
        holder.featureBtn.setOnClickListener(new OnFeatureClickListener(download));

        switch (download.getStatus()) {
            case Download.STATUS_DOWNLOADING:
                holder.progressText.setText(String.format("%d / %d (%.2f%%)", progress, total, progress * 100f / total));
                break;

            case Download.STATUS_SUCCESS:
                holder.progressText.setText(R.string.download_success);
                break;

            case Download.STATUS_ERROR:
                holder.progressText.setText(R.string.download_failed);
                break;

            case Download.STATUS_PAUSED:
                holder.progressText.setText(R.string.download_paused);
                break;

            case Download.STATUS_PENDING:
            case Download.STATUS_RETRY:
                holder.progressText.setText(R.string.download_pending);
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

    private class OnClickListener implements View.OnClickListener {
        private Download download;

        public OnClickListener(Download download) {
            this.download = download;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, GalleryActivity.class);
            Bundle args = new Bundle();

            args.putLong("id", download.getId());
            intent.putExtras(args);
            activity.startActivity(intent);
        }
    }

    private class OnLongClickListener implements View.OnLongClickListener {
        private Download download;

        public OnLongClickListener(Download download) {
            this.download = download;
        }

        @Override
        public boolean onLongClick(View view) {
            showContextMenu(download);
            return true;
        }
    }

    private class OnFeatureClickListener implements View.OnClickListener {
        private Download download;

        public OnFeatureClickListener(Download download) {
            this.download = download;
        }

        @Override
        public void onClick(View view) {
            showContextMenu(download);
        }
    }

    private void showContextMenu(Download download) {
        DialogFragment dialog = new DownloadContextMenu();
        Bundle args = new Bundle();

        args.putLong("id", download.getId());
        args.putString("title", download.getGallery().getTitle());

        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), DownloadContextMenu.TAG);
    }
}

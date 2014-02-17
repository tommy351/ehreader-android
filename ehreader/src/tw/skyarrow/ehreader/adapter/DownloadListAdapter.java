package tw.skyarrow.ehreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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
public class DownloadListAdapter extends BaseListAdapter<Download> {
    private ImageLoader imageLoader;
    private DisplayImageOptions displayOptions;

    public DownloadListAdapter(Context context, List<Download> list) {
        super(context, list);

        imageLoader = ImageLoader.getInstance();
        displayOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .build();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        Download download = getItem(i);
        Gallery gallery = download.getGallery();

        if (view == null) {
            view = getInflater().inflate(R.layout.download_list_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
            view.setClickable(true);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        int progress = download.getProgress();
        int total = gallery.getCount();

        view.setOnClickListener(new OnClickListener(download));
        view.setOnLongClickListener(new OnLongClickListener(download));
        holder.title.setText(gallery.getTitle());
        holder.progressBar.setMax(total);
        holder.progressBar.setProgress(progress);
        holder.featureBtn.setOnClickListener(new OnFeatureClickListener(download));

        if (isScrolling()) {
            holder.cover.setImageBitmap(null);
        } else {
            imageLoader.displayImage(gallery.getThumbnail(), holder.cover, displayOptions);
        }

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
            Intent intent = new Intent(getContext(), GalleryActivity.class);
            Bundle args = new Bundle();

            args.putLong("id", download.getId());
            intent.putExtras(args);
            getContext().startActivity(intent);
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
        dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), DownloadContextMenu.TAG);
    }
}

package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.BaseListAdapter;
import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class GalleryListAdapter extends BaseListAdapter<Gallery> {
    private ImageLoader imageLoader;
    private DisplayImageOptions displayOptions;

    public GalleryListAdapter(Context context, List<Gallery> list) {
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
        Gallery gallery = getItem(i);

        if (view == null) {
            view = getInflater().inflate(R.layout.gallery_list_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        int categoryRes = gallery.getCategoryResource();
        String meta = getContext().getString(categoryRes) + " / " + gallery.getCount() + "P";
        String[] titles = gallery.getTitles(getContext());

        holder.title.setText(titles[0]);
        holder.meta.setText(meta);
        holder.rating.setRating(gallery.getRating());

        if (isScrolling()) {
            holder.cover.setImageBitmap(null);
        } else {
            imageLoader.displayImage(gallery.getThumbnail(), holder.cover, displayOptions);
        }

        return view;
    }

    static final class ViewHolder {
        @InjectView(R.id.cover)
        ImageView cover;

        @InjectView(R.id.meta)
        TextView meta;

        @InjectView(R.id.title)
        TextView title;

        @InjectView(R.id.rating)
        RatingBar rating;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

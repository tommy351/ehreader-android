package tw.skyarrow.ehreader.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class GalleryListAdapter extends BaseListAdapter<Gallery> {
    private static final boolean MEM_CACHE = true;
    private static final boolean FILE_CACHE = true;

    public GalleryListAdapter(Context context, List<Gallery> list) {
        super(context, list);
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

        AQuery aq = new AQuery(view);
        int categoryRes = gallery.getCategoryResource();
        String meta = getContext().getString(categoryRes) + " / " + gallery.getCount() + "P";

        holder.title.setText(gallery.getTitle());
        holder.meta.setText(meta);
        holder.rating.setRating(gallery.getRating());

        if (isScrolling()) {
            holder.cover.setImageBitmap(null);
        } else {
            aq.id(holder.cover).image(gallery.getThumbnail(), MEM_CACHE, FILE_CACHE);
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

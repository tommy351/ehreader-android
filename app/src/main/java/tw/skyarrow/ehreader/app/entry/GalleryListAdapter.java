package tw.skyarrow.ehreader.app.entry;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Gallery;

/**
 * Created by SkyArrow on 2015/9/25.
 */
public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder> {
    private static final Pattern pThumbUrl = Pattern.compile("(\\d+)-(\\d+)-\\w+_l\\.jpg$");

    private Context context;
    private List<Gallery> list;

    public GalleryListAdapter(Context context, List<Gallery> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Gallery gallery = list.get(position);
        String thumb = gallery.getThumbnail();
        Matcher matcher = pThumbUrl.matcher(thumb);
        String[] titles = gallery.getPreferredTitles(context);

        if (matcher.find()) {
            int width = Integer.parseInt(matcher.group(1), 10);
            int height = Integer.parseInt(matcher.group(2), 10);
            holder.cover.setAspectRatio((float) width / height);
        } else {
            holder.cover.setAspectRatio(1f);
        }

        holder.title.setText(titles[0]);
        holder.cover.setImageURI(Uri.parse(thumb));
        holder.category.setText(gallery.getCategoryString());
        holder.category.setTextColor(ContextCompat.getColor(context, gallery.getCategoryColor()));
        holder.count.setText(String.format("%dP", gallery.getCount()));
        holder.favoriteBadge.setVisibility(gallery.getStarred() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        Gallery gallery = list.get(position);
        return gallery.getId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.cover)
        SimpleDraweeView cover;

        @InjectView(R.id.title)
        TextView title;

        @InjectView(R.id.category)
        TextView category;

        @InjectView(R.id.count)
        TextView count;

        @InjectView(R.id.favorite)
        ImageView favoriteBadge;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}

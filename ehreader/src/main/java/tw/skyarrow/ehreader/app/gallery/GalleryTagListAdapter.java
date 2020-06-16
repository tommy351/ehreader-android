package tw.skyarrow.ehreader.app.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.GalleryTag;

public class GalleryTagListAdapter extends RecyclerView.Adapter<GalleryTagListAdapter.ViewHolder> {
    private Context mContext;
    private List<GalleryTag> mTagList;

    public GalleryTagListAdapter(Context context){
        mContext = context;
    }

    public GalleryTagListAdapter(Context context, List<GalleryTag> tagList){
        mContext = context;
        mTagList = tagList;
    }

    @Override
    public int getItemCount() {
        return mTagList == null ? 0 : mTagList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mTagList == null) return;

        GalleryTag tag = mTagList.get(position);

        holder.titleView.setText(tag.getTitle());
    }

    public List<GalleryTag> getTagList() {
        return mTagList;
    }

    public void setTagList(List<GalleryTag> tagList) {
        mTagList = tagList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.title)
        TextView titleView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

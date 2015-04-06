package tw.skyarrow.ehreader.app.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class GalleryTagListAdapter extends RecyclerView.Adapter<GalleryTagListAdapter.ViewHolder> {
    public GalleryTagListAdapter(Context context){
        //
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}

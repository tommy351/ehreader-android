package tw.skyarrow.ehreader.app.entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public abstract class GalleryListFragment extends Fragment implements RecyclerViewItemClickListener.OnItemClickListener {
    protected List<Gallery> galleryList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        galleryList = new ArrayList<>();
    }

    @Override
    public void onItemClick(View view, int position) {
        Gallery gallery = galleryList.get(position);
        Intent intent = GalleryActivity.intent(getActivity(), gallery.getId());

        startActivity(intent);
    }

    @Override
    public void onItemLongPress(View view, int position) {

    }
}

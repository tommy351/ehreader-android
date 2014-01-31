package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;

import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class MainFragmentBase extends Fragment implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Gallery gallery = (Gallery) adapterView.getAdapter().getItem(i);

        if (gallery == null) return;

        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());
        intent.putExtras(args);
        startActivity(intent);
    }
}

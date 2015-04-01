package tw.skyarrow.ehreader.app.photo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.skyarrow.ehreader.R;

public class PhotoFragment extends Fragment {
    public static final String TAG = PhotoFragment.class.getSimpleName();

    public static final String EXTRA_GALLERY_ID = "gallery_id";
    public static final String EXTRA_PAGE = "page";

    public static PhotoFragment newInstance(long galleryId, int page){
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_GALLERY_ID, galleryId);
        args.putInt(EXTRA_PAGE, page);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        return view;
    }
}

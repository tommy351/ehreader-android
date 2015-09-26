package tw.skyarrow.ehreader.app.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.photo.PhotoActivity;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class GalleryAboutFragment extends Fragment {
    private static final String GALLERY_ID = "GALLERY_ID";

    @InjectView(R.id.title)
    TextView titleText;

    @InjectView(R.id.subtitle)
    TextView subtitleText;

    @InjectView(R.id.rating)
    RatingBar ratingBar;

    @InjectView(R.id.info)
    TextView infoText;

    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private long galleryId;
    private Gallery gallery;

    public static GalleryAboutFragment create(long galleryId) {
        GalleryAboutFragment fragment = new GalleryAboutFragment();
        Bundle args = new Bundle();

        args.putLong(GALLERY_ID, galleryId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle args = getArguments();
        galleryId = args.getLong(GALLERY_ID);
        dbHelper = DatabaseHelper.get(getActivity());
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        gallery = galleryDao.load(galleryId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_about, container, false);
        ButterKnife.inject(this, view);

        titleText.setText(gallery.getTitle());
        ratingBar.setRating(gallery.getRating());

        String subtitle = gallery.getSubtitle();

        if (TextUtils.isEmpty(subtitle)){
            subtitleText.setVisibility(View.GONE);
        } else {
            subtitleText.setText(subtitle);
        }

        String category = getString(gallery.getCategoryString());
        SpannableString spannableString = new SpannableString(String.format("%s / %dP", category, gallery.getCount()));
        spannableString.setSpan(new ForegroundColorSpan(gallery.getCategoryColor()), 0, category.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText.setText(spannableString);

        return view;
    }

    @OnClick(R.id.read_btn)
    void onReadBtnPressed(){
        Intent intent = PhotoActivity.intent(getActivity(), galleryId);
        startActivity(intent);
    }
}

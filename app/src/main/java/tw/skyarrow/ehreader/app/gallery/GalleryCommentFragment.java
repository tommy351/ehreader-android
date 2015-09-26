package tw.skyarrow.ehreader.app.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.API;
import tw.skyarrow.ehreader.api.APIService;
import tw.skyarrow.ehreader.model.Comment;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class GalleryCommentFragment extends Fragment {
    private static final String GALLERY_ID = "GALLERY_ID";

    // Posted on 26 September 2015, 01:36 UTC by:
    private static final SimpleDateFormat commentDateFormat = new SimpleDateFormat("'Posted on 'd MMMMM yyyy', 'HH:mm z' by:'", Locale.ENGLISH);

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    @InjectView(R.id.loading)
    ProgressBar progressBar;

    private APIService api;
    private CommentListAdapter listAdapter;
    private List<Comment> commentList;
    private long galleryId;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private Gallery gallery;

    public static GalleryCommentFragment create(long galleryId) {
        GalleryCommentFragment fragment = new GalleryCommentFragment();
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
        commentList = new ArrayList<>();
        api = API.getService(getActivity());
        dbHelper = DatabaseHelper.get(getActivity());
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        gallery = galleryDao.load(galleryId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_comment, container, false);
        ButterKnife.inject(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        listAdapter = new CommentListAdapter(getActivity(), commentList);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            loadCommentList();
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void loadCommentList(){
        progressBar.setVisibility(View.VISIBLE);

        api.getGalleryPage(galleryId, gallery.getToken(), 0)
                .flatMap(html -> {
                    Document doc = Jsoup.parse(html);
                    List<Comment> comments = new ArrayList<>();
                    Elements elements = doc.getElementsByClass("c1");

                    for (Element element : elements){
                        Comment comment = new Comment();
                        Element header = element.getElementsByClass("c3").first();
                        String content = element.getElementsByClass("c6").first().text();
                        String dateText = header.textNodes().get(0).text().trim();

                        try {
                            comment.setDate(commentDateFormat.parse(dateText));
                        } catch (ParseException e){
                            // Do nothing
                        }

                        comment.setAuthor(header.child(0).text());
                        comment.setContent(content);
                        comments.add(comment);
                        commentList.add(comment);
                    }

                    return Observable.from(comments);
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Comment>() {
                    @Override
                    public void onCompleted() {
                        listAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(Comment comment) {

                    }
                });
    }
}

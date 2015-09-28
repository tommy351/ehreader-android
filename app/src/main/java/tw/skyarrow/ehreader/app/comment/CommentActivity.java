package tw.skyarrow.ehreader.app.comment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.drawee.backends.pipeline.Fresco;

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
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.Comment;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.FabricHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.ToolbarHelper;

/**
 * Created by SkyArrow on 2015/9/27.
 */
public class CommentActivity extends AppCompatActivity {
    public static final String GALLERY_ID = "GALLERY_ID";

    // Posted on 26 September 2015, 01:36 UTC by:
    private static final SimpleDateFormat commentDateFormat = new SimpleDateFormat("'Posted on 'd MMMMM yyyy', 'HH:mm z' by:'", Locale.ENGLISH);

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    @InjectView(R.id.loading)
    ProgressBar progressBar;

    private long galleryId;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private Gallery gallery;
    private APIService api;
    private CommentListAdapter listAdapter;
    private List<Comment> commentList;

    public static Intent intent(Context context, long galleryId) {
        Intent intent = new Intent(context, CommentActivity.class);
        Bundle args = bundle(galleryId);

        intent.putExtras(args);

        return intent;
    }

    public static Bundle bundle(long galleryId){
        Bundle bundle = new Bundle();

        bundle.putLong(GALLERY_ID, galleryId);

        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_comment);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.label_comments);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        galleryId = args.getLong(GALLERY_ID);
        dbHelper = DatabaseHelper.get(this);
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        gallery = galleryDao.load(galleryId);
        api = API.getService(this);
        commentList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listAdapter = new CommentListAdapter(this, commentList);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        loadCommentList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ToolbarHelper.upNavigation(this, GalleryActivity.bundle(galleryId));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void loadCommentList(){
        progressBar.setVisibility(View.VISIBLE);

        L.d("loadCommentList: %d", galleryId);

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
                            return Observable.error(e);
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
                        L.e(e, "loadCommentList");
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(Comment comment) {

                    }
                });
    }
}

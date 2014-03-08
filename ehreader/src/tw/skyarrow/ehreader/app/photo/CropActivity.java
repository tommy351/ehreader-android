package tw.skyarrow.ehreader.app.photo;

import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.edmodo.cropper.CropImageView;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/9.
 */
public class CropActivity extends FragmentActivity {
    @InjectView(R.id.image)
    CropImageView imageView;

    public static final String TAG = "CropActivity";

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ButterKnife.inject(this);

        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            try {
                InputStream stream = getContentResolver().openInputStream(data);
                bitmap = BitmapFactory.decodeStream(stream);
                WallpaperManager wm = WallpaperManager.getInstance(this);
                int width = wm.getDesiredMinimumWidth();
                int height = wm.getDesiredMinimumHeight();

                if (width <= 0 || height <= 0) {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    width = size.x;
                    height = size.y;
                }

                L.d("%d x %d", width, height);

                imageView.setAspectRatio(width, height);
                imageView.setFixedAspectRatio(true);
                displayImage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crop, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_rotate_left:
                rotateLeft();
                return true;

            case R.id.menu_rotate_right:
                rotateRight();
                return true;

            case R.id.menu_ok:
                setWallpaper();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayImage() {
        imageView.setImageBitmap(bitmap);
    }

    private void rotateLeft() {
        rotate(-90);
    }

    private void rotateRight() {
        rotate(90);
    }

    // http://stackoverflow.com/a/8996581
    private void rotate(int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        displayImage();
    }

    private void setWallpaper() {
        Bitmap bitmap = imageView.getCroppedImage();

        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            wm.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        finish();
    }
}

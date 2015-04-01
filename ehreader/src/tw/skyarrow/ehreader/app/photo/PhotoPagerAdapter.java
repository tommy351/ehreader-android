package tw.skyarrow.ehreader.app.photo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import tw.skyarrow.ehreader.model.Photo;

public class PhotoPagerAdapter extends FragmentStatePagerAdapter {
    private List<Photo> mPhotoList;

    public PhotoPagerAdapter(FragmentManager fm){
        super(fm);
    }

    public PhotoPagerAdapter(FragmentManager fm, List<Photo> photoList){
        this(fm);
        mPhotoList = photoList;
    }

    @Override
    public Fragment getItem(int position) {
        Photo photo = mPhotoList.get(position);
        return PhotoFragment.newInstance(photo.getGalleryId(), photo.getPage());
    }

    @Override
    public int getCount() {
        return mPhotoList.size();
    }
}
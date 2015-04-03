package tw.skyarrow.ehreader.app.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentAdapter extends FragmentPagerAdapter {
    private String[] mTabs;

    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public MainFragmentAdapter(FragmentManager fm, String[] tabs) {
        this(fm);
        mTabs = tabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return SearchFragment.newInstance("");

            case 1:
                return FavoritesFragment.newInstance();

            case 2:
                return HistoryFragment.newInstance();

            case 3:
                return DownloadFragment.newInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        return mTabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return mTabs[position];
    }
}

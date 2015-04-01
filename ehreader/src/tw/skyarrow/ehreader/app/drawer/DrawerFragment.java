package tw.skyarrow.ehreader.app.drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.main.CollectionFragment;
import tw.skyarrow.ehreader.app.main.DownloadFragment;
import tw.skyarrow.ehreader.app.main.HistoryFragment;
import tw.skyarrow.ehreader.app.main.SearchFragment;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public class DrawerFragment extends Fragment {
    public static final String TAG = DrawerFragment.class.getSimpleName();

    public static final int TAB_LATEST = 1;
    public static final int TAB_COLLECTION = 2;
    public static final int TAB_HISTORY = 3;
    public static final int TAB_DOWNLOAD = 4;

    @InjectView(R.id.main_menu)
    RecyclerView mRecyclerView;

    private int mCurrentPage;

    public static DrawerFragment newInstance(){
        return new DrawerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer, container, false);
        ButterKnife.inject(this, view);

        createMainMenu();

        return view;
    }

    private void createMainMenu(){
        MenuItem[] menuItems = getMenuItemList(R.array.main_menu);

        menuItems[0].setIcon(R.drawable.drawer_latest);
        menuItems[1].setIcon(R.drawable.drawer_collection);
        menuItems[2].setIcon(R.drawable.drawer_history);
        menuItems[3].setIcon(R.drawable.drawer_download);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        MainMenuAdapter adapter = new MainMenuAdapter(menuItems);
        RecyclerViewItemClickListener.SimpleOnItemClickListener itemClickListener = new RecyclerViewItemClickListener.SimpleOnItemClickListener(){
            @Override
            public void onItemClick(View childView, final int position) {
                onMenuMainItemClick(position);
            }
        };

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), itemClickListener));
    }

    private void onMenuMainItemClick(int position){
        if (!setCurrentPage(position)){
            //
        }

        closeDrawer();
    }

    private void closeDrawer(){
        DrawerActivity activity = (DrawerActivity) getActivity();
        activity.closeDrawer();
    }

    private MenuItem[] getMenuItemList(int res){
        String[] arr = getResources().getStringArray(res);
        int length = arr.length;
        MenuItem[] menuItems = new MenuItem[length];

        for (int i = 0; i < length; i++){
            menuItems[i] = new MenuItem(arr[i]);
        }

        return menuItems;
    }

    public int getCurrentPage(){
        return mCurrentPage;
    }

    public boolean setCurrentPage(int page){
        if (mCurrentPage == page) return false;

        Fragment fragment = null;
        String tag = null;
        mCurrentPage = page;
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        switch (page){
            case TAB_LATEST:
                fragment = SearchFragment.newInstance(Constant.BASE_URL);
                tag = SearchFragment.TAG;
                break;

            case TAB_COLLECTION:
                fragment = CollectionFragment.newInstance();
                tag = CollectionFragment.TAG;
                break;

            case TAB_HISTORY:
                fragment = HistoryFragment.newInstance();
                tag = HistoryFragment.TAG;
                break;

            case TAB_DOWNLOAD:
                fragment = DownloadFragment.newInstance();
                tag = DownloadFragment.TAG;
                break;
        }

        if (fragment == null) return false;

        ft.replace(R.id.frame, fragment, tag);
        ft.commit();

        return true;
    }
}

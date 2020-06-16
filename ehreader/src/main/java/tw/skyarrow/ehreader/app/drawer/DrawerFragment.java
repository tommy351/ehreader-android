package tw.skyarrow.ehreader.app.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.login.LoginActivity;
import tw.skyarrow.ehreader.app.login.LogoutDialog;
import tw.skyarrow.ehreader.app.main.DownloadListFragment;
import tw.skyarrow.ehreader.app.main.FavoritesFragment;
import tw.skyarrow.ehreader.app.main.HistoryFragment;
import tw.skyarrow.ehreader.app.main.WebFragment;
import tw.skyarrow.ehreader.app.pref.PrefActivity;
import tw.skyarrow.ehreader.event.LoginEvent;
import tw.skyarrow.ehreader.util.LoginHelper;
import tw.skyarrow.ehreader.util.PrefHelper;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public class DrawerFragment extends Fragment {
    public static final String TAG = DrawerFragment.class.getSimpleName();

    public static final String EXTRA_TAB = "tab";

    public static final int TAB_LATEST = 1;
    public static final int TAB_FAVORITES = 2;
    public static final int TAB_HISTORY = 3;
    public static final int TAB_DOWNLOAD = 4;
    public static final int TAB_PREF = 5;
    public static final int TAB_DONATE = 6;

    @InjectView(R.id.main_menu)
    RecyclerView mRecyclerView;

    private int mCurrentPage;
    private MainMenuAdapter mMenuAdapter;
    private List<MenuItem> mMenuItems;
    private PrefHelper prefHelper;
    private int mTab;

    public static DrawerFragment newInstance(int tab){
        DrawerFragment fragment = new DrawerFragment();
        Bundle args = new Bundle();

        args.putInt(EXTRA_TAB, tab);
        fragment.setArguments(args);

        return fragment;
    }

    public static DrawerFragment newInstance(){
        return newInstance(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle args = getArguments();
        prefHelper = PrefHelper.newInstance(getActivity());
        String launchPagePref = prefHelper.getString(R.string.pref_launch_page, getString(R.string.pref_launch_page_default));
        int tab = Integer.parseInt(launchPagePref);
        mTab = args.getInt(EXTRA_TAB);

        if (mTab == 0){
            mTab = tab + 1;
        }

        // Main menu
        mMenuItems = getMenuItemList(R.array.main_menu);
        mMenuItems.get(0).setIcon(R.drawable.drawer_latest, R.drawable.drawer_latest_selected);
        mMenuItems.get(1).setIcon(R.drawable.drawer_favorites, R.drawable.drawer_favorite_selected);
        mMenuItems.get(2).setIcon(R.drawable.drawer_history, R.drawable.drawer_history_selected);
        mMenuItems.get(3).setIcon(R.drawable.drawer_download, R.drawable.drawer_download_selected);

        // Sub menu
        mMenuItems.addAll(getMenuItemList(R.array.sub_menu));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer, container, false);
        ButterKnife.inject(this, view);

        mMenuAdapter = new MainMenuAdapter(getActivity(), mMenuItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerViewItemClickListener.SimpleOnItemClickListener itemClickListener = new RecyclerViewItemClickListener.SimpleOnItemClickListener(){
            @Override
            public void onItemClick(View childView, final int position) {
                if (position == 0){
                    onHeaderClick();
                } else {
                    onMenuMainItemClick(position);
                }
            }
        };

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mMenuAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), itemClickListener));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mCurrentPage == 0){
            setCurrentPage(mTab);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(LoginEvent event){
        if (mMenuAdapter != null){
            mMenuAdapter.notifyHeaderDataSetChanged();
        }
    }

    private void onHeaderClick(){
        if (LoginHelper.getInstance(getActivity()).isLoggedIn()){
            LogoutDialog dialog = LogoutDialog.newInstance();
            dialog.show(getActivity().getSupportFragmentManager(), LogoutDialog.TAG);
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void onMenuMainItemClick(int position){
        if (!setCurrentPage(position)){
            switch (position){
                case TAB_PREF:
                    openPreference();
                    break;

                case TAB_DONATE:
                    openDonate();
                    break;
            }
        }

        closeDrawer();
    }

    private void closeDrawer(){
        DrawerActivity activity = (DrawerActivity) getActivity();
        activity.closeDrawer();
    }

    private List<MenuItem> getMenuItemList(int res){
        String[] arr = getResources().getStringArray(res);
        List<MenuItem> menuItems = new ArrayList<>();

        for (String title : arr){
            menuItems.add(new MenuItem(title));
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
        boolean loggedIn = LoginHelper.getInstance(getActivity()).isLoggedIn();

        switch (page){
            case TAB_LATEST:
                fragment = WebFragment.newInstance(loggedIn ? Constant.BASE_URL_EX : Constant.BASE_URL);
                tag = WebFragment.TAG;
                break;

            case TAB_FAVORITES:
                fragment = FavoritesFragment.newInstance();
                tag = FavoritesFragment.TAG;
                break;

            case TAB_HISTORY:
                fragment = HistoryFragment.newInstance();
                tag = HistoryFragment.TAG;
                break;

            case TAB_DOWNLOAD:
                fragment = DownloadListFragment.newInstance();
                tag = DownloadListFragment.TAG;
                break;
        }

        if (fragment == null) return false;

        // Update menu
        if (mCurrentPage != 0) mMenuItems.get(mCurrentPage - 1).setSelected(false);
        mMenuItems.get(page - 1).setSelected(true);
        mMenuAdapter.notifyDataSetChanged();

        // Replace fragment
        mCurrentPage = page;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frame, fragment, tag);
        ft.commit();

        return true;
    }

    private void openPreference(){
        Intent intent = new Intent(getActivity(), PrefActivity.class);
        getActivity().startActivity(intent);
    }

    private void openDonate(){
        //
    }
}

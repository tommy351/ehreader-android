package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.DrawerActivity;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public class MainDrawerActivity extends DrawerActivity {
    private DrawerHeaderAdapter headerAdapter;

    @Override
    protected void setupDrawer() {
        super.setupDrawer();

        List<DrawerItem> list = new ArrayList<DrawerItem>();
        ListView drawerView = (ListView) getDrawerView();
        DrawerAdapter adapter = new DrawerAdapter(this, list);

        list.add(new DrawerItem(getString(R.string.drawer_settings), R.drawable.ic_drawer_settings));

        drawerView.setAdapter(adapter);

        ListView headerView = (ListView) getLayoutInflater().inflate(R.layout.drawer_header, null);
        List<DrawerItem> headerList = new ArrayList<DrawerItem>();

        String[] menuItems = getResources().getStringArray(R.array.main_tabs);

        headerList.add(new DrawerItem(menuItems[0], R.drawable.ic_drawer_gallery));
        headerList.add(new DrawerItem(menuItems[1], R.drawable.ic_drawer_star));
        headerList.add(new DrawerItem(menuItems[2], R.drawable.ic_drawer_history));
        headerList.add(new DrawerItem(menuItems[3], R.drawable.ic_drawer_download));

        headerAdapter = new DrawerHeaderAdapter(this, headerList) {
            @Override
            public void onItemClick(int i) {
                MainDrawerActivity.this.onItemClick(i);
            }
        };

        headerView.setAdapter(headerAdapter);
        drawerView.addHeaderView(headerView);
    }

    public void onItemClick(int i) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(MainActivity.EXTRA_TAB, i);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
  }

    public DrawerHeaderAdapter getDrawerAdapter() {
        return headerAdapter;
    }
}

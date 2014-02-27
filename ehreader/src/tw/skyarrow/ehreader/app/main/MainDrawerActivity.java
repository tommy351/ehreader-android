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
    private DrawerAdapter adapter;

    @Override
    protected void setupDrawer() {
        super.setupDrawer();

        List<DrawerItem> list = new ArrayList<DrawerItem>();
        ListView drawerView = (ListView) getDrawerView();
        adapter = new DrawerAdapter(this, list) {
            @Override
            public void onItemClick(int i) {
                MainDrawerActivity.this.onItemClick(i);
            }
        };

        String[] menuItems = getResources().getStringArray(R.array.main_tabs);

        list.add(new DrawerItem(menuItems[0], R.drawable.ic_drawer_gallery));
        list.add(new DrawerItem(menuItems[1], R.drawable.ic_drawer_star));
        list.add(new DrawerItem(menuItems[2], R.drawable.ic_drawer_history));
        list.add(new DrawerItem(menuItems[3], R.drawable.ic_drawer_download));

        drawerView.setAdapter(adapter);
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

    public DrawerAdapter getDrawerAdapter() {
        return adapter;
    }
}

package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.SearchBarActivity;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public class MainDrawerActivity extends SearchBarActivity {
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

        for (int i = 0, len = menuItems.length; i < len; i++) {
            list.add(new DrawerItem(menuItems[i]));
        }

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

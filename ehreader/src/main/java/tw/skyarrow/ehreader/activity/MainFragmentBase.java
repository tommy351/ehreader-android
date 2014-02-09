package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.adapter.GalleryListAdapter;
import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class MainFragmentBase extends Fragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private ListView listView;
    private List<Gallery> list;
    private GalleryListAdapter adapter;

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public List<Gallery> getList() {
        return list;
    }

    public GalleryListAdapter getAdapter() {
        return adapter;
    }

    public void setList(List<Gallery> list) {
        this.list = list;
    }

    public void setAdapter(GalleryListAdapter adapter) {
        this.adapter = adapter;
    }

    public int getCount() {
        return list.size();
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);

        listView = (ListView) view.findViewById(R.id.list);
        list = new ArrayList<Gallery>();
        adapter = new GalleryListAdapter(getActivity(), list);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt("position"));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("position", listView.getSelectedItemPosition());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Gallery gallery = (Gallery) adapterView.getAdapter().getItem(i);

        if (gallery == null) return;

        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());
        intent.putExtras(args);
        startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            adapter.setScrolling(false);
            adapter.notifyDataSetChanged();
        } else {
            adapter.setScrolling(true);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        //
    }
}

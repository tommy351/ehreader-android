package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.event.ListUpdateEvent;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class MainFragmentBase extends Fragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    public static final String EXTRA_POSITION = "position";

    private ListView listView;
    private List<Gallery> list;
    private GalleryListAdapter adapter;
    private EventBus bus;

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
        bus = EventBus.getDefault();
        bus.register(this);

        listView = (ListView) view.findViewById(R.id.list);
        list = new ArrayList<Gallery>();
        adapter = new GalleryListAdapter(getActivity(), list);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt(EXTRA_POSITION));
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_POSITION, listView.getSelectedItemPosition());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Gallery gallery = (Gallery) adapterView.getAdapter().getItem(i);

        if (gallery == null) return;

        Intent intent = new Intent(getActivity(), GalleryActivity.class);

        intent.putExtra(GalleryActivity.EXTRA_GALLERY, gallery.getId());
        startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            adapter.setScrolling(true);
        } else {
            adapter.setScrolling(false);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        //
    }

    public void onEvent(ListUpdateEvent event) {
        adapter.notifyDataSetChanged();
    }
}

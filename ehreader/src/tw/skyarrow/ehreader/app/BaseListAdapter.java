package tw.skyarrow.ehreader.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by SkyArrow on 2014/2/9.
 */
public abstract class BaseListAdapter<E> extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<E> list;
    private boolean isScrolling = false;

    public BaseListAdapter(Context context, List<E> list) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public List<E> getList() {
        return list;
    }

    public void setList(List<E> list) {
        this.list = list;
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public E getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public abstract View getView(int i, View view, ViewGroup viewGroup);
}

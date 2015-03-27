package tw.skyarrow.ehreader.services;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import tw.skyarrow.ehreader.view.HeaderRecyclerViewAdapter;

public class MainMenuAdapter extends HeaderRecyclerViewAdapter {
    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public int getContentItemCount() {
        return 0;
    }

    @Override
    public int getFooterItemCount() {
        return 0;
    }
}

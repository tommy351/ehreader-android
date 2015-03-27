package tw.skyarrow.ehreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class HeaderRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final int TYPE_HEADER = -1;
    private static final int TYPE_FOOTER = -2;

    private int headerItemCount;
    private int contentItemCount;
    private int footerItemCount;

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_HEADER:
                return onCreateHeaderViewHolder(parent, viewType);

            case TYPE_FOOTER:
                return onCreateFooterViewHolder(parent, viewType);
        }

        return onCreateContentViewHolder(parent, viewType);
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_HEADER:
                onBindHeaderViewHolder(holder, position);
                break;

            case TYPE_FOOTER:
                onBindFooterViewHolder(holder, position - headerItemCount - contentItemCount);
                break;

            default:
                onBindContentViewHolder(holder, position - headerItemCount);
        }
    }

    @Override
    public final int getItemCount() {
        headerItemCount = getHeaderItemCount();
        contentItemCount = getContentItemCount();
        footerItemCount = getFooterItemCount();

        return headerItemCount + contentItemCount + footerItemCount;
    }

    @Override
    public final int getItemViewType(int position) {
        if (position < headerItemCount){
            return TYPE_HEADER;
        } else if (position > headerItemCount + contentItemCount){
            return TYPE_FOOTER;
        }

        return 0;
    }

    public final void notifyHeaderDataSetChanged(){
        //
    }

    public final void notifyHeaderItemChanged(int position){
        //
    }

    public final void notifyHeaderItemInserted(int position){
        //
    }

    public final void notifyHeaderItemMoved(int fromPosition, int toPosition){
        //
    }

    public final void notifyHeaderItemRangeChanged(int startPosition, int itemCount){
        //
    }

    public final void notifyHeaderItemRangeInserted(int startPosition, int itemCount){
        //
    }

    public final void notifyHeaderItemRangeRemoved(int startPosition, int itemCount){
        //
    }

    public final void notifyHeaderItemRemoved(int position){
        //
    }

    public final void notifyContentDataSetChanged(){
        //
    }

    public final void notifyContentItemChanged(int position){
        //
    }

    public final void notifyContentItemInserted(int position){
        //
    }

    public final void notifyContentItemMoved(int fromPosition, int toPosition){
        //
    }

    public final void notifyContentItemRangeChanged(int startPosition, int itemCount){
        //
    }

    public final void notifyContentItemRangeInserted(int startPosition, int itemCount){
        //
    }

    public final void notifyContentItemRangeRemoved(int startPosition, int itemCount){
        //
    }

    public final void notifyContentItemRemoved(int position){
        //
    }

    public final void notifyFooterDataSetChanged(){
        //
    }

    public final void notifyFooterItemChanged(int position){
        //
    }

    public final void notifyFooterItemInserted(int position){
        //
    }

    public final void notifyFooterItemMoved(int fromPosition, int toPosition){
        //
    }

    public final void notifyFooterItemRangeChanged(int startPosition, int itemCount){
        //
    }

    public final void notifyFooterItemRangeInserted(int startPosition, int itemCount){
        //
    }

    public final void notifyFooterItemRangeRemoved(int startPosition, int itemCount){
        //
    }

    public final void notifyFooterItemRemoved(int position){
        //
    }

    public abstract RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType);

    public abstract RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType);

    public abstract RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract int getHeaderItemCount();

    public abstract int getContentItemCount();

    public abstract int getFooterItemCount();
}

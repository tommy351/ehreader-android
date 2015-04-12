package tw.skyarrow.ehreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class RecyclerViewAdapter extends RecyclerView.Adapter {
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
        } else if (position >= headerItemCount + contentItemCount){
            return TYPE_FOOTER;
        }

        return 0;
    }

    @Override
    public final long getItemId(int position) {
        switch (getItemViewType(position)){
            case TYPE_HEADER:
                return getHeaderItemId(position);

            case TYPE_FOOTER:
                return getFooterItemId(position - headerItemCount - contentItemCount);

            default:
                return getContentItemId(position - headerItemCount);
        }
    }

    public final void notifyHeaderDataSetChanged(){
        notifyItemRangeChanged(0, headerItemCount);
    }

    public final void notifyHeaderItemChanged(int position){
        notifyItemChanged(position);
    }

    public final void notifyHeaderItemInserted(int position){
        notifyItemInserted(position);
    }

    public final void notifyHeaderItemMoved(int fromPosition, int toPosition){
        notifyItemMoved(fromPosition, toPosition);
    }

    public final void notifyHeaderItemRangeChanged(int startPosition, int itemCount){
        notifyItemRangeChanged(startPosition, itemCount);
    }

    public final void notifyHeaderItemRangeInserted(int startPosition, int itemCount){
        notifyItemRangeInserted(startPosition, itemCount);
    }

    public final void notifyHeaderItemRangeRemoved(int startPosition, int itemCount){
        notifyItemRangeRemoved(startPosition, itemCount);
    }

    public final void notifyHeaderItemRemoved(int position){
        notifyItemRemoved(position);
    }

    public final void notifyContentDataSetChanged(){
        notifyItemRangeChanged(headerItemCount, contentItemCount);
    }

    public final void notifyContentItemChanged(int position){
        notifyItemChanged(headerItemCount + position);
    }

    public final void notifyContentItemInserted(int position){
        notifyItemInserted(headerItemCount + position);
    }

    public final void notifyContentItemMoved(int fromPosition, int toPosition){
        notifyItemMoved(headerItemCount + fromPosition, headerItemCount + toPosition);
    }

    public final void notifyContentItemRangeChanged(int startPosition, int itemCount){
        notifyItemRangeChanged(headerItemCount + startPosition, itemCount);
    }

    public final void notifyContentItemRangeInserted(int startPosition, int itemCount){
        notifyItemRangeInserted(headerItemCount + startPosition, itemCount);
    }

    public final void notifyContentItemRangeRemoved(int startPosition, int itemCount){
        notifyItemRangeRemoved(headerItemCount + startPosition, itemCount);
    }

    public final void notifyContentItemRemoved(int position){
        notifyItemRemoved(headerItemCount + position);
    }

    public final void notifyFooterDataSetChanged(){
        notifyItemRangeChanged(headerItemCount + contentItemCount, footerItemCount);
    }

    public final void notifyFooterItemChanged(int position){
        notifyItemChanged(headerItemCount + contentItemCount + position);
    }

    public final void notifyFooterItemInserted(int position){
        notifyItemInserted(headerItemCount + contentItemCount + position);
    }

    public final void notifyFooterItemMoved(int fromPosition, int toPosition){
        notifyItemMoved(headerItemCount + contentItemCount + fromPosition, headerItemCount + contentItemCount + toPosition);
    }

    public final void notifyFooterItemRangeChanged(int startPosition, int itemCount){
        notifyItemRangeChanged(headerItemCount + contentItemCount + startPosition, itemCount);
    }

    public final void notifyFooterItemRangeInserted(int startPosition, int itemCount){
        notifyItemRangeInserted(headerItemCount + contentItemCount + startPosition, itemCount);
    }

    public final void notifyFooterItemRangeRemoved(int startPosition, int itemCount){
        notifyItemRangeRemoved(headerItemCount + contentItemCount + startPosition, itemCount);
    }

    public final void notifyFooterItemRemoved(int position){
        notifyItemRemoved(headerItemCount + contentItemCount + position);
    }

    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType){
        return null;
    }

    public abstract RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType);

    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType){
        return null;
    }

    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position){

    }

    public abstract void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position);

    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position){

    }

    public int getHeaderItemCount(){
        return 0;
    }

    public abstract int getContentItemCount();

    public int getFooterItemCount(){
        return 0;
    }

    public long getHeaderItemId(int position){
        return RecyclerView.NO_ID;
    }

    public long getContentItemId(int position){
        return RecyclerView.NO_ID;
    }

    public long getFooterItemId(int position){
        return RecyclerView.NO_ID;
    }
}

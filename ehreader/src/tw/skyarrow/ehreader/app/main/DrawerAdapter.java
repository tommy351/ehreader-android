package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.BaseListAdapter;
import tw.skyarrow.ehreader.app.pref.PrefActivity;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public class DrawerAdapter extends BaseListAdapter<DrawerItem> {
    private static final int MENU_SETTINGS = 0;

    public DrawerAdapter(Context context, List<DrawerItem> list) {
        super(context, list);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        DrawerItem drawerItem = getItem(i);

        if (view == null) {
            view = getInflater().inflate(R.layout.drawer_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
            view.setClickable(true);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name.setText(drawerItem.getName());
        holder.icon.setImageDrawable(getIcon(drawerItem.getIcon()));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(i);
            }
        });

        return view;
    }

    private Drawable getIcon(int res) {
        return getContext().getResources().getDrawable(res);
    }

    static final class ViewHolder {
        @InjectView(R.id.name)
        TextView name;

        @InjectView(R.id.icon)
        ImageView icon;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public void onItemClick(int i) {
        switch (i) {
            case MENU_SETTINGS:
                Intent intent = new Intent(getContext(), PrefActivity.class);
                getContext().startActivity(intent);

                break;
        }
    }
}

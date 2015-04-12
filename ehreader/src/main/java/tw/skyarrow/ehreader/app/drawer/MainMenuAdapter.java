package tw.skyarrow.ehreader.app.drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.LoginHelper;

public class MainMenuAdapter extends BaseMenuAdapter {
    private static final String[] AVATAR_EXTNAME = new String[]{".jpg", ".png", ".gif"};

    private int mCurrentExtname;

    public MainMenuAdapter(Context context, List<MenuItem> items) {
        super(context, items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder vh = (HeaderViewHolder) holder;
        LoginHelper loginHelper = LoginHelper.getInstance(getContext());

        if (loginHelper.isLoggedIn()){
            if (loginHelper.getUsername().isEmpty()){
                vh.nameView.setVisibility(View.GONE);
            } else {
                vh.nameView.setVisibility(View.VISIBLE);
                vh.nameView.setText(loginHelper.getUsername());
            }

            vh.hintView.setText(getContext().getString(R.string.logout_title));
            loadAvatar(vh);
        } else {
            vh.nameView.setVisibility(View.VISIBLE);
            vh.nameView.setText(getContext().getString(R.string.not_login_placeholder));
            vh.hintView.setText(getContext().getString(R.string.login_title));
            vh.avatarView.setImageBitmap(null);
        }
    }

    @Override
    public int getHeaderItemCount() {
        return 1;
    }

    private void loadAvatar(final HeaderViewHolder vh){
        String src = LoginHelper.getInstance(getContext()).getAvatar();

        if (src.isEmpty()){
            mCurrentExtname = 0;
            tryAvatarSrc(vh);
        } else {
            L.d("Loading avatar: %s", src);

            ImageLoader imageLoader = ImageLoaderHelper.getImageLoader(getContext());
            imageLoader.get(src, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    vh.avatarView.setImageBitmap(imageContainer.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    // TODO: load placeholder avatar
                    L.e(volleyError);
                }
            });
        }
    }

    private void tryAvatarSrc(final HeaderViewHolder vh){
        ImageLoader imageLoader = ImageLoaderHelper.getImageLoader(getContext());
        final LoginHelper loginHelper = LoginHelper.getInstance(getContext());
        final String src = Constant.AVATAR_SRC + loginHelper.getMemberID() + AVATAR_EXTNAME[mCurrentExtname];

        L.d("Trying to load avatar: %s", src);

        imageLoader.get(src, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                L.d("Valid avatar found: %s", src);

                loginHelper.setAvatar(src);
                vh.avatarView.setImageBitmap(imageContainer.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                L.e(volleyError);
                mCurrentExtname++;

                if (mCurrentExtname < AVATAR_EXTNAME.length){
                    tryAvatarSrc(vh);
                } else {
                    // TODO: load placeholder avatar
                }
            }
        });
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.avatar)
        ImageView avatarView;

        @InjectView(R.id.name)
        TextView nameView;

        @InjectView(R.id.hint)
        TextView hintView;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}

package dulleh.akhyou;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.util.AdapterClickListener;

public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final AdapterClickListener<Anime> adapterClickListener;
    private static final int VIEW_TYPE_HEADER = -1;
    private static final int VIEW_TYPE_ITEM = 0;

    private List<Anime> favourites;

    public DrawerAdapter (AdapterClickListener<Anime> adapterClickListener, List<Anime> favourites) {
        this.adapterClickListener = adapterClickListener;
        this.favourites = favourites;
    }

    public void setFavourites (List<Anime> favourites) {
        this.favourites = favourites;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.drawer_favourite_header, parent, false);
            return new HeaderViewHolder(view);
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_favourite_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            int majorColour = getItem(position).getMajorColour();
            if (majorColour == 0) {
                itemViewHolder.iconView.setColorFilter(MainApplication.RED_ACCENT_RGB, PorterDuff.Mode.SRC_ATOP);
            } else {
                itemViewHolder.iconView.setColorFilter(majorColour, PorterDuff.Mode.SRC_ATOP);
            }
            itemViewHolder.titleView.setText(getItem(position).getTitle());
            itemViewHolder.itemView.setOnClickListener(view -> adapterClickListener.onCLick(getItem(position), position));
        }
    }

    private Anime getItem (int position) {
        return favourites.get(position -1);
    }

    @Override
    public int getItemCount() {
        return favourites.size() + 1;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout itemView;
        public TextView titleView;
        public ImageView iconView;
        public ItemViewHolder(View v) {
            super(v);
            itemView = (RelativeLayout) v.findViewById(R.id.drawer_favourite_item);
            titleView = (TextView) v.findViewById(R.id.favourite_title_view);
            iconView = (ImageView) v.findViewById(R.id.favourite_icon);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }
}

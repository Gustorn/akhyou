package dulleh.akhyou.search.item;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


import java.util.List;

import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.R;
import dulleh.akhyou.search.SearchHolderFragment;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {
    private Context context;
    private SearchFragment searchFragment;

    public SearchListAdapter(SearchFragment searchFragment) {
        this.searchFragment = searchFragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;
        public TextView descView;
        public ImageView imageView;
        public RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);
            relativeLayout = (RelativeLayout) v.findViewById(R.id.relativeLayout);
            titleView = (TextView) relativeLayout.findViewById(R.id.title_view);
            descView = (TextView) relativeLayout.findViewById(R.id.desc_view);
            imageView = (ImageView) relativeLayout.findViewById(R.id.image_view);
        }
    }

    @Override
    public SearchListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        context = parent.getContext();

        View v = LayoutInflater.from(context)
                .inflate(R.layout.search_card, parent, false);

        return  new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Anime anime = getItem(position);
        viewHolder.titleView.setText(anime.getTitle());
        viewHolder.descView.setText(anime.getSynopsis());

        Picasso.with(context)
                .load(anime.getImageUrl())
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(viewHolder.imageView);

        viewHolder.relativeLayout.setOnClickListener(view -> searchFragment.onCLick(getItem(position), null));

    }

    private List<Anime> searchResults () {
        return SearchHolderFragment.getSearchResults();
    }

    private Anime getItem (int position) {
        return searchResults().get(position);
    }

    @Override
    public int getItemCount() {
        return searchResults().size();
    }
}